package project.message;

import project.Macros;

public class GetChunkMessage extends BaseMessage {
    private final Integer chunk_no;

    public GetChunkMessage(double version, int sender_id, String file_id, Integer chunk_no) {
        super(version, Message_type.GETCHUNK, sender_id, file_id);

        this.chunk_no = chunk_no;
    }

    public Integer get_chunk_no() {
        return chunk_no;
    }

    @Override
    public String get_header(){
        return super.get_header() + " " + chunk_no;
    }
}