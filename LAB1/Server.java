import java.util.Hashtable;
import java.io.IOException;
import java.net.*;

public class Server{
    private static Hashtable<String,String> DNStable;

    private static int port;

    public static void main(String[] args) throws IOException{
        if(args.length != 1){
            System.out.println("Missing argument port");
            return;
        }

        DNStable = new Hashtable<String, String>();

        port = Integer.parseInt(args[0]);

        getRequests();
    }

    private static void getRequests() throws IOException {
        DatagramSocket socket = new DatagramSocket(port);

        System.out.println("Server initiated with port " + port);

        while(true){
            byte[] buffer = new byte[256];

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            socket.receive(packet);

            System.out.println("Received packet from client");

            String reply = parseRequest(packet);

            buffer = reply.getBytes();

            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(buffer, buffer.length, address, port);

            socket.send(packet);
        }
    }

    private static String parseRequest(DatagramPacket packet){
        String data = new String(packet.getData(), 0, packet.getData().length);
        String[] words = data.split("\\s");
        RequestPacket request = new RequestPacket();
        String reply;

        if(words.length == 3 && (words[0].equalsIgnoreCase("REGISTER") )){
            request.operation = "register";
            request.DNS = words[1];
            request.IP_address = words[2];

            System.out.println("Server: REGISTER " + request.DNS + " " + request.IP_address);

            if(check_table(request)){
                DNStable.put(request.DNS, request.IP_address);
                reply = Integer.toString(DNStable.size());
            }
            else reply = "-1";
        }
        else{
            request.operation = "lookup";
            request.DNS = words[1];

            System.out.println("Server: LOOKUP " + request.DNS);

            if(check_table(request))
                reply = request.IP_address;
            else reply = "NOT_FOUND";
        }

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
