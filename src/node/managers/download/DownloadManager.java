package node.managers.download;

import common.ConnectionNode;
import common.DataChunk;
import common.Query;

public interface DownloadManager {
    public void download(DataChunk dataChunk, ConnectionNode sender);
    public void upload(Query query, ConnectionNode toNode);
}
