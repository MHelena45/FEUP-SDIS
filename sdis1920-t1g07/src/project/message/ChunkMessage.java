package project.message;

public class ChunkMessage extends BaseMessage{
    private final Integer chunk_no;

    public ChunkMessage(double version, int sender_id, String file_id, Integer chunk_no, byte[] chunk) {
        super(version, Message_type.CHUNK, sender_id, file_id);

        this.chunk_no = chunk_no;
        this.chunk = chunk;
    }

    public Integer get_chunk_no() {
        return chunk_no;
    }

    @Override
    public String get_header(){
        return super.get_header() + " " + chunk_no;
    }
}
