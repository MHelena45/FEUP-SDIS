package project.message;

import project.Macros;

public class StoredMessage extends BaseMessage {
    private final int chunk_no;

    public StoredMessage(double version, int sender_id, String file_id, int chunk_no) {
        super(version, Message_type.STORED, sender_id, file_id);

        this.chunk_no = chunk_no;
    }

    @Override
    public String get_header(){
        return super.get_header() + " " + chunk_no;
    }

    public int getChunk_no(){
        return chunk_no;
    }
}
