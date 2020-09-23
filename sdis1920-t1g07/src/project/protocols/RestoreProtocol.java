package project.protocols;

import project.chunk.Chunk;
import project.message.BaseMessage;
import project.message.ChunkMessage;
import project.message.GetChunkMessage;
import project.message.PutChunkMessage;
import project.peer.Peer;
import project.store.FileManager;
import project.store.FilesListing;
import project.store.Store;

import java.util.ArrayList;

public class RestoreProtocol {

    public static void send_getchunk(double version, Integer sender_id,  String file_id, Integer chunk_no){
        GetChunkMessage getChunkMessage = new GetChunkMessage(version, sender_id, file_id, chunk_no);

        Runnable task = () -> process_get_chunk(getChunkMessage);
        new Thread(task).start();
    }

    public static void process_get_chunk(GetChunkMessage getChunkMessage){

        String chunk_id = getChunkMessage.getFile_id() + "_" + getChunkMessage.get_chunk_no();
        Peer.MC.send_message(getChunkMessage.convert_message());

    }

    /**
     * a peer that has a copy of the specified chunk shall send it in the body of a CHUNK message via the MDR channel
     * @param getChunkMessage message received
     */
    public static void receive_getchunk(GetChunkMessage getChunkMessage){
        String file_id = getChunkMessage.getFile_id();
        Integer chunk_number = getChunkMessage.get_chunk_no();
        Chunk chunk = Store.getInstance().retrieveChunk( file_id, chunk_number);
        send_chunk(getChunkMessage.getVersion(), Peer.id, file_id, chunk_number, chunk.content);
        //send chunk
    }

    public static void send_chunk(double version, Integer sender_id,  String file_id, Integer chunk_no, byte[] chunk_data){
        ChunkMessage chunkMessage = new ChunkMessage(version, sender_id, file_id, chunk_no, chunk_data);

        Runnable task = () -> process_chunk(chunkMessage);
        new Thread(task).start();

    }

    public static void process_chunk(ChunkMessage chunkMessage){
        Peer.MDR.send_message(chunkMessage.convert_message());

    }

    public static void receive_chunk(ChunkMessage chunkMessage){
        String file_id = chunkMessage.getFile_id();
        String file_name = FilesListing.get_files_Listing().get_file_name(file_id);


        String chunk_id = file_id + "_" + 0;

        if(Store.getInstance().check_backup_chunks_occurrences(chunk_id) != -1) {
            System.out.println(file_name);
            FileManager.write_chunk_to_restored_file(file_name, chunkMessage.getChunk(), chunkMessage.get_chunk_no());
        }
    }
}
