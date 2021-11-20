package node.managers;

import common.NodeConnexion;
import common.Query;

public interface SearchManager {
    void search(Query query, NodeConnexion nodeConnexion);
}
