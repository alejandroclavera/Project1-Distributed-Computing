package node.managers.connection;

import common.ConnectionNode;
import common.Query;

import java.util.List;

public interface ConnectionManager {
    public void setConnectionNode(ConnectionNode connectionNode);
    public void connect(String host);
    public void processConnexion(Query connectionQuery, ConnectionNode senderNode);
    public void notifyConnection(Query connectionResponse, ConnectionNode node);
    public List<ConnectionNode> getConnectedNodesList();
}
