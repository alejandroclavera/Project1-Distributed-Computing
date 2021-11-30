package common;

import java.io.Serializable;
import java.util.HashMap;

public class DataInfo implements Serializable {
    public final String hash;
    public final String title;
    public final int size;
    public final HashMap<String, String> metadata;

    public DataInfo(String hash, String title, HashMap<String, String> metadata) {
        this.hash = hash;
        this.title = title;
        this.metadata = metadata;
        size = 0;
    }

    public DataInfo(String hash, String title, int size, HashMap<String, String> metadata) {
        this.hash = hash;
        this.title = title;
        this.metadata = metadata;
        this.size = size;
    }

    @Override
    public String toString() {
        return "{" +
                "hash='" + hash.substring(0, 15)  + "..." +'\'' +
                ", title='" + title + '\'' +
                ", metadata=" + metadata +
                '}' + '\n';
    }
}
