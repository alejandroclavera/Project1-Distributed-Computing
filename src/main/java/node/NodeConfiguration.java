package node;

import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class NodeConfiguration {
    public static String contentDirectory = "contents";
    public static int numBytesChunk = 10000000; // Default 1MB
    public static int numMaxDownloadChunksThreads = 1;
    public static int numMaxUploadThreads = 1;

    public static void saveConfiguration() throws IOException {
        JSONObject configurationJSON = new JSONObject();

        // Add the configurations attributes to a JSON object
        configurationJSON.put("contentDirectory", contentDirectory);
        configurationJSON.put("numBytesChunk", numBytesChunk);
        configurationJSON.put("numMaxDownloadChunksThreads", numMaxDownloadChunksThreads);
        configurationJSON.put("numMaxUploadThreads", numMaxUploadThreads);

        // Write the jsonObject to a file
        FileWriter jsonFile = new FileWriter("configurations/config.json");
        jsonFile.write(configurationJSON.toJSONString());
        jsonFile.flush();
        jsonFile.close();
    }
}
