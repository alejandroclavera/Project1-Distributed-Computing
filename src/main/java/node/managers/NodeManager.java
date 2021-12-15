package node.managers;

import common.*;
import node.NodeConfiguration;
import node.logs.LogSystem;
import node.managers.connection.ConnectionManager;
import node.managers.connection.SimpleConnection;
import node.managers.download.DownloadManager;
import node.managers.download.SplitDownloadManager;
import node.managers.files.FileManager;
import node.managers.files.FileSystemManger;
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

    public NodeManager() {
        this.fileManager = new FileSystemManger(NodeConfiguration.contentDirectory);
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

    public void processQuery(Query query) throws RemoteException {
        // Process the query
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


    public HashMap<String, DataInfo> search(HashMap<String, String> filterBy) throws RemoteException {
        // Operate the filtered search
        return searchManager.doSearch(filterBy);
    }

    public void downloadContent(String hash) {
        try {
            downloadManager.download(hash);
        } catch (RemoteException e) {
            LogSystem.logErrorMessage("Can't download de content");
        }
    }

    public void addMetadata(String hash, HashMap<String, String> metadata) {
        fileManager.addMetadata(hash, metadata);
    }

    public void validateContent(DataInfo dataInfo) {
        fileManager.validate(dataInfo);
    }
    
    //***********************************************************************
    // * METHODS THAT ALLOW THE INTERACTION BETWEEN THE DIFERENTS MANGAGERS *
    //***********************************************************************

    public void forceRemoveConnection(ConnectionNode nodeToRemove) {
        // Force remove the connection with the node
        connectionManager.forceRemoveConnection(nodeToRemove);
    }

    public FileInputStream getContent(String hash) {
        // Get info of the content of the node
        return fileManager.getContent(hash);
    }

    public void addNewContent(String name, byte[] bytes) {
        // Allow to add new content from array of bytes
        fileManager.addNewContent(name, bytes);
    }

    public void addNewContent(String name, List<DataChunk> dataChunkList) {
        // Allow to add new content from a list of DataChunk
        fileManager.addNewContent(name, dataChunkList);
    }

    public void addContentsBytesToTMPFile(String hash, List<DataChunk> dataChunks) {
        // Allow to write the list of DataChunks in a temporal file
        fileManager.writeInTemporalFile(hash, dataChunks);
    }

    public void tmpFileToFile(String hash, String contentFileName) {
        // Allow to write the temporal file to end content file
        fileManager.temporalToFile(hash, contentFileName);
    }

    public List<ConnectionNode> getProviders(String hash) {
        // Get a list of the providers of the content
        List<ConnectionNode> providers = searchManager.getSearchResults().get(hash).providers;
        return new ArrayList<>(providers);
    }

    public List<DataInfo> getContentsList() {
        // Get the list of contents of the node
        return fileManager.getContentsList();
    }

    public List<ConnectionNode> getConnectedNodesList() {
        return connectionManager.getConnectedNodesList();
    }

    public DataInfo getDataInfo(String hash) {
        // Get the data info of a file

        // Get the info obtained in the search
        List<DataInfo> dataInfos = getContentsList();
        if (searchManager.getSearchResults().containsKey(hash))
            return searchManager.getSearchResults().get(hash);

        // Get the info in the local info because in the network it is not found
        for(DataInfo dataInfo : dataInfos) {
            if (dataInfo.hash.equals(hash)) {
                return dataInfo;
            }
        }
        return null;
    }

    public String getDownloadStatus() {
        return downloadManager.getDownloadStatus();
    }

    public void recogniceContents() {
        fileManager.recognizeContents();
    }
}
