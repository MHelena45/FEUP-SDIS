package project.channel;

import project.message.*;
import project.peer.Peer;
import project.protocols.BackupProtocol;
import project.protocols.DeleteProtocol;
import project.protocols.ReclaimProtocol;
import project.protocols.RestoreProtocol;

import java.io.IOException;
import java.net.DatagramPacket;

public class MulticastControlChannel extends Channel {

    public MulticastControlChannel(String address, int port) {
        super(address, port);
    }

    @Override
    public void readable_message(DatagramPacket packet) {
        try {
            byte [] raw_message = packet.getData();
            BaseMessage message = MessageParser.parseMessage(raw_message, raw_message.length);

            if(message.getSender_id() == Peer.id){
                return;
            }

            switch (message.getMessage_type()) {
                case STORED:
                    BackupProtocol.receive_stored((StoredMessage) message);
                    break;
                case GETCHUNK:
                    RestoreProtocol.receive_getchunk((GetChunkMessage) message);
                    break;
                case DELETE:
                    DeleteProtocol.receive_delete((DeleteMessage) message);
                    break;
                case REMOVED:
                    ReclaimProtocol.receive_removed((RemovedMessage) message);
                    break;
                default:
                    System.out.println("Invalid message type for Control Channel: " + message.getMessage_type());
                    break;
            }
        } catch (InvalidMessageException e) {
            e.printStackTrace();
        }
    }
}