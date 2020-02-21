import java.utils.Arrays;
import java.net.*;

class Server{
    class RequestPacket{
        String operation;
        String DNS;
        String IP;
    }

    class Pair{
        String DNS;
        String IP;
    }

    Pair[] DNStable;
    
    final int port;

    public static void main(String[] args){
        port = Integer.parseInt(args[0]);

        getRequests();
    }

    private getRequests(){
        DatagramSocket socket = DatagramSocket(port);
        DatagramPacket packet;
        String reply;
    
        while(true){
            socket.receive(packet);

            parseRequest(packet, reply);
            
            packet = DatagramPacket(reply.getBytes(), reply.getByte().getLength());

            socket.send(packet);
        }
    }

    private parseRequest(DatagramPacket packet, String reply){
        String data = String(packet.getData(), packet.getData().getLength());
        String[] words = data.split("\\s");
        RequestPacket request;

        if(words.length == 3 && (words[0] == "REGISTER" || words[0] == "register")){
            request.operation = "register";
            request.DNS = words[1];
            request.IP = words[2];

            System.out.println("Server: REGISTER " + request.DNS + " " + request.IP);

            if(check_table(request))
                reply = "-1";
            else{
                add_to_table(request);
                reply = DNStable.length.toString();
            }
        }
        else if(words.length == 2 &&  (words[0] == "LOOKUP" || words[0] == "lookup")){
            request.operation = "lookup";
            request.DNS = words[1];

            System.out.println("Server: LOOKUP " + request.DNS);

            if(check_table(request))
                reply = request.IP;
            else reply = "NOT_FOUND";
        }
        else reply= "NOT EXISTING OPERATION";
    }

    private add_to_table(RequestPacket request){
        Pair[] new_DNStable = Pair[DNStable.length + 1];
        new_DNStable.copyOf(DNStable, new_DNStable.length);

        Pair new_pair = Pair(request.DNS, request.IP);

        new_DNStable[new_DNStable.length - 1] = new_pair;
        DNStable = new_DNStable;
    }

    private boolean check_table(RequestPacket request){
        for(int i = 0; i < DNStable.length; i++){
            if(DNStable[i].DNS == request.DNS){
                request.IP = DNStable[i].IP;
                return true;
            }
        }
        return false;
    }
}