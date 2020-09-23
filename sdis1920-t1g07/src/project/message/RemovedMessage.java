package project.message;

import project.Macros;

public class RemovedMessage extends BaseMessage {
    private final Integer chunk_no;

    public RemovedMessage(double version, int sender_id, String file_id, Integer chunk_no) {
        super(version, Message_type.REMOVED, sender_id, file_id);

        this.chunk_no = chunk_no;
    }

    public Integer get_chunk_number() {
        return chunk_no;
    }

    @Override
    public String get_header(){
        return super.get_header() + " " + chunk_no;
    }
}
