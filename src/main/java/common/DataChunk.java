package common;

import java.io.Serializable;

public class DataChunk implements Serializable, Comparable {
    public final String hash;
    public final String name;
    public final long chunkNumber;
    public final int size;
    public byte chunkBytes[];
    public ConnectionNode senderNode;

    public DataChunk(String hash, String name, int chunkNumber, int size, byte[] chunkBytes, ConnectionNode senderNode) {
        this.hash = hash;
        this.name = name;
        this.chunkNumber = chunkNumber;
        this.chunkBytes = chunkBytes;
        this.size = size;
        this.senderNode = senderNode;
    }

    public DataChunk(String hash, String name, int chunkNumber, byte[] chunkBytes, ConnectionNode senderNode) {
        this(hash, name, chunkNumber, chunkBytes.length, chunkBytes, senderNode);
    }

    @Override
    public int compareTo(Object o) {
        DataChunk other = (DataChunk) o;
        if (chunkNumber == other.chunkNumber)
            return 0;
        return (chunkNumber < other.chunkNumber)? -1 : 1;
    }
}
