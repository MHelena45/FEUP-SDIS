package project.peer;

import project.message.InvalidMessageException;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote{
    int backup(String file_path, int replication_degree) throws RemoteException, InvalidMessageException;
    int restore(String file_path) throws RemoteException;
    int delete(String file_path) throws RemoteException;
    int manage(int  max_disk_space) throws RemoteException;
    String retrieve_state() throws RemoteException;
}