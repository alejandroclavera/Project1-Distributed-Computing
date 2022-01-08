package common;

import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class DataInfo implements Serializable {
    public final String hash;
    public List<String> titles;
    public final long size;
    public final HashMap<String, String> metadata;
    public List<ConnectionNode> providers;
    private boolean isNew;
    private boolean isUpdated;
    private boolean isDeleted;

    public DataInfo(String hash, HashMap<String, String> metadata) {
       this(hash, 0, metadata);
    }

    public DataInfo(String hash, long size, HashMap<String, String> metadata) {
        this.hash = hash;
        this.titles = new ArrayList<>();
        if (metadata == null)
            this.metadata = new HashMap<>();
        else
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataInfo dataInfo = (DataInfo) o;
        return size == dataInfo.size &&
                Objects.equals(hash, dataInfo.hash);
    }


    public JSONObject toJson() {
        JSONObject dataInfoJson = new JSONObject();

        dataInfoJson.put("hash", hash);
        dataInfoJson.put("title", titles);
        dataInfoJson.put("size", size);
        dataInfoJson.put("metadata", metadata);

        return dataInfoJson;
    }
}
