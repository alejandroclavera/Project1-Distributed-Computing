package node.managers;

import common.DataChunk;
import common.NodeConnexion;
import common.Query;

public interface DownloadManager {
    public void download(DataChunk dataChunk, NodeConnexion sender);
    public void upload(Query query, NodeConnexion toNode);
}
