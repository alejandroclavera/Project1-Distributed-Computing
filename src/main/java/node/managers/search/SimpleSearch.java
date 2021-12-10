package node.managers.search;

import common.ConnectionNode;
import common.DataInfo;
import common.Query;
import common.QueryType;
import jdk.jshell.spi.ExecutionControl;
import node.managers.NodeManager;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SimpleSearch implements SearchManager{
    private NodeManager nodeManager;
    private HashMap<String, DataInfo> currentSearchResults;
    private ConnectionNode connectionNode;
    private boolean endSearch;
    private int numbersToWait = 0;
    private int nReci = 0;


    public SimpleSearch(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
        endSearch = false;
    }

    @Override
    public void setConnectionNode(ConnectionNode connectionNode) {
        this.connectionNode = connectionNode;
        this.currentSearchResults = new HashMap<>();
    }

    @Override
    public HashMap<String, DataInfo> doSearch() throws RemoteException {
        List<ConnectionNode> nodesToSend = nodeManager.getConnectedNodesList();
        // Set Initial Params
        numbersToWait = nodesToSend.size();
        nReci = 0;
        // Send the search query
        for (ConnectionNode node : nodesToSend)
            node.send(new Query(QueryType.SEARCH, null, connectionNode));
        // Wait to all responses
        synchronized (currentSearchResults) {
            try {
                while (!endSearch)
                    currentSearchResults.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return new HashMap<>(currentSearchResults);
    }

    @Override
    public HashMap<String, DataInfo> doSearch(HashMap<String, String> filterBy) throws RemoteException {
        return new HashMap<>(currentSearchResults);
    }

    @Override
    public void search(Query query)  {
        ConnectionNode senderNode = query.senderNode;
        System.out.println("Procesando peticion de busqueda");
        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("contents", (ArrayList<DataInfo>)nodeManager.getContentsList());
        try {
            senderNode.send(new Query(QueryType.SEARCH_RESPONSE, queryParams, connectionNode));
        } catch (RemoteException e) {
            nodeManager.forceRemoveConnection(senderNode);
        }
    }

    @Override
    public void processSearchResponse(Query searchResponse) {
        ConnectionNode senderNode = searchResponse.senderNode;
        synchronized (currentSearchResults) {
            List<ConnectionNode> connectedNodes = nodeManager.getConnectedNodesList();
            if (connectedNodes.contains(senderNode)) {
                List<DataInfo> dataInfos = (List<DataInfo>) searchResponse.parameters.get("contents");
                // Get the content info of the node response
                for (DataInfo dataInfo : dataInfos) {
                    if (!currentSearchResults.containsKey(dataInfo.hash)) {
                        currentSearchResults.put(dataInfo.hash, new DataInfo(dataInfo.hash, dataInfo.size, null));
                    }
                    currentSearchResults.get(dataInfo.hash).titles.add(dataInfo.titles.get(0));
                    currentSearchResults.get(dataInfo.hash).providers.add(senderNode);
                }
                nReci +=1;
                if (nReci == numbersToWait) {
                    endSearch = true;
                    currentSearchResults.notifyAll();
                }
            }
        }
    }

    @Override
    public HashMap<String, DataInfo> getSearchResults() {
        return currentSearchResults;
    }

}
