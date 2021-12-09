package common;

import java.io.Serializable;
import java.util.HashMap;

public class Query implements Serializable {
    public final QueryType queryType;
    public final ConnectionNode senderNode;
    public final HashMap<String, Object> parameters;

    public Query(QueryType queryType, HashMap<String, Object> parameters, ConnectionNode senderNode) {
        this.queryType = queryType;
        this.parameters = parameters;
        this.senderNode = senderNode;
    }
}
