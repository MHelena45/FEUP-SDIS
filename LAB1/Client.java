import java.io.IOException;
import java.net.*;


public class Client {
    //kind of Macro
    static final String separator = "|";
    static final int  timeout = 1000;
    static int numberOfTimeOuts = 3;

    // Identifiers of the server (IP Address and Port)
    private static String serverIPAddressStr;
    private static int serverPort;
    private static RequestPacket requestPacket = new RequestPacket();

    public static void main(String[] args) throws IOException {
        if (args.length != 4 && args.length == 5) {
            System.out.println("Usage:java Client <host> <port> <oper> <opnd>*");
            return;
        } else {
            //parse args
            serverIPAddressStr = args[0];
            serverPort = Integer.parseInt(args[1]);
            requestPacket.operation = args[2];
            
            //check if oper is REGISTER OU LOOKUP, as are the only valid options
            if (requestPacket.operation.equalsIgnoreCase("REGISTER")) {
                if (args.length != 5) {
                    System.out.println("Usage: java client <host <port> register <DNS name> <IP address>");
                    return;
                }

                requestPacket.DNS = args[3];
                requestPacket.IP_address = args[4];

            } else if (requestPacket.operation.equalsIgnoreCase("LOOKUP")) {
                if (args.length != 4) {
                    System.out.println("Usage: java client <host> <port> lookup <DNS name>");
                    return;
                }
                requestPacket.DNS = args[3];

            } else {
                System.out.println("Usage: java client <host_addr> <port> ( REGISTER | LOOKUP ) <opnd> * ");
                return;
            }
        }

        while (numberOfTimeOuts > 0) {
            try {
                run();
            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                numberOfTimeOuts--;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public static void run() throws IOException {

        // send request
        DatagramSocket socket= new DatagramSocket();
        // build message
        String message = requestPacket.operation.toString();
        if( requestPacket.operation.equalsIgnoreCase( "LOOKUP")) {
            message += separator + requestPacket.DNS; // args separated by "|"
        } else {
            message += separator + requestPacket.DNS + separator + requestPacket.IP_address;// oper and opnds are prepare to be send
        }
        byte[] sbuf = message.getBytes();
        InetAddress address= InetAddress.getByName(serverIPAddressStr);
        Integer port = Integer.valueOf(serverPort);
        DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, address, port);
        socket.setSoTimeout(timeout);
        socket.send(packet);

        // get response
        byte[] rbuf= new byte[256];
        packet = new DatagramPacket(rbuf, rbuf.length);
        socket.receive(packet);
        String received= new String(packet.getData());

        // display response
        if(requestPacket.operation.equalsIgnoreCase( "LOOKUP")) {
            System.out.println("Client: " + requestPacket.operation + " " + requestPacket.DNS   + ":" + received);
        } else {
            System.out.println("Client: " + requestPacket.operation + " " + requestPacket.DNS  + " " + requestPacket.IP_address + ":" + received);
        }

        socket.close();
    }
}