package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ConnectionNode extends Remote {
    public void send(Query query, ConnectionNode senderNode) throws RemoteException;
    public void send(DataChunk dataChunk, ConnectionNode senderNode) throws RemoteException;
}
