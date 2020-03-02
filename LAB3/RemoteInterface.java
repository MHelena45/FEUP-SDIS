package com.company.lab3;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote{
    public String lookup(String DNS) throws RemoteException;
    public int register(String DNS, String IP) throws RemoteException;
}