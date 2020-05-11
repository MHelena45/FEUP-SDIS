import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.cert.CertificateException;
import javax.net.ssl.*;
import java.security.*;


public class Client {
    //kind of Macro
    static final String separator = " ";
    static final int  timeout = 1000;
    static int numberOfTimeOuts = 3;
    private static RequestPacket requestPacket = new RequestPacket();

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

        try {
            host = args[0];
            port = Integer.parseInt(args[1]);
            if( port <1024 || port>= 1 << 16){
                usage();
                System.out.println("\t <port_no> must be a 16 bit integer");
                return;
            }

        } catch (final NumberFormatException e) {
            usage();
            return;
        }

        // Create and initialize the SSLContext with key material
        char[] passphrase = "passphrase".toCharArray();

        // First initialize the key and trust material
        KeyStore ksKeys = null;
        KeyManagerFactory kmf = null;
        TrustManagerFactory tmf = null;

        try {
            ksKeys = KeyStore.getInstance("JKS");
            ksKeys.load(new FileInputStream("testKeys"), passphrase);
            KeyStore ksTrust = KeyStore.getInstance("JKS");
            ksTrust.load(new FileInputStream("testTrust"), passphrase);

            // KeyManagers decide which key material to use
            kmf = KeyManagerFactory.getInstance("PKIX");
            kmf.init(ksKeys, passphrase);

            // TrustManagers decide whether to allow connections
            tmf = TrustManagerFactory.getInstance("PKIX");
            tmf.init(ksTrust);

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        // Get an instance of SSLContext for TLS protocols
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }


        // Create the engine
        SSLEngine engine = sslContext.createSSLEngine(host, port);

        // Use as client
        engine.setUseClientMode(true);


        // Create a nonblocking socket channel
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(host, port));

        // Complete connection
        while (!socketChannel.isConnected()) {
            // do something until connect completed
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


        while (numberOfTimeOuts > 0) {
            try {
                run(engine, socketChannel);
                break;
            }  catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public static void run(SSLEngine engine, SocketChannel socketChannel) throws IOException {

        //Create byte buffers for holding application and encoded data

        SSLSession session = engine.getSession();
        ByteBuffer myAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
        ByteBuffer myNetData = ByteBuffer.allocate(session.getPacketBufferSize());
        ByteBuffer peerAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
        ByteBuffer peerNetData = ByteBuffer.allocate(session.getPacketBufferSize());

        // Do initial handshake
        try {
            Common.doHandshake(socketChannel, engine, myNetData, peerNetData);
        } catch (Exception e) {
            e.printStackTrace();
        }

        myAppData.put("hello".getBytes());
        myAppData.flip();

        while (myAppData.hasRemaining()) {
            // Generate TLS/DTLS encoded data (handshake or application data)
            SSLEngineResult res = engine.wrap(myAppData, myNetData);

            // Process status of call
            if (res.getStatus() == SSLEngineResult.Status.OK) {
                myAppData.compact();

                // Send TLS/DTLS encoded data to peer
                while(myNetData.hasRemaining()) {
                    int num = socketChannel.write(myNetData);
                    if (num == 0) {
                        // no bytes written; try again later
                    }
                }
            }

            // Handle other status:  BUFFER_OVERFLOW, CLOSED
        }

        // build message
  /*      String message = requestPacket.operation.toString();
        if( requestPacket.operation.equalsIgnoreCase( "LOOKUP")) {
            message += separator + requestPacket.DNS; // args separated by "|"
        } else {
            message += separator + requestPacket.DNS + separator + requestPacket.IP_address;// oper and opnds are prepare to be send
        }



        // display response
        if(requestPacket.operation.equalsIgnoreCase( "LOOKUP")) {
            System.out.println("Client: " + requestPacket.operation + " " + requestPacket.DNS   + ":" + received);
        } else {
            System.out.println("Client: " + requestPacket.operation + " " + requestPacket.DNS  + " " + requestPacket.IP_address + ":" + received);
        }

        System.out.println("Client: Server shut down output: closing");

*/
    }
}