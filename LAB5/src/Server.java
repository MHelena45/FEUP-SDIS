import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;

public class Server{
    private static HashMap<String,String> DNStable = new HashMap<String, String>();

    private static int port;

    private static SSLSocket sslSocket = null;
    private static SSLServerSocket s = null;
    private static SSLServerSocketFactory ssf = null;

    static void usage(){
        System.out.println("Usage: java SSLServer <port_no> <cypher-suite>*");
    }

    public static void main(String[] args) throws IOException {

        if(args.length != 1){
            usage();
            return;
        }

        ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        try {
            port = Integer.parseInt(args[0]);
            if( port <1024 || port>= 1<<16){
                usage();
                System.out.println("\t <port_no> must be a 16 bit integer");
                return;
            }
            s = (SSLServerSocket) ssf.createServerSocket(port);
           // s.setNeedClientAuth(true);
        }
        catch( IOException e) {
            System.out.println("Server - Failed to create SSLServerSocket");
            e.getMessage();
            return;
        } catch ( NumberFormatException e){
            usage();
            return;
        }

        s.setSoTimeout(50000);

        try {
            //s = s.accept();
            // the method SSLServerSocket.accept() returns a SSLSocket, that is used by the server side to transfer data
            sslSocket = (SSLSocket) s.accept();
        } catch (IOException e){
            System.out.println("Failed to accept on port " + port);
        }

        System.out.println("Server initiated with port " + port);

        if(args.length > 1){
            String[] cyphers = Arrays.copyOfRange(args, 1, args.length);
            for(String cypher : cyphers) {
                System.out.println(cypher);
            }

            try {
                sslSocket.setEnabledCipherSuites(cyphers);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return;
            }

        } else {
            System.out.println("Using custom Cipher Suites");
        }

        getRequest();

    }

    private static void getRequest() throws IOException {
        PrintWriter out = null;
        BufferedReader in = null;

        in = new BufferedReader( new InputStreamReader(
                sslSocket.getInputStream()
        ));

        out = new PrintWriter(sslSocket.getOutputStream());

        String line = null;
        String reply = null;

        System.out.println("Server: waiting for client request");

        line = in.readLine();

        System.out.println("Server: received \n\t" + line);

        reply = parseRequest(line);

        out.println(reply);
        out.flush();

        System.out.println("Server: echoed \n\t" + reply);

        sslSocket.close();

        while (in.readLine() != null);

        System.out.println("Server: Client shutdown output: closing socket");

        s.close();
        sslSocket.shutdownOutput();

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