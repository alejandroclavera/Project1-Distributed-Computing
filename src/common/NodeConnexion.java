package common;

import java.rmi.Remote;

public interface NodeConnexion extends Remote {
    public void send(Query query, NodeConnexion senderNode);
    public void send(DataChunk dataChunk, NodeConnexion senderNode);
}
