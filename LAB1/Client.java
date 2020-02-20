import java.io.IOException;
import java.net.*;

public class Client {

    static final int  timeout = 1000;
    public static void main(String[] args) throws IOException {
        if (args.length >= 4 ) {
            System.out.println("Usage:java Client <host> <port> <oper> <opnd>*");
            return;
        }

        if( !( args[2] == "register" || args[2] ==  "lookup" || args[2] == "REGISTER" || args[2] ==  "LOOKUP")) {
            System.out.println("<oper> must be register ou lookup");
            return;
        }

        // send request
        DatagramSocket socket = new DatagramSocket();
        String message = args[2] + " " + args[3]; // oper and opnd are prepare to be send
        byte[] sbuf= message.getBytes();
        InetAddress address= InetAddress.getByName(args[0]);
        Integer port = Integer.valueOf(args[1]);
        DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, address, port);
        socket.setSoTimeout(timeout);
        socket.send(packet);

        // get response
        byte[] rbuf= new byte[sbuf.length];
        packet = new DatagramPacket(rbuf, rbuf.length);
        socket.receive(packet);
        String received= new String(packet.getData());

        // display response
        System.out.println("Client: " + args[2] + " " + args[3] + ":" + received);

        socket.close();
    }
}
