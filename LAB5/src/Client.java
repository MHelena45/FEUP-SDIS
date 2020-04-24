import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Arrays;

public class Client {
    //kind of Macro
    static final String separator = " ";
    static final int  timeout = 1000;
    static int numberOfTimeOuts = 3;
    private static RequestPacket requestPacket = new RequestPacket();

    private static SSLSocket sslSocket;
    private static SSLSocketFactory ssf = null;

    static void usage(){
        System.out.println("Usage: java Client  <host> <port>  <oper> <opnd>* <cypher-suite>*");
    }

    public static void main(String[] args) throws IOException {
        String host;
        int port = 0;

        if (args.length != 4 && args.length != 5) {
            usage();
            return;
        }

        ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();

        try {
            host = args[0];
            port = Integer.parseInt(args[1]);
            if( port <1024 || port>= 1 << 16){
                usage();
                System.out.println("\t <port_no> must be a 16 bit integer");
                return;
            }
            sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port);

        } catch (final NumberFormatException e) {
            usage();
            return;
        } catch ( final  IOException e){
            System.out.println("Failed to connect to localhost on port" + port);
            return;
        }

        Integer number_of_not_cypher_suite_args = 0;
        //check if oper is REGISTER OU LOOKUP, as are the only valid options
        if (requestPacket.operation.equalsIgnoreCase("REGISTER")) {
            if (args.length < 5) {
                System.out.println("Usage: java client <host> <port> register <DNS name> <IP address> <cypher-suite>*");
                return;
            }

            requestPacket.DNS = args[3];
            requestPacket.IP_address = args[4];
            number_of_not_cypher_suite_args = 5;

        } else if (requestPacket.operation.equalsIgnoreCase("LOOKUP")) {
            if (args.length < 4) {
                System.out.println("Usage: java client <host> <port> lookup <DNS name> <cypher-suite>*");
                return;
            }
            requestPacket.DNS = args[3];
            number_of_not_cypher_suite_args = 4;

        } else {
            System.out.println("Usage: java client <host_addr> <port> ( REGISTER | LOOKUP ) <opnd> * <cypher-suite>*");
            return;
        }


        if((args.length - number_of_not_cypher_suite_args) >= 1){
            String[] cyphers = Arrays.copyOfRange(args, number_of_not_cypher_suite_args, args.length);
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

          /*
        System.out.println("Client: Sleeping for 10 s");

        try {
            Thread.sleep(10000);
        } catch (Exception e){
            System.out.println("Client: timeout interrupted");
        } */

        while (numberOfTimeOuts > 0) {
            try {
                run();
                break;
            }  catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public static void run() throws IOException {

        // build message
        String message = requestPacket.operation.toString();
        if( requestPacket.operation.equalsIgnoreCase( "LOOKUP")) {
            message += separator + requestPacket.DNS; // args separated by "|"
        } else {
            message += separator + requestPacket.DNS + separator + requestPacket.IP_address;// oper and opnds are prepare to be send
        }

        PrintWriter out = null;
        BufferedReader in = null;

        in = new BufferedReader( new InputStreamReader(
                sslSocket.getInputStream()
        ));

        sslSocket.setSoTimeout(timeout);

        out = new PrintWriter(sslSocket.getOutputStream());

        out.println(message);
        out.flush();

        System.out.println("Client: sent \n\t" + message);

        String received = null;
        received = in.readLine();

        // display response
        if(requestPacket.operation.equalsIgnoreCase( "LOOKUP")) {
            System.out.println("Client: " + requestPacket.operation + " " + requestPacket.DNS   + ":" + received);
        } else {
            System.out.println("Client: " + requestPacket.operation + " " + requestPacket.DNS  + " " + requestPacket.IP_address + ":" + received);
        }

        try {
            while (in.readLine() != null);
            System.out.println("Client: Server shut down output: closing");
            sslSocket.close();
        } catch (IOException e){
            sslSocket.close();
        }

        sslSocket.shutdownOutput();

    }
}