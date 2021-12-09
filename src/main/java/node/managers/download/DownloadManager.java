package node.managers.download;

import common.ConnectionNode;
import common.DataChunk;
import common.Query;

import java.rmi.RemoteException;

public interface DownloadManager {
    void setConnectionNode(ConnectionNode connectionNode);
    void download(String hash) throws RemoteException;
    public void download(DataChunk dataChunk);
    public void upload(Query query) throws RemoteException;
}
