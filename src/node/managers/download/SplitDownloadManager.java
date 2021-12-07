package node.managers.download;

import common.*;
import node.managers.NodeManager;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

public class SplitDownloadManager implements DownloadManager {
    private ConnectionNode connectionNode;
    private NodeManager nodeManager;
    private HashMap<String, List<DataChunk>> pendingDownload;
    private int chunckWindowSize = 50;
    private int numBytesChunk = 10000000;


    public SplitDownloadManager(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
        pendingDownload = new HashMap<>();
    }

    public SplitDownloadManager(NodeManager nodeManager, int numBytesChunk) {
        this(nodeManager);
        this.numBytesChunk = numBytesChunk;
    }

    @Override
    public void setConnectionNode(ConnectionNode connectionNode) {
        this.connectionNode = connectionNode;
    }

    @Override
    public void download(String hash) throws RemoteException {
        DataInfo contentDataInfo = nodeManager.getDataInfo(hash);
        List<ConnectionNode> providers = nodeManager.getProviders(hash);
        // Download the content if the content have a providers
        if (providers.size() != 0) {
            downLoadProcess(contentDataInfo, providers);
        }
    }

    @Override
    public void download(DataChunk dataChunk, ConnectionNode sender) {
        String hash = dataChunk.hash;
        List<DataChunk> dataChunks = pendingDownload.get(hash);
        DataInfo contentDataInfo = nodeManager.getDataInfo(dataChunk.hash);
        synchronized (dataChunks) {
            // Add the new dataChunk to the list
            dataChunks.add((int)dataChunk.chunkNumber % chunckWindowSize, dataChunk);
            // If all the window fragments are in the queue, are written to a temporary file
            if (dataChunks.size() != 0 && (dataChunks.size()  % chunckWindowSize) == 0) {
                writeContent(hash);
                // Clean the dataChunks of the windows
                dataChunks.clear();
                // Notifies to the download process to continue with the next chunks window
                dataChunks.notifyAll();
            }
        }
    }

    @Override
    public void upload(Query query, ConnectionNode toNode) throws RemoteException {
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
            DataChunk dataChunk = new DataChunk(hash, dataInfo.titles.get(0), (int)chunkNumber, size, bytes);
            // Send the chunk
            toNode.send(dataChunk, connectionNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downLoadProcess(DataInfo contentDataInfo, List<ConnectionNode> providers) throws RemoteException {
        String hash = contentDataInfo.hash;
        // Calculate the number of chunks
        long numberOfChunks = contentDataInfo.size / numBytesChunk;
        if (contentDataInfo.size % numBytesChunk != 0)
            numberOfChunks += 1;

        // Create a Chunks Queue for the download of the content
        pendingDownload.put(contentDataInfo.hash, new ArrayList<>());
        List<DataChunk> chunckBytesList = pendingDownload.get(hash);

        // Split download
        for (long chunkNumber = 0; chunkNumber < numberOfChunks; chunkNumber++) {
            // Select the next provider (cyclic way)
            ConnectionNode nodeToSend = providers.get((int)(chunkNumber % providers.size()));
            // Set query params
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("hash",  hash);
            parameters.put("chunkNumber", String.valueOf(chunkNumber));
            Query query = new Query(QueryType.DOWNLOAD, parameters);
            // Send the query
            nodeToSend.send(query, connectionNode);
            if (chunkNumber == 0  || (chunkNumber + 1) % chunckWindowSize != 0)
                continue;
            // Wait for the arrival of all the window chunks
            synchronized (chunckBytesList) {
                while (chunckBytesList.size() > 0) {
                    try {
                        chunckBytesList.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // Write the last fragments if the number of fragments is less than the size of the windows
        writeContent(hash);
        nodeManager.tmpFileToFile(hash, contentDataInfo.titles.get(0));
        // Remove the chunk queue of the content download
        pendingDownload.remove(hash);
    }

    private void writeContent(String hash) {
        List<DataChunk> chunkList = pendingDownload.get(hash);
        nodeManager.addContentsBytesToTMPFile(hash, chunkList);
    }
}
