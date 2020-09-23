package project.message;

import project.InvalidFileException;
import project.Macros;

import java.util.Arrays;

/**
 * fields common to all messages
 */
public abstract class BaseMessage {
    //Header
    private final double version;
    private final Message_type message_type;
    private final int sender_id;
    private final String file_id;
    protected byte[] chunk;

    public BaseMessage(double version, Message_type message_type, int sender_id, String file_id) {
        this.version = version;
        this.message_type = message_type;
        this.sender_id = sender_id;
        this.file_id = file_id;
        this.chunk = null;
    }

    public String get_header(){
        return this.version + " " + this.message_type + " " + this.sender_id + " " + this.file_id;
    }

    public byte[] convert_message(){
        String header = get_header() + " " + ((char)Macros.CR) + ((char)Macros.LF) + ((char)Macros.CR) + ((char)Macros.LF);

        if(this.chunk == null)
            return header.getBytes();
        else{
            byte[] header_bytes = header.getBytes();
            byte[] message = new byte[this.chunk.length + header_bytes.length];
            System.arraycopy(header_bytes, 0, message, 0, header_bytes.length);
            System.arraycopy(this.chunk, 0, message, header_bytes.length, this.chunk.length);
            return message;
        }
    }


    public double getVersion() {
        return version;
    }

    public Message_type getMessage_type() {
        return message_type;
    }

    public int getSender_id() {
        return sender_id;
    }

    public String getFile_id() {
        return file_id;
    }

    public byte[] getChunk(){ return chunk; }
}