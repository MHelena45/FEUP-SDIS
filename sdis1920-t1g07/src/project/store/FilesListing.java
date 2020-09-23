package project.store;

import project.peer.Peer;
import project.protocols.BackupProtocol;
import project.protocols.DeleteProtocol;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * class that keeps record of the conversion of file_name to file_id
 */
public class FilesListing {

    private static FilesListing filesListing = new FilesListing();
    private static ConcurrentHashMap<String, String> files = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Integer> files_chunks = new ConcurrentHashMap<>();

    //singleton
    private FilesListing() {
        get_files_disk_info();
    }

    /**
     * get all files listed
     * @return an instance FilesListing
     */
    public static FilesListing get_files_Listing() {
        return filesListing;
    }

    /**
     *  checks the files ConcurrentHashMap for a file
     * @param file_name encoded name of the file
     * @return file_id if file name exists
     */
    public String get_file_id(String file_name) {
        return files.get(file_name);
    }

    public String get_file_name(String file_id) {
        if(files.containsValue(file_id)) {
            for( String key : files.keySet()){
                if(files.get(key).equals(file_id))
                    return key;

            }
        }
        return null;
    }

    public Integer get_number_of_chunks(String file_name) {
        return files_chunks.get(file_name);
    }

    public void add_file(String file_name, String file_id, Integer number_of_chunks) {

        //put returns the previous value associated with key, or null if there was no mapping for key
        String previous_file_id = files.put(file_name, file_id);
        Integer previous_number_of_chunks = files_chunks.put(file_name, number_of_chunks);

        if (previous_file_id != null) {
            System.out.println("This file_name already exists, updating the content.");

            System.out.println("Deleting " + previous_number_of_chunks + " chunks from the out of date file");

            //deletes file from network storage
            DeleteProtocol.send_delete(Peer.version, Peer.id, file_id );

            //deletes own files with chunks of the file in the 3 folders ( files, stored, restored)
            FileManager.delete_file_folder(previous_file_id);

            //old file is ours so unregister chunks of the file
            Store.getInstance().remove_stored_chunks(file_id);

        }
        set_files_disk_info();
    }

    public void delete_file_records(String file_name) {
        files.remove(file_name);
        files_chunks.remove(file_name);
        set_files_disk_info();
    }


    /**
     * changes the content of the file to contain this current object
     * @return true if successful and false otherwise
     */
    public boolean set_files_disk_info() {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(Store.getInstance().get_files_info_directory_path()));
            objectOutputStream.writeObject(this);
        } catch (Exception e) {
            e.getStackTrace();
            System.out.println("Couldn't get files info to the disk");
            return false;
        }
        return true;
    }

    /**
     *
     * @return true if successful and false otherwise
     */
    public static boolean get_files_disk_info() {

        //if file is empty there is nothing to have in the concurrentMap
        if (new File(Store.getInstance().get_files_info_directory_path()).length() == 0) {
            files = new ConcurrentHashMap<>();
            System.out.println("There isn't previous file info.");
            return true;
        }

        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(Store.getInstance().get_files_info_directory_path()));
            filesListing = (FilesListing) objectInputStream.readObject();
            System.out.println("Files Concurrent map is updated according to disk info");

        } catch (Exception ignored) {
            System.out.println("Couldn't put files info into the disk");
            return false;
        }
        return true;
    }
}
