package project;

public class Macros {
    public final static byte CR = 0xD;
    public final static byte LF = 0xA;
    public static final Double VERSION = 1.0;

    public static final int CHUNK_MAX_SIZE = 64000; //in bytes

    // Given that each chunk is 64 KByte, this limits the size of the files to backup to 64 GByte.
    public static final int FILE_MAX_SIZE = 64000000;
    public static final int MAX_NUMBER_CHUNKS = 1000000;
    public static final int TTL = 1;
    public static final int MAX_MESSAGE_SIZE = 64200;
}
