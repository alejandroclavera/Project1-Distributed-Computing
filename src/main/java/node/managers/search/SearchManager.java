package node.managers.search;

import common.ConnectionNode;
import common.DataInfo;
import common.Query;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

public interface SearchManager {
    void setConnectionNode(ConnectionNode connectionNode);
    HashMap<String, DataInfo> doSearch() throws RemoteException;
    HashMap<String, DataInfo> doSearch(HashMap<String, String> filterBy) throws RemoteException;
    void search(Query query) throws RemoteException;
    void processSearchResponse(Query searchResponse);
    HashMap<String, DataInfo> getSearchResults();
}
