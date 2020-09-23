package project.store;

import project.Macros;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileManager {

    /**
     * Creates an empty file to start the restoring procedure or the backup
     * @return true if successful, and false otherwise
     */
    public static boolean create_empty_file_for_restoring(String file_name) {
        String restore_file_path = Store.getInstance().get_restored_directory_path() + file_name;
        return create_empty_file(restore_file_path);
    }

    /**
     * Creates an empty file to start the restoring procedure or the backup/info file
     * if exists return true but doesn't creates a new file
     * @param file_path path of the file that is being created
     */
    public static boolean create_empty_file(String file_path) {

        try {
            File file = new File(file_path);
            if(file.exists())
                return true;

            if(file.createNewFile()) {
                System.out.println("File: " + file + " was created");
                return true;
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println("Couldn't create an empty file to start restoring");
            return false;
        }
        return false;
    }

    /**
     * Idempotent Method that creates a directory given the path
     * @return success if directory was or is created and false if not
     */
    public static boolean create_directory(String directory_path) {

        File directory = new File(directory_path);
        if (!directory.exists()) {
            //mkdirs() returns true if created and false on failure or if exists
            if (directory.mkdir()) {
                return true;
            } else {
                System.out.println("Directory " + directory + " wasn't created!");
                return false;
            }
        }
        return true;
    }

    /**
     * encodes a file name with a sha-256 cryptographic hash function
     * @param file used to get name and lastModified date
     * @return file id
     */
    public static String createFileId(File file) {

        String file_name = file.getName();

        //encoded file name uses the file.lastModified() that ensures that a modified file has a different fileId
        String file_name_to_encode = file_name + file.lastModified();

        //identifier is obtained by applying SHA256, a cryptographic hash function, to some bit string.
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] hash = digest.digest(file_name_to_encode.getBytes(StandardCharsets.UTF_8));

        return String.valueOf(hash);
    }


    /**
     * This functions append the body of a chunk (file data) in the position desired ( calculated with chunk number)
     * Used for restoring a file with a certain given filename
     * @param file_name name of the file
     * @param chunk_data the array with the bytes to put
     * @param chunk_number number of the chunk
     * @return true if success and false otherwise
     */
    public static synchronized boolean write_chunk_to_restored_file(String file_name, byte[] chunk_data, int chunk_number) {

        String file_path = Store.getInstance().get_restored_directory_path() + "/" + file_name;

        //Random access file offers a seek feature that can go directly to a particular position
        try (RandomAccessFile file = new RandomAccessFile(file_path, "rw")) {
            file.seek(chunk_number * Macros.CHUNK_MAX_SIZE);
            file.write(chunk_data);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Couldn't append chunk number " + chunk_number + " to file " + file_name + ".");
            return false;
        }
    }

    /**
     * deletes folder with chunks of a file passed in the first argument
     * @param file_id encoded
     * @return true if successful, and false other wise
     */
    public static boolean delete_file_folder(String file_id) {
        File file_directory = new File(Store.getInstance().get_store_directory_path() + file_id);

        if(file_directory == null){
            return false;
        }

        if (file_directory.isFile()) {
            return file_directory.delete();
        }

        if (!file_directory.isDirectory()) {
            return false;
        }

        Store.getInstance().remove_stored_chunks(file_id);

        File[] folder_files = file_directory.listFiles();
        if (folder_files != null && folder_files.length > 0) {
            for (File f : folder_files) {
                if (!f.delete()) {
                    return false;
                }
            }
        }

        return file_directory.delete();
    }
}
