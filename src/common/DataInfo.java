package common;

import java.io.Serializable;
import java.util.HashMap;

public class DataInfo implements Serializable {
    public final String hash;
    public final String title;
    public final HashMap<String, String> metadata;

    public DataInfo(String hash, String title, HashMap<String, String> metadata) {
        this.hash = hash;
        this.title = title;
        this.metadata = metadata;
    }
}
