package common;

import java.io.Serializable;
import java.util.HashMap;

public class Query implements Serializable {
    public final QueryType queryType;
    public final HashMap<String, Object> parameters;

    public Query(QueryType queryType, HashMap<String, Object> parameters) {
        this.queryType = queryType;
        this.parameters = parameters;
    }
}
