package project.store;

import project.Macros;
import project.chunk.Chunk;
import project.peer.Peer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

public class Store {
    private static Store store = new Store();

    //state of others chunks
    private Hashtable<String, Pair<Integer,ArrayList<Integer>>> stored_chunks = new Hashtable<>();
    private Hashtable<String, String> restored_files = new Hashtable<>();
    //state of our files - key file_id + chunk and value wanted_replication degree and list of peers
    private ConcurrentHashMap<String, Pair<Integer,ArrayList<Integer>>> backup_chunks_occurrences = new ConcurrentHashMap<>();

    private static String peer_directory_path;
    private static String files_directory_path;
    private static String files_info_directory_path;
    private static String store_directory_path;
    private static String store_info_directory_path;
    private static String restored_directory_path;

    private int space_with_storage = 0; //in bytes
    private Integer space_allow = -1; //Initial there isn't restrictions of space


    /**
     * creates the four needed directory
     */
    private Store() {
        initializeStore();
    }

    /**
     * creates the four needed directory
     */
    public void initializeStore(){
        //setting the directory name
        peer_directory_path = Peer.id + "_directory/";
        files_directory_path = peer_directory_path + "files/";
        files_info_directory_path = peer_directory_path + "files.txt";
        store_directory_path = peer_directory_path + "stored/";
        store_info_directory_path = peer_directory_path + "stored.txt";
        restored_directory_path = peer_directory_path + "restored/";

        FileManager.create_directory(peer_directory_path);
        FileManager.create_directory(files_directory_path);
        FileManager.create_empty_file(files_info_directory_path);
        FileManager.create_directory(store_directory_path);
        //if exists return true but doesn't creates a new file
        FileManager.create_empty_file(store_info_directory_path);
        FileManager.create_directory(restored_directory_path);
    }

    public static Store getInstance(){
        return store;
    }

    public void remove_stored_chunks(String file_id ){
        stored_chunks.remove(file_id);
    }

    public void set_space_allow(Integer space_allow) {
        this.space_allow = space_allow;

        //TODO delete necessary chunk to have that space
      /*   while(space_allow < space_with_storage) {
            String file_id;
            String chunk_number;
            //remove chunks updates space with storage
            removeChunk(file_id, chunk_number);

        } */

    }

    public boolean checkStoredChunk(String file_id, int chunk_no){
        if(stored_chunks.containsKey(file_id)) {
            return stored_chunks.get(file_id).second.contains(chunk_no);
        }
        else return false;
    }

    private void add_stored_chunk(String file_id, int chunk_number, int replicationDegree) {
        if(stored_chunks.containsKey(file_id)) {
            Pair<Integer, ArrayList<Integer>> pair = stored_chunks.get(file_id);
            pair.second.add(chunk_number);

            stored_chunks.replace(file_id, pair);
        }
        else {
            ArrayList<Integer> chunks_stored = new ArrayList<>();
            chunks_stored.add(chunk_number);
            Pair pair = new Pair<>(replicationDegree, chunks_stored);
            stored_chunks.put(file_id, pair);
        }
    }

    /**
     * get chunk from stored directory
     * @param file_id encoded
     * @param chunk_no number of the chunk we want to retrieve
     * @return wanted chunk data
     */
    public Chunk retrieveChunk(String file_id, int chunk_no){
        if(checkStoredChunk(file_id, chunk_no)) {
            Chunk chunk;
            //get the chunk information from the chunks saved file
            final String chunk_path = store_directory_path + "/" + file_id + "/" + chunk_no;
            File file = new File(chunk_path);
            int chunk_size = (int) file.length();
            byte[] chunk_data = new byte[chunk_size];
            try (FileInputStream fileInputStream = new FileInputStream(chunk_path)) {
                if(fileInputStream.read(chunk_data) < 0)
                    return null;
                chunk = new Chunk(chunk_no, chunk_data, chunk_size);
                return chunk;
            } catch (IOException e) {
                System.err.println("Couldn't get chunk "+ chunk_no + " of file " + file_id);
                e.printStackTrace();
                return null;
            }

        }

        // Does not have the chunk
        return null;

    }

    /**
     * Given the chunk_body ( the data ), stores it in a file
     * @param file_id encoded
     * @param chunk_number number of the chunk
     * @param chunk_body data
     * @param replicationDegree wanted replication degree
     * @return true if successful or false otherwise
     */
    public boolean storeChunk(String file_id, int chunk_number, byte[] chunk_body, int replicationDegree) {

        //check if the chunk already exists
        if (checkStoredChunk(file_id, chunk_number)) {
            System.out.println("A chunk with number " + chunk_number + " and file_id " + file_id + " already exists.");
            return true;
        }

        //check if there is enough storage
        if (!this.hasSpace(chunk_body.length)) {
            System.out.println("A chunk with number " + chunk_number + " and file_id " + file_id + " can't be store because there isn't space left.");
            return false;
        }

        String chunk_directory =  store_directory_path + "/" + file_id + "/";

        // Idempotent Method
        FileManager.create_directory(chunk_directory);
        String chunk_path = chunk_directory + chunk_number;

        try {
            //FileOutputStream.write() method automatically create a new file and write content to it.
            FileOutputStream file = new FileOutputStream(chunk_path);
            file.write(chunk_body);
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        add_stored_chunk(file_id, chunk_number, replicationDegree);

        //update the current space used for storage
       this.AddToSpace_with_storage(chunk_body.length);
        return true;
    }

    /**
     * used to check if there is space to store a new chunk
     * @param space_wanted space we pretend to use
     * @return true if space exist or false otherwise
     */
    public boolean hasSpace(Integer space_wanted) {
        //don't exit restrictions
        if(this.space_allow == -1)
            return true;
        return (this.space_allow >= this.space_with_storage + space_wanted);
    }

    /**
     * used when a new chunk is store ( by backup )
     * @param space_with_storage storage space added
     */
    public void AddToSpace_with_storage(int space_with_storage) {
        this.space_with_storage += space_with_storage;
    }

    /**
     *
     * @return spaced used for storage
     */
    public Integer getSpace_with_storage() {
        return space_with_storage;
    }

    /**
     * sets the spaces used for storage
     * @param space_with_storage the amount of space in bytes used for storage
     */
    public void setSpace_with_storage(int space_with_storage) {
        this.space_with_storage = space_with_storage;
    }

    /**
     * used when a chunk is deleted
     * @param space_with_storage the amount of space in bytes used for storage
     */
    public void remove_space_with_storage(int space_with_storage) {
        this.space_with_storage -= space_with_storage;
    }



    /**
     * Deletes a chunk
     * @param file_id encoded
     * @param chunk_number number of the chunk
     * @return true if chunk was removed and false if it was
     */
    public boolean removeChunk(String file_id, int chunk_number) {

        System.out.println("Deleting chunk "+ chunk_number + " with id:" + file_id);

        //check if the chunk exists
        if (retrieveChunk(file_id, chunk_number) == null) {
            System.out.println("A chunk with number " + chunk_number + " and file_id " + file_id + " doesn't exists.");
            return true;
        }

        //chunk will be in backup_directory/file_id/chunk_no
        String chunk_dir =  store_directory_path + "/" + file_id + "/"+ chunk_number;

        File chunk_file = new File(chunk_dir);

        if( !chunk_file.exists() ){
            System.out.println("File doesn't exists in the correct path ");
            return false;
        }

        if (chunk_file.delete()) {
            this.remove_space_with_storage((int) chunk_file.length());
            return true;
        }

        return false;
    }

    public void new_Backup_chunk(String chunk_id, int replication_degree) {
        if(this.backup_chunks_occurrences.containsKey(chunk_id)){
            Pair<Integer, ArrayList<Integer>> pair = this.backup_chunks_occurrences.get(chunk_id);

            pair.first = replication_degree;

            this.backup_chunks_occurrences.replace(chunk_id, pair);
        }
        else this.backup_chunks_occurrences.put(chunk_id, new Pair<Integer, ArrayList<Integer>>(replication_degree, new ArrayList<>()));
    }

    public void add_Backup_chunks_occurrences(String chunk_id, int peer_id) {
        if(this.backup_chunks_occurrences.containsKey(chunk_id)){
            Pair<Integer, ArrayList<Integer>> pair = this.backup_chunks_occurrences.get(chunk_id);

            if(pair.second.contains(peer_id)){
                System.out.println("Already received a positive feedback from this peer");
                return;
            }

            pair.second.add(peer_id);
            this.backup_chunks_occurrences.replace(chunk_id, pair);
        }
    }

    public int check_backup_chunks_occurrences(String chunk_id) {
        if(this.backup_chunks_occurrences.get(chunk_id) != null)
            return this.backup_chunks_occurrences.get(chunk_id).second.size();

        return -1;
    }

    public void remove_Backup_chunk_occurrence(String chunk_id, Integer peer_id) {
        Pair<Integer,ArrayList<Integer>> value = this.backup_chunks_occurrences.get(chunk_id);

        if(value != null ){
            ArrayList<Integer> peersList = value.second;
            peersList.remove(peer_id);
            Pair<Integer, ArrayList<Integer>> pair = new Pair<>(value.first, peersList);
            this.backup_chunks_occurrences.replace(chunk_id, pair);
        }

    }

    public void remove_Backup_chunks_occurrences(String chunk_id) {
        this.backup_chunks_occurrences.remove(chunk_id);
    }

    public String get_files_info_directory_path() {
        return files_info_directory_path;
    }

    public String get_restored_directory_path() {
        return restored_directory_path;
    }

    public String get_store_directory_path() {
        return store_directory_path;
    }
}


