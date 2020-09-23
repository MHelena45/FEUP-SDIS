package project.peer;

import java.io.File;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;


import project.Macros;

import project.channel.*;
import project.chunk.ChunkFactory;
import project.message.InvalidMessageException;
import project.protocols.BackupProtocol;
import project.protocols.DeleteProtocol;
import project.protocols.RestoreProtocol;
import project.store.FileManager;
import project.store.FilesListing;
import project.store.Store;

public class Peer implements RemoteInterface {
    private static final int RegistryPort = 1099;
    public static double version;
    public static int id;

    private static String service_access_point;

    //Addresses, ports and InetAdress of each channel
    public static MulticastControlChannel MC;
    public static MulticastDataBackupChannel MDB;
    public static MulticastDataRecoveryChannel MDR;

    public Peer(String MC_address, int MC_port, String MDB_address, int MDB_port, String MDR_address, int MDR_port)  {
        MC = new MulticastControlChannel(MC_address, MC_port);
        MDB = new MulticastDataBackupChannel(MDB_address, MDB_port);
        MDR = new MulticastDataRecoveryChannel(MDR_address, MDR_port);

        id = UUID.randomUUID().hashCode();
    }

    //class methods
    public static void main(String[] args){
        if(args.length != 8){
            System.out.println("Usage: [package]Peer <protocol_version> <service_access_point> " +
                    "<MC_address> <MC_port> <MDB_address> <MDB_port> <MDR_address> <MDR_port>");
            return;
        }

        try{
            version = Double.parseDouble(args[0]);
            //fiquei confusa, nao e suposto ser o peer a definir a sua versao?
            if( version != Macros.VERSION) {
                System.out.println("Not default version");
            }

            //since we are using RMI transport protocol, then the access_point is <remote_object_name>
            service_access_point = args[1];

            Peer object_peer = new Peer(args[2], Integer.parseInt(args[3]), args[4], Integer.parseInt(args[5]), args[6], Integer.parseInt(args[7]));
            RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(object_peer, 0);

            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(RegistryPort);
            }catch (RemoteException e){
                registry = LocateRegistry.getRegistry(RegistryPort);
            }

            registry.rebind(service_access_point, stub);

            new Thread(MC).start();
            new Thread(MDB).start();
            new Thread(MDR).start();

            System.out.println("Peer ready");

        } catch (Exception e) {
            System.err.println("Peer exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public int backup(String file_path, int replication_degree) throws InvalidMessageException {
        if(replication_degree <= 0 || replication_degree > 9)
            throw new InvalidMessageException("Replication degree is invalid");

        System.out.println("Backup file: "+ file_path);

        File file = new File(file_path);

        ChunkFactory chunkFactory = new ChunkFactory(file, replication_degree);

        String file_id = FileManager.createFileId(file);

        Integer number_of_chunks = (int) Math.ceil((float) file.length() / Macros.CHUNK_MAX_SIZE );

        FilesListing.get_files_Listing().add_file(file.getName(), file_id, number_of_chunks);

        BackupProtocol.send_putchunk(Peer.version, Peer.id, replication_degree, file_id, chunkFactory.get_chunks());

        return 0;
    }


    /**
     *
     * @param file_path
     * The client shall specify the file to restore by its pathname.
     */
    @Override
    public int restore(String file_path) {

        final String file_name = new File(file_path).getName();

        final String file_id = FilesListing.get_files_Listing().get_file_id(file_name);
        if(file_id == null) {
            System.err.println("A file with that name wasn't found, cannot restore a file that was't been backup by this peer");
            return -1;
        }

        FileManager.create_empty_file_for_restoring( file_name );

        Integer number_of_chunks = FilesListing.get_files_Listing().get_number_of_chunks(file_name);
        //Restore all chunks
        for(int i = 0; i < number_of_chunks; i++) {
            RestoreProtocol.send_getchunk(version, Peer.id, file_id, i);
        }

        return 0;
    }

    /**
     * The client shall specify the file to delete by its pathname.
     * @param file_path of the file that is going to be deleted
     */
    @Override
    public int delete(String file_path)  {

        //a peer should remove from its backing store all chunks belonging to the specified file.
        final String file_name = new File(file_path).getName();

        //gets the file_id from the entry with key file_name form allFiles
        final String file_id = FilesListing.get_files_Listing().get_file_id(file_name) ;

        if (file_id == null) {
            System.err.println("File name was't find, cannot delete file that wasn't been backup");
            System.exit(-1);
        }

        //sends message REMOVE to all peers
        DeleteProtocol.send_delete(version, id, file_id);

        //remove file of own records and files
        Store.getInstance().remove_Backup_chunks_occurrences(file_id);
        FileManager.delete_file_folder(file_id);

        // Remove entry with the file_name and correspond file_id from allFiles
        FilesListing.get_files_Listing().delete_file_records(file_name);



        return 0;
    }

    /**
     *
     * @param max_disk_space
     * The client shall specify the maximum disk space in KBytes (1KByte = 1000 bytes) that can be used for storing chunks.
     * It must be possible to specify a value of 0, thus reclaiming all disk space previously allocated to the service.
     */
    @Override
    public int manage(int max_disk_space) {
        if(max_disk_space < 0) {
            System.err.println("Invalid maximum disk space");
            System.exit(-1);
        }

        Store.getInstance().set_space_allow(max_disk_space);

        return 0;
    }

    /**
     * This operation allows to observe the service state.
     */
    @Override
    public String retrieve_state() {
        return "retrieve state successful";
    }
}