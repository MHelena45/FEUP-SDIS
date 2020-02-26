import java.io.IOException;
import java.net.*;

public class Client {

    static final int timeout = 2000; // in ms
    public static void main(String[] args) throws IOException {
        String message;
        boolean register = false;
       
        System.out.println(args.length);
        System.out.println(args[2]);

        if (args.length == 4 && (args[2].equalsIgnoreCase("lookup") )) {
            message = args[2] + " " + args[3]; // oper and opnd are prepare to be send
        }
        else if(args.length == 5 && (args[2].equalsIgnoreCase("register") )) {
            message = args[2] + " " + args[3] + " " + args[4]; // oper and opnd are prepare to be send
            register = true;
        }
        else {
            System.out.println("Usage:java Client <host> <port> <oper> <opnd>*");
            return;
        }

        // send request
        DatagramSocket socket = new DatagramSocket();

        byte[] sbuf= message.getBytes();
        InetAddress address= InetAddress.getByName(args[0]);
        Integer port = Integer.valueOf(args[1]);
        DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, address, port);

        socket.send(packet);

        socket.setSoTimeout(timeout);
        
        // get response
        byte[] rbuf= new byte[sbuf.length];
        packet = new DatagramPacket(rbuf, rbuf.length);
        socket.receive(packet);
        String received= new String(packet.getData());

        // display response
        if(register){
            if(received.equals("-1"))
                received = "ERROR";

            System.out.println("Client: " + args[2] + " " + args[3] + " " + args[4] + " : " + received);
        }
        else System.out.println("Client: " + args[2] + " " + args[3] + " : " + received);

        socket.close();
    }
}
