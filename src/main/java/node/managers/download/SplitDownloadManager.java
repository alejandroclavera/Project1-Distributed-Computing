package node.managers.download;

import common.*;
import node.NodeConfiguration;
import node.logs.LogSystem;
import node.managers.NodeManager;


import java.io.*;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SplitDownloadManager implements DownloadManager {
    private ConnectionNode connectionNode;
    private NodeManager nodeManager;
    private HashMap<String, List<DataChunk>> pendingDownload;
    private HashMap<String, DownloadStatus> downloadStatus;
    private int chunkWindowSize = NodeConfiguration.chunkWindowSize ;
    private int numBytesChunk = NodeConfiguration.numBytesChunk;
    // Thread control attributes
    private int nThreadsContentDownload = 0;
    private Queue<String> downloadContentQueue = new ConcurrentLinkedQueue<>();
    private int nThreadsDownload = 0;
    private Queue<DataChunk> downloadQueue = new ConcurrentLinkedQueue<>();
    private int nThreadsUpload = 0;
    private Queue<Query> uploadQuery = new ConcurrentLinkedQueue<>();

    public SplitDownloadManager(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
        pendingDownload = new HashMap<>();
        downloadStatus = new HashMap<>();
    }

    @Override
    public void setConnectionNode(ConnectionNode connectionNode) {
        this.connectionNode = connectionNode;
    }

    public void download(String hash) throws RemoteException {
        boolean downloadContent = false;
        synchronized (downloadContentQueue) {
            if (nThreadsDownload < NodeConfiguration.numMaxDownloadThreads) {
                downloadContent = true;
                nThreadsDownload +=1;
            } else {
                downloadContentQueue.add(hash);
            }
        }

        if (downloadContent) {
            new Thread(() -> {
                downloadThread(hash);
            }).start();
        }
    }

    @Override
    public String getDownloadStatus() {
        String statusFormated = "";
        for (String hash : downloadStatus.keySet()) {
            DataInfo dataInfo = nodeManager.getDataInfo(hash);
            statusFormated += dataInfo.titles.get(0) + " " + downloadStatus.get(hash).toString() + "\n";
        }
        return statusFormated;
    }

    @Override
    public void download(DataChunk dataChunk) {
        String hash = dataChunk.hash;
        List<DataChunk> dataChunks = pendingDownload.get(hash);
        DataInfo contentDataInfo = nodeManager.getDataInfo(dataChunk.hash);
        DownloadStatus status = downloadStatus.get(hash);
        synchronized (dataChunks) {
            // Add the new dataChunk to the list
            LogSystem.logInfoMessage("chunk: " + dataChunk.chunkNumber);
            if (!dataChunks.contains(dataChunk))
                dataChunks.add(dataChunk);
            status.numChunksDownloaded += 1;
            // If all the window fragments are in the queue, are written to a temporary file
            if ((dataChunks.size() - 1)  % chunkWindowSize == 0 || status.numWindow == status.maxWindows) {
                writeContent(hash);
                // Clean the dataChunks of the windows
                dataChunks.clear();
                // Notifies to the download process to continue with the next chunks window
                dataChunks.notifyAll();
            }
        }
    }

    @Override
    public void upload(Query query)  {
        Query nextQuery=null;

        // Add the query to uploadQuery queue (priority the first queries)
        uploadQuery.add(query);
        synchronized (uploadQuery) {
            // Check if the current number of upload Threads exceeds the maximum
            if (nThreadsUpload < NodeConfiguration.numMaxUploadThreads){
                nextQuery = uploadQuery.poll();
                nThreadsUpload += 1;
            }
        }
        // Attend the queries
        while (nextQuery != null) {
            // Process the next upload query
            uploadProcess(nextQuery);
            nextQuery = uploadQuery.poll();
            // if the queue is empty nextQueue = null -> the thread end
            if (nextQuery == null) {
                synchronized (uploadQuery){
                    nThreadsUpload -= 1;
                }
            }
        }
    }

    private void downloadThread(String firstHash) {
        String hash = firstHash;
        do {
            try {
                downloadContent(hash);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            synchronized (downloadContentQueue) {
                if (downloadContentQueue.size() != 0)
                    hash = downloadContentQueue.peek();
                else {
                    hash = null;
                    nThreadsContentDownload -=1;
                }
            }
        } while (hash != null);
    }

    private void downloadContent(String hash) throws RemoteException {
        DataInfo contentDataInfo = nodeManager.getDataInfo(hash);
        List<ConnectionNode> providers = nodeManager.getProviders(hash);

        // Download the content if the content have a providers and the node don't have it
        if (!nodeManager.getContentsList().contains(contentDataInfo) && providers.size() != 0) {
            downLoadProcess(contentDataInfo, providers);
            // Validate the content
            nodeManager.validateContent(contentDataInfo);
            LogSystem.logInfoMessage("Download completed");
        }
    }

    private void downloadChunk(DataChunk dataChunk) {
        String hash = dataChunk.hash;
        List<DataChunk> dataChunks = pendingDownload.get(hash);
        DataInfo contentDataInfo = nodeManager.getDataInfo(dataChunk.hash);
        DownloadStatus status = downloadStatus.get(hash);
        synchronized (dataChunks) {
            // Add the new dataChunk to the list
            LogSystem.logInfoMessage("chunk: " + dataChunk.chunkNumber);
            if (!dataChunks.contains(dataChunk))
                dataChunks.add(dataChunk);
            status.numChunksDownloaded += 1;
            // If all the window fragments are in the queue, are written to a temporary file
            if ((dataChunks.size() - 1)  % chunkWindowSize == 0 || status.numWindow == status.maxWindows) {
                writeContent(hash);
                // Clean the dataChunks of the windows
                dataChunks.clear();
                // Notifies to the download process to continue with the next chunks window
                dataChunks.notifyAll();
            }
        }
    }

    private void uploadProcess(Query query) {
        ConnectionNode toNode = query.senderNode;
        byte bytes[] = new byte[numBytesChunk];
        // Get the query params
        HashMap<String, Object> paramas = query.parameters;
        String hash = (String) paramas.get("hash");
        // Get the dataInfo of the file
        DataInfo dataInfo = nodeManager.getDataInfo(hash);
        long chunkNumber = Long.parseLong((String) paramas.get("chunkNumber"));
        // Get the contentFile to send
        FileInputStream fileInputStream = nodeManager.getContent(hash);
        try {
            // Find the chunk to send
            fileInputStream.skip(numBytesChunk * chunkNumber);
            // Get the size of the chunk
            int size = fileInputStream.read(bytes, 0, numBytesChunk);
            DataChunk dataChunk = new DataChunk(hash, dataInfo.titles.get(0), (int)chunkNumber, size, bytes, connectionNode);
            // Send the chunk
            sendChunk(dataChunk, toNode);
        } catch (IOException e) {
           LogSystem.logErrorMessage("Can't read chunk");
        }
    }

    private void downLoadProcess(DataInfo contentDataInfo, List<ConnectionNode> providers) throws RemoteException {
        String hash = contentDataInfo.hash;
        // Calculate the number of chunks
        long numberOfChunks = contentDataInfo.size / numBytesChunk;
        if (contentDataInfo.size % numBytesChunk != 0)
            numberOfChunks += 1;

        // Create a Chunks Queue for the download of the content
        pendingDownload.put(contentDataInfo.hash, new ArrayList<DataChunk>());
        downloadStatus.put(hash, new DownloadStatus((int) numberOfChunks, chunkWindowSize));
        DownloadStatus status = downloadStatus.get(hash);
        List<DataChunk> chunckBytesList = pendingDownload.get(hash);
        // The first one is null to prevent the process ending before the chunks arrive
        chunckBytesList.add(null);

        // Split download
        boolean allCompletedWindow = true;
        for (long chunkNumber = 0; chunkNumber < numberOfChunks; chunkNumber++) {
            // Select the next provider (cyclic way)
            ConnectionNode nodeToSend = providers.get((int)(chunkNumber % providers.size()));
            // Set query params
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("hash",  hash);
            parameters.put("chunkNumber", String.valueOf(chunkNumber));
            Query query = new Query(QueryType.DOWNLOAD, parameters, connectionNode);
            // Send the query
            sendQuery(hash, (int)chunkNumber, nodeToSend);
            if ((chunkNumber == 0  || (chunkNumber + 1) % chunkWindowSize != 0) && chunkNumber != numberOfChunks - 1)
                continue;
            // Wait for the arrival of all the window chunks
            allCompletedWindow &= waitWindowChunk(hash);
            if (!allCompletedWindow)
                return;

            chunckBytesList.add(null);
            status.numWindow += 1;
        }
        // Write the last fragments if the number of fragments is less than the size of the windows
        //writeContent(hash);
        nodeManager.tmpFileToFile(hash, contentDataInfo.titles.get(0));
        // Remove the chunk queue of the content download
        pendingDownload.remove(hash);
        downloadStatus.remove(hash);
    }

    private void sendQuery(String hash, int chunk, ConnectionNode nodeToSend) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("hash",  hash);
        parameters.put("chunkNumber", String.valueOf(chunk));
        Query query = new Query(QueryType.DOWNLOAD, parameters, connectionNode);
        try {
            nodeToSend.send(query);
        } catch (RemoteException e) {
            nodeManager.forceRemoveConnection(nodeToSend);
        }
    }

    private void sendChunk(DataChunk dataChunk, ConnectionNode nodeToSend) {
        try {
            nodeToSend.send(dataChunk);
        } catch (RemoteException e) {
            nodeManager.forceRemoveConnection(nodeToSend);
        }
    }

    private void tryCompleteWindow(String hash, int tryNumber) {
        DownloadStatus status = downloadStatus.get(hash);
        List<DataChunk> receivedChunks = pendingDownload.get(hash);
        List<ConnectionNode> providers = nodeManager.getProviders(hash);
        int firstChunkWindow = status.numWindow * chunkWindowSize;
        int endChunk;
        if (status.numWindow == status.maxWindows)
            endChunk = firstChunkWindow + (status.maxChunks - 1) - status.numChunksDownloaded - 1;
        else
            endChunk = firstChunkWindow + chunkWindowSize - 1;

        LogSystem.logInfoMessage("Start the try " + tryNumber + " to complete the windows");
        for(int chunk = firstChunkWindow; chunk <=endChunk; chunk++) {
            if (!isChunkReceived(hash, chunk)) {
                ConnectionNode nodeToSend = providers.get((int)((chunk + tryNumber) % providers.size()));
                sendQuery(hash, chunk, nodeToSend);
            }
        }
    }

    private boolean isChunkReceived(String hash, int chunkNumber) {
        List<DataChunk> downloadedChunks = pendingDownload.get(hash);
        for (DataChunk dataChunk : downloadedChunks) {
            if (dataChunk != null && dataChunk.chunkNumber == chunkNumber)
                return true;
        }
        return false;
    }

    private boolean waitWindowChunk(String hash){
        List<DataChunk> windowChunks = pendingDownload.get(hash);
        synchronized (windowChunks) {
            int trys = 0;
            if (windowChunks.size() > 0) {
                while (trys <= NodeConfiguration.maxTryWindow && windowChunks.size() > 0) {
                    try {
                        windowChunks.wait(NodeConfiguration.windowsTimeout);
                    } catch (InterruptedException e) {
                       LogSystem.logInfoMessage("Interruption");
                       System.exit(0);
                    }
                    trys += 1;
                    tryCompleteWindow(hash, trys);
                }
                if (windowChunks.size() > 0 ) {
                    LogSystem.logErrorMessage("max num of try of window");
                    return false;
                }
            }
        }
        return true;
    }

    private void writeContent(String hash) {
        List<DataChunk> chunkList = pendingDownload.get(hash);
        // Remove the first because it is null
        chunkList.remove(null);
        nodeManager.addContentsBytesToTMPFile(hash, chunkList);
    }
}

class DownloadStatus {
    int numWindow;
    int maxWindows;
    int numChunksDownloaded;
    int maxChunks;

    public DownloadStatus(int numChunks, int windowsSize) {
        this.numWindow = 0;
        this.maxWindows = numChunks / windowsSize + ((numChunks % windowsSize == 0) ? -1 : 0);
        this.numChunksDownloaded = 0;
        this.maxChunks = numChunks;
    }

    @Override
    public String toString() {
        return numChunksDownloaded + " / " + maxChunks;
    }
}
