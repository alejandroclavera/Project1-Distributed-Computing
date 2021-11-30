package node.managers;

import common.*;
import node.managers.connection.ConnectionManager;
import node.managers.connection.SimpleConnection;
import node.managers.download.DownloadManager;
import node.managers.download.SimpleDownloadManger;
import node.managers.files.FileManager;
import node.managers.search.SearchManager;
import node.managers.search.SimpleSearch;

import java.io.FileInputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class NodeManager {
    private ConnectionNode connectionNode;
    private SearchManager searchManager;
    private DownloadManager downloadManager;
    private ConnectionManager connectionManager;
    private FileManager fileManager;


    public NodeManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.searchManager = new SimpleSearch(this);
        this.downloadManager = new SimpleDownloadManger(this);
        this.connectionManager = new SimpleConnection();

    }

    public void setConnectionNode(ConnectionNode connectionNode) {
        this.connectionNode = connectionNode;
        connectionManager.setConnectionNode(connectionNode);
        searchManager.setConnectionNode(connectionNode);
        downloadManager.setConnectionNode(connectionNode);
    }

    public void connectTo(String host) {
        connectionManager.connect(host);
    }

    public void processQuery(Query query, ConnectionNode senderNode) throws RemoteException {
        if (query.queryType == QueryType.CONNECTION)
            connectionManager.processConnexion(query, senderNode);
        else if (query.queryType == QueryType.CONNECTION_ACCEPTED)
            connectionManager.notifyConnection(query, senderNode);
        else if (query.queryType == QueryType.CONNECTION_REJECTED)
            connectionManager.notifyConnection(query, senderNode);
        else if (query.queryType == QueryType.SEARCH)
            searchManager.search(query, senderNode);
        else if (query.queryType == QueryType.SEARCH_RESPONSE)
            searchManager.processSearchResponse(query, senderNode);
        else if (query.queryType == QueryType.DOWNLOAD)
            downloadManager.upload(query, senderNode);
    }

    public void receiveContent(DataChunk chunk, ConnectionNode sender) {
        downloadManager.download(chunk, sender);
    }


    public List<DataInfo> search() throws RemoteException {
        return searchManager.doSearch();
    }

    public void downloadContent(String hash) {
        try {
            downloadManager.download(hash);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public List<ConnectionNode> getConnectedNodesList() {
        return connectionManager.getConnectedNodesList();
    }

    public List<DataInfo> getContentsList() {
        return fileManager.getContentsList();
    }

    public List<ConnectionNode> getProviders(String hash) {
        return searchManager.getProviders().get(hash);
    }

    public FileInputStream getContent(String hash) {
        return fileManager.getContent(hash);
    }

    public void addNewContent(String name, byte[] bytes) {
        fileManager.addNewContent(name, bytes);
    }

}
