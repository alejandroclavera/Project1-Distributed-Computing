package node.managers.download;

import common.*;
import node.managers.NodeManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

public class SimpleDownloadManger implements DownloadManager {
    private NodeManager nodeManager;
    private ConnectionNode connectionNode;

    public SimpleDownloadManger(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    @Override
    public void setConnectionNode(ConnectionNode connectionNode) {
        this.connectionNode = connectionNode;
    }

    @Override
    public void download(String hash) throws RemoteException {
        List<ConnectionNode> providers = nodeManager.getProviders(hash);
        if (providers != null) {
            HashMap<String, String> params = new HashMap<>();
            params.put("hash", hash);
            Query query = new Query(QueryType.DOWNLOAD, params);
            providers.get(0).send(query, connectionNode);
        }
    }

    @Override
    public void download(DataChunk dataChunk, ConnectionNode sender) {
        nodeManager.addNewContent(dataChunk.name, dataChunk.chunkBytes);
    }

    @Override
    public void upload(Query query, ConnectionNode toNode) throws RemoteException {
        String hash = (String) query.parameters.get("hash");
        FileInputStream fileStream = nodeManager.getContent(hash);
        byte bytes[];
        try {
            bytes = fileStream.readAllBytes();
            fileStream.close();
        } catch (IOException e) {
            bytes = null;
        }
        String name = nodeManager.getDataInfo(hash).titles.get(0);
        toNode.send(new DataChunk(hash, name, 0, bytes), connectionNode);
    }
}
