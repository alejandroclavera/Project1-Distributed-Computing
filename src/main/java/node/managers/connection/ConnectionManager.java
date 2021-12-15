package node.managers.connection;

import common.ConnectionNode;
import common.Query;

import java.util.List;

public interface ConnectionManager {
    void setConnectionNode(ConnectionNode connectionNode);
    boolean connect(String host);
    boolean connect(String host, int port);
    void forceRemoveConnection(ConnectionNode nodeToRemove);
    void processConnexion(Query connectionQuery);
    void notifyConnection(Query connectionResponse);
    List<ConnectionNode> getConnectedNodesList();
}
