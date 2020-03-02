import java.util.*;
import java.io.IOException;
import java.net.*;

public class Server{
    private static HashMap<String,String> DNStable = new HashMap<String, String>();

    private static String serviceAddress;
    private static int servicePort;
    private static String mcast_addr;
    private static int mcast_port;
    static final long  delay = 1000;
    static final int TTL = 1;

    public static void main(String[] args) throws IOException{
        if(args.length != 3){
            System.out.println("Usage: java Server <srvc_port> <mcast_addr> <mcast_port>");
            return;
        }

        initialize(args);
    }

    private static void initialize(String[] args){
        serviceAddress = Utils.getIPv4();
        servicePort = Integer.parseInt(args[0]);
        mcast_addr = args[1];
        mcast_port = Integer.parseInt(args[2]);

        InetAddress multicastAddress = InetAddress.getByName(mcast_addr);

        MulticastSocket multicastSocket = new MulticastSocket();
        multicastSocket.setTimeToLive(TTL);

        Timer timer = new Timer();

        TimerTask task = new TimerTask(){
            @Override
            public void run() {
                String msg = serviceAddress + " : " + Integer.toString(servicePort);

                DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.getBytes().length, multicastAddress, mcast_port);
                multicastSocket.send(packet);

                System.out.println("multicast: " + multicastAddress + " "
						+ mcast_port + ": " + serviceAddress + " "
						+ servicePort);
            }
        };

        timer.schedule(task, delay);

        DatagramSocket serverSocket = new DatagramSocket(servicePort);   

        System.out.println("Server initiated with servicePort " + servicePort);   

        getRequests(serverSocket, multicastSocket);

        serverSocket.close();
        multicastSocket.close();
    }

    private static void getRequests(DatagramSocket serverSocket, MulticastSocket multicastSocket)throws IOException {
        while(true){
            byte[] buffer = new byte[256];

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            serverSocket.receive(packet);

            System.out.println("Received packet from client");

            String reply = parseRequest(packet);

            buffer = reply.getBytes();

            InetAddress address = packet.getAddress();
            int servicePort = packet.getPort();
            packet = new DatagramPacket(buffer, buffer.length, address, servicePort);

            serverSocket.send(packet);
        }
    }

    private static String parseRequest(DatagramPacket packet){
        String data = new String(packet.getData(), 0, packet.getData().length);
        String[] words = data.split("\\s");
        RequestPacket request = new RequestPacket();
        String reply;

        if(words.length == 3 && (words[0].equalsIgnoreCase("REGISTER") )){
            request.operation = "register";
            request.DNS = words[1].trim();
            request.IP_address = words[2].trim();

            if(!check_table(request)){
                DNStable.put(request.DNS, request.IP_address);
                reply = Integer.toString(DNStable.size());
            }
            else reply = "-1";

            System.out.println("REGISTER " + request.DNS + " " + request.IP_address + " :: " + reply);
        }
        else if(words[0].equalsIgnoreCase("lookup")){
            request.operation = "lookup";
            request.DNS = words[1].trim();

            System.out.println("LOOKUP " + request.DNS  + " :: " + reply);

            if(check_table(request))
                reply = request.IP_address;
            else reply = "NOT_FOUND";
        } else reply = "-1";

        return reply;
    }

    private static boolean check_table(RequestPacket request){
        System.out.println(DNStable.containsKey(request.DNS));
        if(DNStable.containsKey(request.DNS)){
            System.out.println("entrou");
            request.IP_address = DNStable.get(request.DNS);
            return true;
        }
        else return false;
    }
}
