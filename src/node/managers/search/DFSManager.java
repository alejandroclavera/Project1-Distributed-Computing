package node.managers.search;

import common.ConnectionNode;
import common.DataInfo;
import common.Query;
import common.QueryType;
import node.managers.NodeManager;

import java.rmi.RemoteException;
import java.util.*;

public class DFSManager implements SearchManager {
    private ConnectionNode connectionNode;
    private HashMap<String, DataInfo> searchResult;
    private NodeManager nodeManager;
    private HashMap<ConnectionNode, Query> lastResponseQuery;
    private Query mySearchQueryReponse;
    private Object objectToNotify;

    public DFSManager(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
        this.searchResult = new HashMap<>();
        lastResponseQuery = new HashMap<>();
        objectToNotify = new Object();
    }

    @Override
    public void setConnectionNode(ConnectionNode connectionNode) {
        this.connectionNode = connectionNode;
    }

    @Override
    public HashMap<String, DataInfo> doSearch() throws RemoteException {
        HashMap<String, Object> params = new HashMap<>();
        List<ConnectionNode> askedNodes = new ArrayList<>();
        List<DataInfo> dataInfoList;

        // Set the query params
        params.put("askedNodes", askedNodes);
        params.put("topQuery", connectionNode);
        params.put("depth", 0);
        // Add the node that init the query to the list of the askedNodes(to avoid loops)
        askedNodes.add(connectionNode);

        // Send the search query to the nodes that the current node it is connected
        dataInfoList = searchToNextLevel(new Query(QueryType.SEARCH, params), true);

        // update the dataInfo
        updateDataInfo(dataInfoList);
        return searchResult;
    }

    @Override
    public void search(Query query, ConnectionNode senderNode) throws RemoteException {
        List<DataInfo> dataInfoReceived;
        ConnectionNode topSearchNode = (ConnectionNode) query.parameters.get("topQuery");
        List<ConnectionNode> askedNodes = (List<ConnectionNode>) query.parameters.get("askedNodes");
        int depth = (int) query.parameters.get("depth") + 1;

        // Update the depth param
        query.parameters.replace("depth", depth);
        // Propagate the query to the nodes of the next levels
        dataInfoReceived = searchToNextLevel(query, false);

        // Add the contents node to the explored contentsList
        List<DataInfo> myDataInfos = nodeManager.getContentsList();
        for(DataInfo dataInfo : myDataInfos)
            dataInfo.providers.add(connectionNode);
        dataInfoReceived.addAll(myDataInfos);

        // Generate response
        HashMap<String, Object> params = new HashMap<>();
        params.put("topQuery", topSearchNode);
        params.put("depth", query.parameters.get("depth"));
        params.put("askedNodes", askedNodes);
        params.put("partialDataInfo", dataInfoReceived);
        senderNode.send(new Query(QueryType.SEARCH_RESPONSE, params), connectionNode);
    }

    @Override
    public void processSearchResponse(Query searchResponse, ConnectionNode senderNode) {
        ConnectionNode topQueryNode = (ConnectionNode) searchResponse.parameters.get("topQuery");
        int depthResponse = (int) searchResponse.parameters.get("depth");
        if(depthResponse == 1) {
            System.out.println("Last Response");
            mySearchQueryReponse = searchResponse;
        } else {
            System.out.println("Response");
            lastResponseQuery.put(topQueryNode, searchResponse);
        }
        // Notify the respond it is avadible
        synchronized (objectToNotify) {
            objectToNotify.notifyAll();
        }
    }

    @Override
    public HashMap<String, DataInfo> getSearchResults() {
        synchronized (searchResult) {
            return searchResult;
        }
    }

    private List<DataInfo> searchToNextLevel(Query query, boolean isFirstLevel) throws RemoteException {
        ConnectionNode topSearchNode = (ConnectionNode) query.parameters.get("topQuery");
        List<ConnectionNode> askedNodes = (List<ConnectionNode>) query.parameters.get("askedNodes");
        List<DataInfo> dataInfoReceived = new ArrayList<>();

        // // Send the search query to the nodes that the current node it is connected foreach node wait its response
        for (ConnectionNode nodeToSend : nodeManager.getConnectedNodesList()) {
            if (askedNodes.contains(nodeToSend))
                continue;
            askedNodes.add(nodeToSend);
            nodeToSend.send(query, connectionNode);

            // Wait to the node response
            synchronized (objectToNotify) {
                while ((isFirstLevel && mySearchQueryReponse == null)
                        || (!isFirstLevel && !lastResponseQuery.containsKey(topSearchNode))) {
                    try {
                        objectToNotify.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Get the information of the response of the node of the nextLevel
            if (isFirstLevel) {
                List<DataInfo> partialDataInfo = (List<DataInfo>) mySearchQueryReponse.parameters.get("partialDataInfo");
                askedNodes.addAll((Collection<? extends ConnectionNode>) mySearchQueryReponse.parameters.get("askedNodes"));
                dataInfoReceived.addAll(partialDataInfo);
                mySearchQueryReponse = null;
            } else {
                HashMap<String, Object> lastParams = lastResponseQuery.get(topSearchNode).parameters;
                List<DataInfo> partialDataInfoReceived = (List<DataInfo>) lastParams.get("partialDataInfo");
                dataInfoReceived.addAll(partialDataInfoReceived);
                askedNodes = (List<ConnectionNode>) query.parameters.get("askedNodes");
                lastResponseQuery.remove(topSearchNode);
            }
        }
        return dataInfoReceived;
    }

    private synchronized void updateDataInfo(List<DataInfo> dataInfos) {
        searchResult = new HashMap<>();
        for (DataInfo newDataInfo : dataInfos) {
            if (searchResult.containsKey(newDataInfo.hash)) {
                DataInfo dataInfo = searchResult.get(newDataInfo.hash);
                dataInfo.providers.addAll(newDataInfo.providers);
                dataInfo.titles.addAll(newDataInfo.titles);
            } else {
                searchResult.put(newDataInfo.hash, newDataInfo);
            }
        }
    }
}
