package node.managers;

import common.DataChunk;
import common.ConnectionNode;
import common.Query;
import common.QueryType;
import node.managers.connection.ConnectionManager;
import node.managers.connection.SimpleConnection;
import node.managers.download.DownloadManager;
import node.managers.files.FileManager;
import node.managers.search.SearchManager;

import java.io.Serializable;

public class NodeManager {
    private ConnectionNode connectionNode;
    private SearchManager searchManager;
    private DownloadManager downloadManager;
    private ConnectionManager connectionManager;
    private FileManager fileManager;


    public NodeManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.connectionManager = new SimpleConnection();
    }

    public void setConnectionNode(ConnectionNode connectionNode) {
        this.connectionNode = connectionNode;
        connectionManager.setConnectionNode(connectionNode);
    }

    public void connectTo(String host) {
        connectionManager.connect(host);
    }

    public void processQuery(Query query, ConnectionNode senderNode) {
        if (query.queryType == QueryType.CONNECTION)
            connectionManager.processConnexion(query, senderNode);
        else if (query.queryType == QueryType.CONNECTION_ACCEPTED)
            connectionManager.notifyConnection(query, senderNode);
        else if (query.queryType == QueryType.CONNECTION_REJECTED)
            connectionManager.notifyConnection(query, senderNode);
        else if (query.queryType == QueryType.SEARCH)
            searchManager.search(query, senderNode);
        else if (query.queryType == QueryType.DOWNLOAD)
            downloadManager.upload(query, senderNode);
    }

    public void receiveContent(DataChunk chunk, ConnectionNode sender) {
        downloadManager.download(chunk, sender);
    }

}
