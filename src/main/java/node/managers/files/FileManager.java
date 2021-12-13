package node.managers.files;

import common.DataChunk;
import common.DataInfo;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;

public interface FileManager {
    void recognizeContents();
    List<DataInfo> getContentsList();
    FileInputStream getContent(String hash);
    void validate(DataInfo dataInfo);
    void addMetadata(String hash, HashMap<String, String> metadata);
    void addNewContent(String name, byte[] allBytes);
    void addNewContent(String name, List<DataChunk> dataChunks);
    void writeInTemporalFile(String hash, List<DataChunk> dataChunks);
    void temporalToFile(String hash, String fileName);
}
