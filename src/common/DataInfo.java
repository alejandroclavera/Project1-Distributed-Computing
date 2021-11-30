package common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataInfo implements Serializable {
    public final String hash;
    public List<String> titles;
    public final int size;
    public final HashMap<String, String> metadata;
    public List<ConnectionNode> providers;

    public DataInfo(String hash, HashMap<String, String> metadata) {
       this(hash, 0, metadata);
    }

    public DataInfo(String hash, int size, HashMap<String, String> metadata) {
        this.hash = hash;
        this.titles = new ArrayList<>();
        this.metadata = metadata;
        this.size = size;
        this.providers = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "{" +
                "hash='" + hash.substring(0, 15)  + "..." +'\'' +
                ", title='" + titles + '\'' +
                ", metadata=" + metadata +
                '}' + '\n';
    }
}
