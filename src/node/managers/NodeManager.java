package node.managers;

import common.DataChunk;
import common.NodeConnexion;
import common.Query;
import common.QueryType;

public class NodeManager {

    private SearchManager searchManager;
    private DownloadManager downloadManager;
    private FileManager fileManager;

    public NodeManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void processQuery(Query query, NodeConnexion senderNode) {
        if (query.queryType == QueryType.SEARCH) {
            searchManager.search(query, senderNode);
        } else if (query.queryType == QueryType.DOWNLOAD){
            downloadManager.upload(query, senderNode);
        }
    }

    public void receiveContent(DataChunk chunk, NodeConnexion sender) {
        downloadManager.download(chunk, sender);
    }

}
