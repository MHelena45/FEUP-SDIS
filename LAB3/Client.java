import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;

public class Client {
    private static String host_name, remote_object_name;
    private static RequestPacket requestPacket = new RequestPacket();
    
    public static void main(String[] args) throws IOException {
        if (args.length != 4 && args.length != 5) {
            System.out.println("Usage:java Client <host_name> <remote_object_name> <oper> <opnd>*");
            return;
        } else {
            //parse args
            host_name = args[0];
            remote_object_name = args[1];
            requestPacket.operation = args[2];

            //check if oper is REGISTER OU LOOKUP, as are the only valid options
            if (requestPacket.operation.equalsIgnoreCase("REGISTER")) {
                if (args.length != 5) {
                    System.out.println("Usage: java client <host_name> <remote_object_name> register <DNS name> <IP address>");
                    return;
                }

                requestPacket.DNS = args[3];
                requestPacket.IP_address = args[4];

            } else if (requestPacket.operation.equalsIgnoreCase("LOOKUP")) {
                if (args.length != 4) {
                    System.out.println("Usage: java client <host_name> <remote_object_name> lookup <DNS name>");
                    return;
                }
                requestPacket.DNS = args[3];

            } else {
                System.out.println("Usage: java client <host_name> <remote_object_name> ( REGISTER | LOOKUP ) <opnd> * ");
                return;
            }
        }

        try {
            run();
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
            return;
        }
    }

    public static void run() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(host_name);
        RemoteInterface stub = (RemoteInterface) registry.lookup(remote_object_name);
        if( requestPacket.operation.equalsIgnoreCase( "LOOKUP")) {
            //lookup operation of Remote Interface
            String received = stub.lookup(requestPacket.DNS);
            // display response         <oper> <opnd>*:: <out>
            System.out.println("Client: " + requestPacket.operation + " " + requestPacket.DNS   + ":" + received);
        } else {
            //register operation of Remote Interface
            int received = stub.register(requestPacket.DNS, requestPacket.IP_address);
            System.out.println("Client: " + requestPacket.operation + " " + requestPacket.DNS  + " " + requestPacket.IP_address + ":" + received);
        }

    }
}