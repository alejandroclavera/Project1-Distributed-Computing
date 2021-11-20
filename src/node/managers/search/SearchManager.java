package node.managers.search;

import common.NodeConnexion;
import common.Query;

public interface SearchManager {
    void search(Query query, NodeConnexion nodeConnexion);
}
