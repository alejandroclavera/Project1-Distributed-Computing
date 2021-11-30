package node;

import common.ConnectionNode;
import common.DataChunk;
import common.Query;
import node.managers.NodeManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class NetworkConnexionNode extends UnicastRemoteObject implements ConnectionNode {
    NodeManager nodeManager;

    public NetworkConnexionNode(NodeManager nodeManager) throws RemoteException {
        this.nodeManager = nodeManager;
    }

    @Override
    public void send(Query query, ConnectionNode senderNode) throws RemoteException {
        nodeManager.processQuery(query, senderNode);
    }

    @Override
    public void send(DataChunk dataChunk, ConnectionNode senderNode) {
        nodeManager.receiveContent(dataChunk, senderNode);
    }
}
