import java.io.IOException;
import java.net.*;

public class Client {

    static final String separator = "|";
    static final int timeout = 2000; // in ms

    private static String serviceAddressStr;
    private static int servicePort;

    //Args given
    private static String multicastIPAddressStr; //IP address of the multicast group used by the server to advertise its service
    private static int multicastPort; //port number of the multicast group used by the server to advertise its service
    private static String oper; //  "register" or "lookup", depending on the operation to invoke
    private static String plate, owner; // list of operands of the specified operation

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: java client <mcast_addr> <mcast_port> <oper> <opnd> * ");
            return;

        } else {
            //parse args
            multicastIPAddressStr = args[0];
            multicastPort = Integer.parseInt(args[1]);
            oper = args[2];

            //check if oper is REGISTER OU LOOKUP, as are the only valid options
            if ( oper.equalsIgnoreCase("REGISTER")) {
                if (args.length != 5) {
                    System.out.println("Usage: java client <mcast_addr> <mcast_port> register <plate number> <owner name>");
                    return;
                }

                plate = args[3];
                owner = args[4];

            } else if (oper.equalsIgnoreCase( "LOOKUP")) {
                if (args.length != 4) {
                    System.out.println("Usage: java client <mcast_addr> <mcast_port> lookup <plate number>");
                    return;
                }
                plate = args[3];

            } else {
                System.out.println("Usage: java client <mcast_addr> <mcast_port> ( REGISTER | LOOKUP ) <opnd> * ");
                return;
            }
        }
        try {
            run();
        } catch (SocketTimeoutException s) {
            System.out.println("Socket timed out!");
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public static void run() throws IOException {

        InetAddress group = InetAddress.getByName(multicastIPAddressStr);
        MulticastSocket multicastSocket = new MulticastSocket(multicastPort);

        //joinGroup(InetAddress mcastaddr) - Joins a multicast group
        multicastSocket.joinGroup(group);

        byte[] buf = new byte[256];
        DatagramPacket multicastPacket = new DatagramPacket(buf, buf.length);
        multicastSocket.receive(multicastPacket);

        String msg = new String(multicastPacket.getData());
        String[] parts = msg.split(separator);
        serviceAddressStr = parts[0];
        servicePort = Integer.parseInt(parts[1]);

        // multicast: <mcast_addr> <mcast_port>: <srvc_addr> <srvc_port>
        System.out.println("multicast: " + multicastIPAddressStr + " " + multicastPort + ": " + serviceAddressStr + " " + servicePort);

        // build message
        String request = oper.toString();
        if(oper.equalsIgnoreCase( "LOOKUP")) {
            request += separator + plate; // args separated by "|"
        } else {
            request += separator + plate + separator + owner;
        }

        // send request
        DatagramSocket socket = new DatagramSocket();

        buf = request.getBytes();
        InetAddress address = InetAddress.getByName(serviceAddressStr);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, servicePort);

        socket.send(packet);
        socket.setSoTimeout(timeout);

        // receive response
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        String response = new String(packet.getData(), 0, packet.getLength());

        // print messages on the terminal describing the actions it executes - <oper> <opnd> *:: <out>
        System.out.println(request + " :: " + response);

        // close socket
        socket.close();

        // leaveGroup(InetAddress mcastaddr) - Leave a multicast group.
        multicastSocket.leaveGroup(group);
        multicastSocket.close();
    }

}