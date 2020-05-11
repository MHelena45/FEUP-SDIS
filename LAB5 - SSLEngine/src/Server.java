import java.io.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import javax.net.ssl.*;
import java.security.*;

public class Server{
    private static HashMap<String,String> DNStable = new HashMap<String, String>();

    private static int port;
    private static String host;

    static void usage(){
        System.out.println("Usage: java SSLServer <port_no> <host> <cypher-suite>*");
    }

    public static void main(String[] args) throws IOException {

        if(args.length < 1){
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
            host = args[1];
        } catch ( NumberFormatException e){
            usage();
            return;
        }


        // Create and initialize the SSLContext with key material
        char[] passphrase = "passphrase".toCharArray();

        // First initialize the key and trust material
        KeyStore ksKeys = null;
        SSLEngine engine = null;

        try {
            ksKeys = KeyStore.getInstance("PKCS12");
            ksKeys.load(new FileInputStream("server.keys"), passphrase);
            KeyStore ksTrust = KeyStore.getInstance("PKCS12");
            ksTrust.load(new FileInputStream("truststore"), passphrase);

            // KeyManagers decide which key material to use
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("PKIX");
            kmf.init(ksKeys, passphrase);

            // TrustManagers decide whether to allow connections
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
            tmf.init(ksTrust);

            // Get an SSLContext for DTLS Protocol without authentication
            SSLContext sslContext = SSLContext.getInstance("DTLS");
            sslContext.init(null, null, null);

            // Create the engine
            engine = sslContext.createSSLEngine(host, port);

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }


        // Use the engine as server
        engine.setUseClientMode(false);

        // Require client authentication
        engine.setNeedClientAuth(false); //true


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