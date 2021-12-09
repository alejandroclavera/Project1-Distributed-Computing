package node;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class NodeConfiguration {
    private static String configPath = Paths.get("configurations", "config.json").toString();
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
        FileWriter jsonFile = new FileWriter(configPath);
        jsonFile.write(configurationJSON.toJSONString());
        jsonFile.flush();
        jsonFile.close();
    }

    public static void loadConfigurations() throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        // Parse the configuration json file
        JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(configPath));

        // Change the configuration attributes
        contentDirectory = (String) jsonObject.get("contentDirectory");
        numBytesChunk = (int)(long)jsonObject.get("numBytesChunk");
        numMaxDownloadChunksThreads = (int)(long)jsonObject.get("numMaxDownloadChunksThreads");
        numMaxUploadThreads = (int)(long)jsonObject.get("numMaxUploadThreads");
    }
}
