package common;

import java.io.Serializable;
import java.util.HashMap;

public class Query implements Serializable {
    public final QueryType queryType;
    public final HashMap<String, String> parameters;

    public Query(QueryType queryType, HashMap<String, String> parameters) {
        this.queryType = queryType;
        this.parameters = parameters;
    }
}
