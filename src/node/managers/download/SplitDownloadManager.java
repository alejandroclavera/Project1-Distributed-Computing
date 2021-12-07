package node.managers.download;

import common.*;
import node.managers.NodeManager;

import java.io.*;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        if (providers.size() != 0) {
            downLoadProccess(contentDataInfo, providers);
        }
    }

    @Override
    public void download(DataChunk dataChunk, ConnectionNode sender) {
        String hash = dataChunk.hash;
        List<DataChunk> dataChunks = pendingDownload.get(hash);
        DataInfo contentDataInfo = nodeManager.getDataInfo(dataChunk.hash);
        synchronized (dataChunks) {
            dataChunks.add((int)dataChunk.chunkNumber % chunckWindowSize, dataChunk);
            if (dataChunks.size() != 0 && (dataChunks.size()  % chunckWindowSize) == 0) {
                writeContent(hash);
                dataChunks.clear();
                dataChunks.notifyAll();
            }
        }
    }

    @Override
    public void upload(Query query, ConnectionNode toNode) throws RemoteException {
        byte bytes[] = new byte[numBytesChunk];
        HashMap<String, Object> paramas = query.parameters;
        String hash = (String) paramas.get("hash");
        DataInfo dataInfo = nodeManager.getDataInfo(hash);
        long chunkNumber = Long.parseLong((String) paramas.get("chunkNumber"));
        FileInputStream fileInputStream = nodeManager.getContent(hash);
        try {
            fileInputStream.skip(numBytesChunk * chunkNumber);
            int size = fileInputStream.read(bytes, 0, numBytesChunk);
            DataChunk dataChunk = new DataChunk(hash, dataInfo.titles.get(0), (int)chunkNumber, size, bytes);
            toNode.send(dataChunk, connectionNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downLoadProccess(DataInfo contentDataInfo, List<ConnectionNode> providers) throws RemoteException {
        String hash = contentDataInfo.hash;
        long numberOfChunks = contentDataInfo.size / numBytesChunk;

        if (contentDataInfo.size % numBytesChunk != 0)
            numberOfChunks += 1;

        pendingDownload.put(contentDataInfo.hash, new ArrayList<>());

        List<DataChunk> chunckBytesList = pendingDownload.get(hash);
        for (long chunkNumber = 0; chunkNumber < numberOfChunks; chunkNumber++) {
            ConnectionNode nodeToSend = providers.get((int)(chunkNumber % providers.size()));
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("hash",  hash);
            parameters.put("chunkNumber", String.valueOf(chunkNumber));
            Query query = new Query(QueryType.DOWNLOAD, parameters);
            nodeToSend.send(query, connectionNode);
            if (chunkNumber == 0  || (chunkNumber + 1) % chunckWindowSize != 0)
                continue;
            // Wait to the X chunks
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
        writeContent(hash);
        nodeManager.tmpFileToFile(hash, contentDataInfo.titles.get(0));
    }

    private void writeContent(String hash) {
        List<DataChunk> chunkList = pendingDownload.get(hash);
        DataInfo contentDataInfo = nodeManager.getDataInfo(hash);
        /*
        for (DataChunk dataChunk : chunkList) {
            try {
                RandomAccessFile randomAccessFile = new RandomAccessFile(new File("contentsClient/"+ contentDataInfo.titles.get(0)), "rws");
                randomAccessFile.seek(dataChunk.chunkNumber * numBytesChunk);
                randomAccessFile.write(dataChunk.chunkBytes, 0, dataChunk.size);
                randomAccessFile.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Excepcion");
            }
        }
         */
        nodeManager.addContentsBytesToTMPFile(hash, chunkList);
    }
}
