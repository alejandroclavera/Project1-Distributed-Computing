package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ConnectionNode extends Remote {
    public void send(Query query) throws RemoteException;
    public void send(DataChunk dataChunk) throws RemoteException;
}
