import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Common {

    public static void doHandshake(SocketChannel socketChannel, SSLEngine engine,
                     ByteBuffer myNetData, ByteBuffer peerNetData) throws Exception {

        // Create byte buffers to use for holding application data
        int appBufferSize = engine.getSession().getApplicationBufferSize();
        ByteBuffer myAppData = ByteBuffer.allocate(appBufferSize);
        ByteBuffer peerAppData = ByteBuffer.allocate(appBufferSize);

        // Begin handshake
        engine.beginHandshake();
        SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();

        // Process handshaking message
        while (hs != SSLEngineResult.HandshakeStatus.FINISHED &&
                hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {

            switch (hs) {

                case NEED_UNWRAP:
                    // Receive handshaking data from peer
                    if (socketChannel.read(peerNetData) < 0) {
                        // The channel has reached end-of-stream
                    }

                    // Process incoming handshaking data
                    peerNetData.flip();
                    SSLEngineResult res = engine.unwrap(peerNetData, peerAppData);
                    peerNetData.compact();
                    hs = res.getHandshakeStatus();

                    // Check status
                    switch (res.getStatus()) {
                        case OK :
                            // Handle OK status
                            break;

                        // Handle other status: BUFFER_UNDERFLOW, BUFFER_OVERFLOW, CLOSED
                        // ...
                    }
                    break;

                case NEED_WRAP :
                    // Empty the local network packet buffer.
                    myNetData.clear();

                    // Generate handshaking data
                    res = engine.wrap(myAppData, myNetData);
                    hs = res.getHandshakeStatus();

                    // Check status
                    switch (res.getStatus()) {
                        case OK :
                            myNetData.flip();

                            // Send the handshaking data to peer
                            while (myNetData.hasRemaining()) {
                                socketChannel.write(myNetData);
                            }
                            break;

                        // Handle other status:  BUFFER_OVERFLOW, BUFFER_UNDERFLOW, CLOSED
                        // ...
                    }
                    break;

                case NEED_TASK :
                    // Handle blocking tasks
                    break;

                // Handle other status:  // FINISHED or NOT_HANDSHAKING
                // ...
            }
        }

        // Processes after handshaking
        // ...
    }




}
