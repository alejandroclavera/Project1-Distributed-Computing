package node;

import common.DataChunk;
import common.NodeConnexion;
import common.Query;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class NetworkConnexionNode extends UnicastRemoteObject implements NodeConnexion {
    NodeManager nodeManager;

    public NetworkConnexionNode(NodeManager nodeManager) throws RemoteException {
        this.nodeManager = nodeManager;
    }

    @Override
    public void send(Query query, NodeConnexion senderNode) {
        nodeManager.processQuery(query, senderNode);
    }

    @Override
    public void send(DataChunk dataChunk, NodeConnexion senderNode) {
        nodeManager.receiveContent(dataChunk, senderNode);
    }
}
