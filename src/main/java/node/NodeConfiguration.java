package node;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

public class NodeConfiguration {
    private static String configDirectoryPath = "configurations";
    private static String configPath = Paths.get(configDirectoryPath, "config.json").toString();
    public static String contentDirectory = "contents";
    public static int numBytesChunk = 1000000; // Default 1MB
    public static int chunkWindowSize = 25;
    public static int numMaxDownloadThreads = 2;
    public static int numMaxUploadThreads = 2;
    public static int windowsTimeout = 25;
    public static int maxTryWindow = 3;
    public static int connectionResponseTimeout = 15;
    public static int wsNodeID = -1;


    public static void saveConfiguration() throws IOException {
        JSONObject configurationJSON = new JSONObject();
        File configDirectory = new File(configDirectoryPath);
        File configFile = new File(configPath);

        // Create configuration directory if not exists create the directory
        if (!configDirectory.exists())
            configDirectory.mkdir();

        // Add the configurations attributes to a JSON object
        configurationJSON.put("contentDirectory", contentDirectory);
        configurationJSON.put("numBytesChunk", numBytesChunk);
        configurationJSON.put("chunkWindowSize", chunkWindowSize);
        configurationJSON.put("numMaxDownloadThreads", numMaxDownloadThreads);
        configurationJSON.put("numMaxUploadThreads", numMaxUploadThreads);
        configurationJSON.put("maxTryWindow", maxTryWindow);
        configurationJSON.put("windowsTimeout", windowsTimeout);
        configurationJSON.put("connectionResponseTimeout", connectionResponseTimeout);
        configurationJSON.put("wsNodeID", wsNodeID);


        // Write the jsonObject to a file
        FileWriter jsonFile = new FileWriter(configPath);
        jsonFile.write(configurationJSON.toJSONString());
        jsonFile.flush();
        jsonFile.close();
    }

    public static void loadConfigurations() throws IOException, ParseException {
        File configurationDirectory = new File(configPath);
        JSONParser jsonParser = new JSONParser();

        // Load configurations if the config file exists
        if (configurationDirectory.exists()) {
            // Parse the configuration json file
            JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(configPath));

            // Change the configuration attributes
            contentDirectory = (String) jsonObject.get("contentDirectory");
            numBytesChunk = (int)(long) jsonObject.get("numBytesChunk");
            chunkWindowSize = (int)(long) jsonObject.get("chunkWindowSize");
            numMaxDownloadThreads = (int)(long) jsonObject.get("numMaxDownloadThreads");
            numMaxUploadThreads = (int)(long) jsonObject.get("numMaxUploadThreads");
            maxTryWindow = (int)(long) jsonObject.get("maxTryWindow");
            windowsTimeout = (int)(long) jsonObject.get("windowsTimeout");
            connectionResponseTimeout = (int)(long) jsonObject.get("connectionResponseTimeout");
            wsNodeID = (int)(long) jsonObject.get("wsNodeID");
        }
    }

    public static String getParams() {
        return "contentDirectory: " + contentDirectory +"\n" +
                "numBytesChunk: " + numBytesChunk +"\n" +
                "chunkWindowSize: " + chunkWindowSize + "\n" +
                "numMaxDownloadThreads: " + numMaxDownloadThreads + "\n" +
                "numMaxUploadThreads: " + numMaxUploadThreads + "\n" +
                "maxTryWindow: " + maxTryWindow + "\n" +
                "windowsTimeout: " + windowsTimeout + "\n" +
                "connectionResponseTimeout: " + connectionResponseTimeout;
    }
}
