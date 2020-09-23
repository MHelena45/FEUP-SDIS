package project.protocols;

import project.message.*;
import project.peer.Peer;
import project.store.FileManager;
import project.store.Store;

public class DeleteProtocol {

    public static void send_delete(double version, int sender_id, String file_id){
        DeleteMessage deleteMessage = new DeleteMessage( version, sender_id, file_id);
        Runnable task = () -> process_delete_message(deleteMessage);
        new Thread(task).start();

    }

    public static void process_delete_message(DeleteMessage deleteMessage){
        Peer.MC.send_message(deleteMessage.convert_message());

    }


    public static void receive_delete(DeleteMessage deleteMessage){
        System.out.println("Receive Delete of : ");
        System.out.println(deleteMessage.getFile_id());
        System.out.println("------------------");

        String file_id = deleteMessage.getFile_id();

        //delete all files and records in stored
        FileManager.delete_file_folder(file_id);

    }
}
