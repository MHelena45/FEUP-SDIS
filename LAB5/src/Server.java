import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server{
    private static HashMap<String,String> DNStable = new HashMap<String, String>();

    private static int port;
    private static SSLServerSocket srvSo = null;
    private static Socket echoSo = null;

    static void usage(){
        System.out.println("Usage: java EchoServer <port_no>");
    }

    public static void main(String[] args) throws IOException {

        if(args.length != 1){
            usage();
            return;
        }

        try {
            port = Integer.parseInt(args[0]);
            if( port <1024 || port>= 1<<16){
                usage();
                System.out.println("\t <port_no> must be a 16 bit integer");
                return;
            }
            srvSo = new ServerSocket(port);
        } catch ( NumberFormatException e){
            usage();
            return;
        } catch (IOException e){
            System.out.println("Failed to listen on port " + port);
        }

        srvSo.setSoTimeout(50000);

        try {
            echoSo = srvSo.accept();
        } catch (IOException e){
            System.out.println("Failed to accept on port " + port);
        }

        System.out.println("Server initiated with port " + port);

        getRequest();

    }

    private static void getRequest() throws IOException {
        PrintWriter out = null;
        BufferedReader in = null;

        in = new BufferedReader( new InputStreamReader(
                echoSo.getInputStream()
        ));

        out = new PrintWriter(echoSo.getOutputStream());

        String line = null;
        String reply = null;

        System.out.println("Server: waiting for client request");

        line = in.readLine();

        System.out.println("Server: received \n\t" + line);

        reply = parseRequest(line);

        out.println(reply);
        out.flush();

        System.out.println("Server: echoed \n\t" + reply);

        echoSo.shutdownOutput();

        while (in.readLine() != null);

        System.out.println("Server: Client shutdown output: closing socket");
        echoSo.close();

        srvSo.close();

    }

    private static String parseRequest(String line){

        String[] words = line.trim().split("\\s");
        RequestPacket request = new RequestPacket();
        String reply;

        if(words.length == 3 && (words[0].equalsIgnoreCase("REGISTER") )){
            request.operation = "register";
            request.DNS = words[1].trim();
            request.IP_address = words[2].trim();

            System.out.println("Server: REGISTER " + request.DNS + " " + request.IP_address);

            if(!check_table(request)){
                DNStable.put(request.DNS, request.IP_address);
                reply = Integer.toString(DNStable.size());
            }
            else reply = "-1";
        }
        else if(words[0].equalsIgnoreCase("lookup")){
            request.operation = "lookup";
            request.DNS = words[1].trim();

            System.out.println("Server: LOOKUP " + request.DNS);

            if(check_table(request))
                reply = request.IP_address;
            else reply = "NOT_FOUND";
        } else reply = "-1";

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