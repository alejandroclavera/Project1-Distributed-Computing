package node.managers.search;

import common.ConnectionNode;
import common.Query;

public interface SearchManager {
    void search(Query query, ConnectionNode nodeConnexion);
}
