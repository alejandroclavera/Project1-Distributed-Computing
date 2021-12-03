package node.managers.files;

import common.DataInfo;

import java.io.FileInputStream;
import java.util.List;

public interface FileManager {
    void recognizeContents();
    List<DataInfo> getContentsList();
    FileInputStream getContent(String hash);
    void addNewContent(String name, byte[] allBytes);
    public void addNewContent(String name, List<byte[]> bytes);
}
