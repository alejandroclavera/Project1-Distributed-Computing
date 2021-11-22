package node;

import common.DataChunk;
import common.NodeConnexion;
import common.Query;

public class NodeManager {

    public NodeManager(FileManager fileManager) {
    }

    public void processQuery(Query query, NodeConnexion senderNode) {
    }
    
    public void receiveContent(DataChunk chunk, NodeConnexion sender){
        downloadManager.download(chunk, sender);
    }
}
