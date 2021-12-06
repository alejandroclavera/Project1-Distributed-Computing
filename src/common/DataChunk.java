package common;

import java.io.Serializable;

public class DataChunk implements Serializable {
    public final String hash;
    public final String name;
    public final long chunkNumber;
    public final int size;
    public byte chunkBytes[];

    public DataChunk(String hash, String name, int chunkNumber, int size, byte[] chunkBytes) {
        this.hash = hash;
        this.name = name;
        this.chunkNumber = chunkNumber;
        this.chunkBytes = chunkBytes;
        this.size = size;
    }

    public DataChunk(String hash, String name, int chunkNumber, byte[] chunkBytes) {
        this(hash, name, chunkNumber, chunkBytes.length, chunkBytes);
    }
}
