package node.managers.search;

import common.ConnectionNode;
import common.DataInfo;
import common.Query;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

public interface SearchManager {
    void setConnectionNode(ConnectionNode connectionNode);
    List<DataInfo> doSearch() throws RemoteException;
    void search(Query query, ConnectionNode senderNode) throws RemoteException;
    void processSearchResponse(Query searchResponse, ConnectionNode senderNode);
    HashMap<String, List<ConnectionNode>> getProviders();
}
