package node.managers.files;

import common.DataChunk;
import common.DataInfo;

import java.io.FileInputStream;
import java.util.List;

public interface FileManager {
    void recognizeContents();
    List<DataInfo> getContentsList();
    FileInputStream getContent(String hash);
    void addNewContent(String name, byte[] allBytes);
    void addNewContent(String name, List<DataChunk> dataChunks);
}
