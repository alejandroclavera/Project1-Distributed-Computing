package node.managers.files;

import common.DataChunk;
import common.DataInfo;

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

    public FileSystemManger(String contentsDirectoryPath) {
        this.contentsDirectoryPath = contentsDirectoryPath;
        this.contentsMap = new HashMap<>();
        recognizeContents();
    }

    private String getHash(String fileName) throws IOException, NoSuchAlgorithmException {
        FileInputStream fileInputStream = new FileInputStream(new File(fileName));
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");

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
        // Check if the content directory exists
        if (!contentsDirectory.exists())
            contentsDirectory.mkdir();

        // recognize the contents of the directory
        for (String contentName : contentsDirectory.list()) {
            try {
                Path path = Paths.get(contentsDirectoryPath, contentName);
                String hash = getHash(path.toString());
                long size = Files.size(Paths.get(contentsDirectoryPath, contentName));
                DataInfo dataInfo = new DataInfo(hash, size, null);
                dataInfo.titles.add(contentName);
                contentsMap.put(hash, dataInfo);
            } catch (Exception e) {
                System.out.println("Error to get information of the content: " + contentName);
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

}
