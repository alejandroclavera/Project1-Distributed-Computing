package node;

import common.DataChunk;
import common.NodeConnexion;
import common.Query;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class NetworkConnexionNode extends UnicastRemoteObject implements NodeConnexion {

    protected NetworkConnexionNode() throws RemoteException {
    }

    @Override
    public void send(Query query, NodeConnexion senderNode) {

    }

    @Override
    public void send(DataChunk dataChunk, NodeConnexion senderNode) {

    }
}
