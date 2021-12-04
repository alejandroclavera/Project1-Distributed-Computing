package node.managers.download;

import common.*;
import node.managers.NodeManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SplitDownloadManager implements DownloadManager {
    private ConnectionNode connectionNode;
    private NodeManager nodeManager;
    private HashMap<String, List<DataChunk>> pendingDownload;
    private int numBytesChunk = 100;


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
        long numberOfChunks = contentDataInfo.size / numBytesChunk;
        if (contentDataInfo.size % numBytesChunk != 0)
            numberOfChunks += 1;
        pendingDownload.put(hash, new ArrayList<>());
        List<ConnectionNode> providers = contentDataInfo.providers;
        for (long chunkNumber = 0; chunkNumber < numberOfChunks; chunkNumber++) {
            ConnectionNode nodeToSend = providers.get((int)(chunkNumber % providers.size()));
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("hash",  hash);
            parameters.put("chunkNumber", String.valueOf(chunkNumber));
            Query query = new Query(QueryType.DOWNLOAD, parameters);
            nodeToSend.send(query, connectionNode);
        }
        List<DataChunk> chunckBytesList = pendingDownload.get(hash);
        synchronized (chunckBytesList) {
            while (chunckBytesList.size() < numberOfChunks) {
                try {
                    chunckBytesList.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        writeContent(hash);
    }

    @Override
    public void download(DataChunk dataChunk, ConnectionNode sender) {
        String hash = dataChunk.hash;
        List<DataChunk> dataChunks = pendingDownload.get(hash);
        synchronized (dataChunks) {
            dataChunks.add((int)dataChunk.chunkNumber, dataChunk);
            dataChunks.notifyAll();
        }
    }

    @Override
    public void upload(Query query, ConnectionNode toNode) throws RemoteException {
        byte bytes[] = new byte[numBytesChunk];
        HashMap<String, Object> paramas = (HashMap<String, Object>) query.parameters;
        String hash = (String) paramas.get("hash");
        DataInfo dataInfo = nodeManager.getDataInfo(hash);
        long chunkNumber = Long.parseLong((String) paramas.get("chunkNumber"));
        FileInputStream fileInputStream = nodeManager.getContent(hash);
        try {
            fileInputStream.skip(numBytesChunk * chunkNumber);
            fileInputStream.read(bytes, 0, numBytesChunk);
            DataChunk dataChunk = new DataChunk(hash, dataInfo.titles.get(0), (int)chunkNumber, bytes);
            toNode.send(dataChunk, connectionNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeContent(String hash) {
        List<DataChunk> chunkList = pendingDownload.get(hash);
        List<byte[]> bytesList = new ArrayList<>();
        for (DataChunk dataChunk: chunkList) {
            bytesList.add(dataChunk.chunkBytes);
        }
        DataInfo dataInfo = nodeManager.getDataInfo(hash);
        nodeManager.addNewContent(dataInfo.titles.get(0), bytesList);
    }
}
