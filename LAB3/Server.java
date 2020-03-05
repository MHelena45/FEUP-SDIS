package com.lab3;

import java.util.HashMap;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;

public class Server implements RemoteInterface{
    private static HashMap<String,String> DNStable = new HashMap<String, String>();

    public static void main(String[] args) {
        if(args.length != 1){
            System.out.println("Usage: java Server <remote_object_name>");
            return;
        }

        try {
            Server obj = new Server();
            RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(args[0], stub);

            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public String lookup(String DNS) throws RemoteException{
        RequestPacket request = new RequestPacket();
        String reply;
        request.DNS = DNS;
        if(check_table(request))
            reply = request.IP_address;
        else reply = "NOT_FOUND";

        return reply;
    }

    public int register(String DNS, String IP) throws RemoteException{
        RequestPacket request = new RequestPacket();
        int reply;

        request.DNS = DNS;
        request.IP_address = IP;
        if(!check_table(request)){
            DNStable.put(DNS, IP);
            reply = DNStable.size();
        }
        else reply = -1;

        return reply;
    }

    private static boolean check_table(RequestPacket request){
        if(DNStable.containsKey(request.DNS)){
            request.IP_address = DNStable.get(request.DNS);
            return true;
        }
        else return false;
    }
}