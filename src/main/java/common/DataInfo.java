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
    public List<ConnectionNode> providers;
    public String wsId = "";
    public HashMap<String, String> metadata;
    public boolean isNew = false;
    public boolean isUpdated = false;
    public boolean isDeleted = false;
    public boolean fileDeleted = false;
    public String owner = "";

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
                " wsID= " + wsId +
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
        dataInfoJson.put("isNew", isNew);
        dataInfoJson.put("isUpdated", isUpdated);
        dataInfoJson.put("isDeleted", isDeleted);
        dataInfoJson.put("fileDeleted", fileDeleted);
        dataInfoJson.put("owner", owner);
        dataInfoJson.put("wsId", wsId);
        return dataInfoJson;
    }

    public static DataInfo fromJSON(JSONObject dataInfoJson) {
        // Get dataInfo form JSON
        String hash = (String) dataInfoJson.get("hash");
        List<String> titles = (ArrayList)dataInfoJson.get("title");
        long size = (long) dataInfoJson.get("size");
        HashMap<String, String> metadata = (HashMap<String, String>) dataInfoJson.get("metadata");
        boolean isNew = (boolean) dataInfoJson.get("isNew");
        boolean isUpdated = (boolean) dataInfoJson.get("isUpdated");
        boolean isDeleted = (boolean) dataInfoJson.get("isDeleted");
        boolean fileDeleted = (boolean) dataInfoJson.get("fileDeleted");
        String owner = (String) dataInfoJson.get("owner");
        String wsId = (String) dataInfoJson.get("wsId");

        // Create dataInfo object
        DataInfo dataInfo = new DataInfo(hash, size, metadata);
        dataInfo.isNew = isNew;
        dataInfo.isUpdated = isUpdated;
        dataInfo.isDeleted = isDeleted;
        dataInfo.fileDeleted = fileDeleted;
        dataInfo.titles = titles;
        dataInfo.owner = owner;
        dataInfo.wsId = wsId;
        return dataInfo;
    }
}
