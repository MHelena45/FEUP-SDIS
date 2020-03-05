package com.lab3;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote{
    String lookup(String DNS) throws RemoteException;
    int register(String DNS, String IP) throws RemoteException;
}