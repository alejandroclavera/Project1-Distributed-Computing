package node.managers;

import common.*;
import node.managers.connection.ConnectionManager;
import node.managers.connection.SimpleConnection;
import node.managers.download.DownloadManager;
import node.managers.download.SplitDownloadManager;
import node.managers.files.FileManager;
import node.managers.search.DFSManager;
import node.managers.search.SearchManager;


import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NodeManager {
    private ConnectionNode connectionNode;
    private SearchManager searchManager;
    private DownloadManager downloadManager;
    private ConnectionManager connectionManager;
    private FileManager fileManager;


    public NodeManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.searchManager = new DFSManager(this);
        this.downloadManager = new SplitDownloadManager(this);
        this.connectionManager = new SimpleConnection();

    }

    public void setConnectionNode(ConnectionNode connectionNode) {
        this.connectionNode = connectionNode;
        connectionManager.setConnectionNode(connectionNode);
        searchManager.setConnectionNode(connectionNode);
        downloadManager.setConnectionNode(connectionNode);
    }

    public boolean connectTo(String host) {
        return connectionManager.connect(host);
    }

    public boolean connectTo(String host, int port) {
        return connectionManager.connect(host, port);
    }

    public void processQuery(Query query, ConnectionNode senderNode) throws RemoteException {
        if (query.queryType == QueryType.CONNECTION)
            connectionManager.processConnexion(query);
        else if (query.queryType == QueryType.CONNECTION_ACCEPTED)
            connectionManager.notifyConnection(query);
        else if (query.queryType == QueryType.CONNECTION_REJECTED)
            connectionManager.notifyConnection(query);
        else if (query.queryType == QueryType.SEARCH)
            searchManager.search(query);
        else if (query.queryType == QueryType.SEARCH_RESPONSE)
            searchManager.processSearchResponse(query);
        else if (query.queryType == QueryType.DOWNLOAD)
            downloadManager.upload(query);
    }

    public void receiveContent(DataChunk chunk) {
        downloadManager.download(chunk);
    }

    public HashMap<String, DataInfo> search() throws RemoteException {
        return searchManager.doSearch();
    }


    public void downloadContent(String hash) {
        try {
            downloadManager.download(hash);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
    //***********************************************************************
    // * METHODS THAT ALLOW THE INTERACTION BETWEEN THE DIFERENTS MANGAGERS *
    //***********************************************************************

    public void forceRemoveConnection(ConnectionNode nodeToRemove) {
        connectionManager.forceRemoveConnection(nodeToRemove);
    }

    public FileInputStream getContent(String hash) {
        return fileManager.getContent(hash);
    }

    public void addNewContent(String name, byte[] bytes) {
        fileManager.addNewContent(name, bytes);
    }

    public void addNewContent(String name, List<DataChunk> dataChunkList) {
        fileManager.addNewContent(name, dataChunkList);
    }

    public void addContentsBytesToTMPFile(String hash, List<DataChunk> dataChunks) {
        fileManager.writeInTemporalFile(hash, dataChunks);
    }

    public void tmpFileToFile(String hash, String contentFileName) {
        fileManager.temporalToFile(hash, contentFileName);
    }

    public List<ConnectionNode> getProviders(String hash) {
        List<ConnectionNode> providers = searchManager.getSearchResults().get(hash).providers;
        return new ArrayList<>(providers);
    }

    public List<DataInfo> getContentsList() {
        return fileManager.getContentsList();
    }

    public List<ConnectionNode> getConnectedNodesList() {
        return connectionManager.getConnectedNodesList();
    }

    public DataInfo getDataInfo(String hash) {
        List<DataInfo> dataInfos = getContentsList();
        for(DataInfo dataInfo : dataInfos) {
            if (dataInfo.hash.equals(hash)) {
                return dataInfo;
            }
        }
        return searchManager.getSearchResults().get(hash);
    }

}
