package project.protocols;

import project.chunk.Chunk;
import project.message.PutChunkMessage;
import project.message.StoredMessage;
import project.peer.Peer;
import project.store.Store;

import java.util.ArrayList;

public class BackupProtocol {
    public static void send_putchunk(double version, int sender_id, int replication_degree, String file_id, ArrayList<Chunk> chunks){
        //sends putchunks
        for (Chunk chunk : chunks) {
            PutChunkMessage putchunk = new PutChunkMessage(version, sender_id, file_id, chunk.chunk_no, replication_degree, chunk.content);

            String chunk_id = file_id + "_" + chunk.chunk_no;

            Store.getInstance().new_Backup_chunk(chunk_id, replication_degree);

            Runnable task = () -> process_putchunk(putchunk, chunk_id);

            new Thread(task).start();
        }
    }

    public static void process_putchunk(PutChunkMessage putchunk, String chunk_id){
        int tries = 1;

        System.out.println("BODY SENT IN PUTCHUNK (" + putchunk.getChunk().length + ")");
        byte[] message = putchunk.convert_message();

        while(tries <= 5){
            Peer.MDB.send_message(message);
            System.out.println("PUTCHUNK SENT (" + message.length + ") of try " + tries);

            try{
                Thread.sleep(1000*tries);

                if(Store.getInstance().check_backup_chunks_occurrences(chunk_id) >= putchunk.getReplicationDegree()){
                    break;
                }

                tries++;

            } catch (InterruptedException e) {
                e.getStackTrace();
                break;
            }
        }
        if(tries > 5){
            System.out.println("Putchunk failed desired replication degree: <file_id: " + putchunk.getFile_id() + "> <chunk_no: " + putchunk.getChunkNo() + ">");
        }
    }

    public static void receive_putchunk(PutChunkMessage putchunk){
        System.out.println("------------------");
        System.out.println("Received putchunk ");
        if(Store.getInstance().storeChunk(putchunk.getFile_id(), putchunk.getChunkNo(), putchunk.getChunk(), putchunk.getReplicationDegree())){
            StoredMessage stored = new StoredMessage(putchunk.getVersion(), Peer.id, putchunk.getFile_id(), putchunk.getChunkNo());

            Peer.MC.send_message(stored.convert_message());
        }

        System.out.println("------------------");
    }

    public static void receive_stored(StoredMessage message){
        System.out.println("------------------");
        System.out.println("Received stored ");
        System.out.println("------------------");
        String chunk_id = message.getFile_id() + "_" + message.getChunk_no();
        Store.getInstance().add_Backup_chunks_occurrences(chunk_id, message.getSender_id());
    }
}
