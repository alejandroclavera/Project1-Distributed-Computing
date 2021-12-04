package node.managers.connection;


import common.ConnectionNode;
import common.Query;
import common.QueryType;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SimpleConnection implements ConnectionManager {
    private List<ConnectionNode> connectedNodes;
    private List<ConnectionNode> pendingNodes;
    private ConnectionNode connectionNode;

    @Override
    public void setConnectionNode(ConnectionNode connectionNode) {
        this.connectionNode = connectionNode;
        connectedNodes = new ArrayList<>();
        pendingNodes = new ArrayList<>();
    }

    @Override
    public void connect(String host) {
        connect(host, 1099);
    }

    @Override
    public void connect(String host, int port) {
        try {
            //Get the ConnectionNode object of the node to connect
            Registry registry = LocateRegistry.getRegistry(host, port);
            ConnectionNode nodeToConnect = (ConnectionNode) registry.lookup("node");
            Query connectionQuery = new Query(QueryType.CONNECTION, null);
            // Send the connection query
            synchronized (pendingNodes) {
                pendingNodes.add(nodeToConnect);
            }
            nodeToConnect.send(connectionQuery, connectionNode);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processConnexion(Query connectionQuery, ConnectionNode senderNode) {
        HashMap<String, Object> parameters = new HashMap<>();
        QueryType responseType;
        if (connectionQuery.queryType != QueryType.CONNECTION) {
            responseType = QueryType .CONNECTION_REJECTED;
            parameters.put("message", "Error queryType must be CONNECTION");
        } else {
            responseType = QueryType.CONNECTION_ACCEPTED;
            parameters.put("message", "Connection accepted");
            connectedNodes.add(senderNode);
            System.out.println("CONNECTION ACCEPTED");
        }
        // Send the connection response
        try {
            senderNode.send(new Query(responseType, parameters), connectionNode);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notifyConnection(Query connectionResponse, ConnectionNode node) {
        synchronized (pendingNodes){
            if (connectionResponse.queryType == QueryType.CONNECTION_ACCEPTED) {
                if (pendingNodes.contains(node)) {
                    pendingNodes.remove(node);
                    synchronized (connectedNodes) {
                        connectedNodes.add(node);
                    }
                }
            } else if (connectionResponse.queryType == QueryType.CONNECTION_REJECTED) {
                if (pendingNodes.contains(node))
                    pendingNodes.remove(node);
            }
        }
    }

    @Override
    public List<ConnectionNode> getConnectedNodesList() {
        synchronized (connectedNodes) {
            return new ArrayList<>(connectedNodes);
        }
    }
}
