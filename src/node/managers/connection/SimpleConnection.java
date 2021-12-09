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
    public boolean connect(String host) {
        return connect(host, 1099);
    }

    @Override
    public boolean connect(String host, int port) {
        ConnectionNode nodeToConnect = null;
        try {
            //Get the ConnectionNode object of the node to connect
            Registry registry = LocateRegistry.getRegistry(host, port);
            nodeToConnect = (ConnectionNode) registry.lookup("node");
            Query connectionQuery = new Query(QueryType.CONNECTION, null, connectionNode);
            // Send the connection query
            synchronized (pendingNodes) {
                pendingNodes.add(nodeToConnect);
            }
            nodeToConnect.send(connectionQuery);
            synchronized (connectedNodes) {
                while (!connectedNodes.contains(nodeToConnect) && connectedNodes.contains(connectionNode)) {
                    connectedNodes.wait();
                }
            }
        } catch (RemoteException e) {
            if (nodeToConnect != null)
                pendingNodes.remove(nodeToConnect);
            return false;
        } catch (NotBoundException | InterruptedException e ) {
            return false;
        }
        return true;
    }

    @Override
    public void forceRemoveConnection(ConnectionNode nodeToRemove) {
        synchronized (connectedNodes) {
            if (connectedNodes.contains(nodeToRemove))
                connectedNodes.remove(nodeToRemove);
        }
    }

    @Override
    public void processConnexion(Query connectionQuery) {
        ConnectionNode senderNode = connectionQuery.senderNode;
        HashMap<String, Object> parameters = new HashMap<>();
        QueryType responseType;
        if (connectionQuery.queryType != QueryType.CONNECTION) {
            responseType = QueryType .CONNECTION_REJECTED;
            parameters.put("message", "Error queryType must be CONNECTION");
        } else {
            responseType = QueryType.CONNECTION_ACCEPTED;
            parameters.put("message", "Connection accepted");
            synchronized (connectedNodes) {
                connectedNodes.add(senderNode);
            }
            System.out.println("CONNECTION ACCEPTED");
        }
        // Send the connection response
        try {
            senderNode.send(new Query(responseType, parameters, connectionNode));
        } catch (RemoteException e) {
           // If a exception occurred remove the node of the list of connectedNodes
           synchronized (connectedNodes) {
               if (connectedNodes.contains(senderNode))
                   connectedNodes.remove(senderNode);
           }
        }
    }

    @Override
    public void notifyConnection(Query connectionResponse) {
        ConnectionNode node = connectionResponse.senderNode;
        synchronized (pendingNodes){
            if (connectionResponse.queryType == QueryType.CONNECTION_ACCEPTED) {
                if (pendingNodes.contains(node)) {
                    pendingNodes.remove(node);
                    synchronized (connectedNodes) {
                        connectedNodes.add(node);
                        connectedNodes.notifyAll();
                    }
                }
            } else if (connectionResponse.queryType == QueryType.CONNECTION_REJECTED) {
                if (pendingNodes.contains(node))
                    pendingNodes.remove(node);
                    connectedNodes.notifyAll();
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
