package node.managers.files;

import common.DataChunk;
import common.DataInfo;
import node.Node;
import node.NodeConfiguration;
import node.logs.LogSystem;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileSystemManger implements FileManager{
    private String contentsDirectoryPath;
    private HashMap<String, DataInfo> contentsMap;
    private List<DataInfo> contentToValidate;

    public FileSystemManger(String contentsDirectoryPath) {
        this.contentsDirectoryPath = contentsDirectoryPath;
        this.contentsMap = new HashMap<>();
        this.contentToValidate = new ArrayList<>();
        recognizeContents();
        startValidator();
    }

    private String getHash(String fileName) throws IOException, NoSuchAlgorithmException {
        FileInputStream fileInputStream = new FileInputStream(new File(fileName));
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");

        // Create byte array to read data in chunks
        int bytesCount = 0;
        byte[] byteArray = new byte[1024];
        // read the data from file and update that data in the message digest
        while ((bytesCount = fileInputStream.read(byteArray)) != -1)
            messageDigest.update(byteArray, 0, bytesCount);
        fileInputStream.close();

        // Transform the bytes array to String hex hash
        StringBuilder sb = new StringBuilder();
        // loop through the bytes array
        for (int i = 0; i < byteArray.length; i++)
            sb.append(Integer.toString((byteArray[i] & 0xff) + 0x100, 16).substring(1));
        // finally we return the complete hash
        return sb.toString();
    }

    @Override
    public void recognizeContents() {
        File contentsDirectory = new File(contentsDirectoryPath);
        File contentsJSON = new File(Paths.get(contentsDirectoryPath, "contents.json").toString());
        JSONObject contentsJsonObject = null;
        List<String> recognizeHashes = new ArrayList<>();
        boolean newContent = false;
        List<String> nameList;
        List<String> hashList;
        JSONArray dataInfoList;

        // Check if the content directory exists
        if (!contentsDirectory.exists())
            contentsDirectory.mkdir();

        contentsJsonObject = getContentsJson();

        // Get all fields of the json object
        nameList = (List<String>) contentsJsonObject.get("names");
        hashList = (List<String>) contentsJsonObject.get("hashes");
        dataInfoList = (JSONArray) contentsJsonObject.get("dataInfo");

        // recognize the contents of the directory
        DataInfo dataInfo = null;
        String hash = null;
        for (String contentName : contentsDirectory.list()) {
            // Ignore the contents.json
            if (contentName.equals("contents.json") || contentName.endsWith(".tmp"))
                continue;
            try {
                Path path = Paths.get(contentsDirectoryPath, contentName);
                // Get the json contain data info of the file get data info
                if (nameList.contains(contentName)) {
                    int index = nameList.indexOf(contentName);
                    // Get the information of the dataInfo stored in the json file
                    JSONObject dataInfoJson = (JSONObject) dataInfoList.get(index);
                    long size = (long) dataInfoJson.get("size");
                    HashMap<String, String> metadata = (HashMap<String, String>) dataInfoJson.get("metadata");
                    hash = hashList.get(index);
                    dataInfo = new DataInfo(hash, size, metadata);
                    dataInfo.titles.add(contentName);
                } else {
                    hash = getHash(path.toString());
                    long size = 0;
                    HashMap<String, String> metadata = null;
                    // check if the is renamed
                    if (hashList.contains(hash)) {
                        // Get the information of the json object because the file it is renamed
                        int index = hashList.indexOf(hash);
                        JSONObject dataInfoJson = (JSONObject) dataInfoList.get(index);
                        size = (long) dataInfoJson.get("size");
                        nameList.remove(index);
                        nameList.add(index, contentName);
                        metadata = (HashMap<String, String>) dataInfoJson.get("metadata");
                    } else {
                        // Generate new data info if the content is new
                        size = Files.size(Paths.get(contentsDirectoryPath, contentName));
                        nameList.add(contentName);
                        hashList.add(hash);
                    }

                    // Create new data with the content information
                    dataInfo = new DataInfo(hash, size, metadata);
                    dataInfo.titles.add(contentName);
                    // Add info to the json info
                    dataInfoList.add(dataInfo.toJson());
                    newContent = true;
                }
                contentsMap.put(hash, dataInfo);
                recognizeHashes.add(hash);
            } catch (Exception e) {
                System.out.println("Error to get information of the content: " + contentName);
            }
        }

        // remove old hashes
        newContent = newContent || removeOldHashes(recognizeHashes, contentsJsonObject);
        // If have new content or updates, write the file
        if (newContent) {
            try {
                writeContentsInJson(contentsJsonObject);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<DataInfo> getContentsList() {
        return new ArrayList<>(contentsMap.values());
    }

    @Override
    public FileInputStream getContent(String hash) {
        FileInputStream fileInputStream;
        if (!hash.contains(hash))
            return null;
        DataInfo contentInfo = contentsMap.get(hash);
        String contentPath = Paths.get(contentsDirectoryPath, contentInfo.titles.get(0)).toString();
        try {
            fileInputStream = new FileInputStream(contentPath);
        } catch (FileNotFoundException e) {
            return null;
        }
        return fileInputStream;
    }

    @Override
    public void addNewContent(String name, byte[] allBytes) {
        String contentPath = Paths.get(contentsDirectoryPath, name).toString();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(contentPath);
            fileOutputStream.write(allBytes);
            fileOutputStream.close();
            // Add to the hashmap (TODO)
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void addNewContent(String name, DataChunk dataChunk) {
        byte allBytes[] = dataChunk.chunkBytes;
        String contentPath = Paths.get(contentsDirectoryPath, name).toString();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(contentPath);
            fileOutputStream.write(allBytes);
            fileOutputStream.close();
            // Add to the hashmap (TODO)
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addNewContent(String name, List<DataChunk> dataChunks) {
        String contentPath = Paths.get(contentsDirectoryPath, name).toString();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(contentPath);
            for (DataChunk dataChunk:  dataChunks) {
                fileOutputStream.write(dataChunk.chunkBytes, 0, dataChunk.size);
            }
            fileOutputStream.close();
            // Add to the hashmap (TODO)
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void writeContentsInJson(JSONObject jsonObject) throws IOException {
        FileWriter jsonFile = new FileWriter(Paths.get(contentsDirectoryPath, "contents.json").toString());
        jsonFile.write(jsonObject.toJSONString());
        jsonFile.flush();
        jsonFile.close();
    }

    @Override
    public void writeInTemporalFile(String hash, List<DataChunk> dataChunks) {
        String tmpFilePath = Paths.get(contentsDirectoryPath, hash.substring(0,50)).toString() + ".tmp";
        for (DataChunk dataChunk : dataChunks) {
            try {
                RandomAccessFile randomAccessFile = new RandomAccessFile(new File(tmpFilePath), "rws");
                randomAccessFile.seek(dataChunk.chunkNumber * NodeConfiguration.numBytesChunk); // change
                randomAccessFile.write(dataChunk.chunkBytes, 0, dataChunk.size);
                randomAccessFile.close();
            } catch (FileNotFoundException e) {
                LogSystem.logErrorMessage("Can't write in tmp file, not found");
            } catch (IOException e) {
                LogSystem.logErrorMessage("Can't write in tmp file (IOExcepetion)");
            }
        }
    }

    @Override
    public void temporalToFile(String hash, String fileName) {
        String tmpFilePath = Paths.get(contentsDirectoryPath, hash.substring(0,50)).toString() + ".tmp";
        String contentFilePath = Paths.get(contentsDirectoryPath, fileName).toString();
        File tmpFile = new File(tmpFilePath);
        File contentFile = new File(contentFilePath);
        tmpFile.renameTo(contentFile);
        try {
            long size = Files.size(Paths.get(contentFilePath));
            DataInfo dataInfo = new DataInfo(hash, size, null);
            dataInfo.titles.add(fileName);
            contentsMap.put(hash, dataInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void validate(DataInfo dataInfo) {
        synchronized (contentToValidate) {
            // Add the content to the list to validate it
            synchronized (contentToValidate) {
                contentToValidate.add(dataInfo);
                // Notify to validator
                contentToValidate.notify();
            }
        }
    }

    @Override
    public void addMetadata(String hash, HashMap<String, String> metadata) {
        DataInfo contentDataInfo = contentsMap.get(hash);
        contentDataInfo.metadata.putAll(metadata);
        JSONObject jsonObject = getContentsJson();
        JSONArray jsonArray = new JSONArray();
        for (DataInfo dataInfo : contentsMap.values()) {
            jsonArray.add(dataInfo.toJson());
        }
        jsonObject.put("dataInfo", jsonArray);
        try {
            writeContentsInJson(jsonObject);
        } catch (IOException e) {
            LogSystem.logErrorMessage("can't add metadata to the content");
        }
    }

    private void startValidator() {
        // Start the thread that validate the download contents
        new Thread(() ->{
            validator();
        }).start();
    }

    private void validator() {
        while (Node.isRunning) {
            List<DataInfo> contentsValidated = new ArrayList<>();
            List<DataInfo> toValidate;
            synchronized (contentToValidate) {
                // Copy the list to avoid errors
                toValidate = new ArrayList<>(contentToValidate);
            }
            // Try the validate each content of the list
            for (DataInfo dataInfo : toValidate) {
                String path = Paths.get(contentsDirectoryPath, dataInfo.titles.get(0)).toString();
                File contentFile = new File(path);
                // Validate the content if the file exists
                if (contentFile.exists()) {
                    try {
                        // Calculate the hash of the content
                        String hash = getHash(path);
                        // Check if the hash of the content is the same of the expected
                        if (hash.equals(dataInfo.hash)) {
                            // Add the content to the contents registry
                            contentsMap.put(dataInfo.hash, dataInfo);
                            LogSystem.logInfoMessage("content :" + dataInfo.titles.get(0) + " validated");
                        } else {
                            // If the hash aren't equals -> security problem
                            contentFile.delete();
                            LogSystem.logErrorMessage("error of security of the content");
                        }
                        contentsValidated.add(dataInfo);
                    } catch (IOException e) {
                        LogSystem.logErrorMessage("Can't validate the content (IOException)");
                    } catch (NoSuchAlgorithmException e) {
                        LogSystem.logErrorMessage("Can't validate the content (Bad hash algorithm)");
                    }
                }
            }
            synchronized (contentToValidate) {
                // Remove the data info of the contents validated
                contentToValidate.removeAll(contentsValidated);
                if (contentsValidated.size() == 0) {
                    // if the thread don't have contents to validate wait.
                    try {
                        contentToValidate.wait(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private JSONObject readContentsJson() throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        FileReader jsonFile = new FileReader(Paths.get(contentsDirectoryPath, "contents.json").toString());
        return (JSONObject) jsonParser.parse(jsonFile);
    }

    private JSONObject getContentsJson() {
        File contentsDirectory = new File(contentsDirectoryPath);
        File contentsJSON = new File(Paths.get(contentsDirectoryPath, "contents.json").toString());
        JSONObject contentsJsonObject = null;
        // Generate content json object empty if no exists the json file
        if (!contentsJSON.exists()) {
            contentsJsonObject = new JSONObject();
            contentsJsonObject.put("names", new ArrayList<>());
            contentsJsonObject.put("hashes", new ArrayList<>());
            contentsJsonObject.put("dataInfo", new JSONArray());
        } else {
            try {
                contentsJsonObject = readContentsJson();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return contentsJsonObject;
    }

    private boolean removeOldHashes(List<String> recognizedHashes, JSONObject jsonObject) {
        List<String> hashListJson = (List<String>) jsonObject.get("hashes");
        List<String> namesListJson = (List<String>) jsonObject.get("names");
        JSONArray dataInfos = (JSONArray) jsonObject.get("dataInfo");
        boolean anyHashRemoved = false;
        for (String hash : new ArrayList<>(hashListJson)) {
            // If no contain the hash remove the hash
            if (!recognizedHashes.contains(hash)) {
                int index = hashListJson.indexOf(hash);
                hashListJson.remove(index);
                namesListJson.remove(index);
                dataInfos.remove(index);
                anyHashRemoved = true;
            }
        }
        return anyHashRemoved;
    }
}
