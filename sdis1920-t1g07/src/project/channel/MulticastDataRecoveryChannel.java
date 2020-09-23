package project.channel;

import project.message.*;
import project.peer.Peer;
import project.protocols.RestoreProtocol;

import java.io.IOException;
import java.net.DatagramPacket;

public class MulticastDataRecoveryChannel extends Channel {

    public MulticastDataRecoveryChannel(String address, int port) {
        super(address, port);
    }

    @Override
    public void readable_message(DatagramPacket packet) {
        try {
            byte [] raw_message = packet.getData();
            BaseMessage message = MessageParser.parseMessage(raw_message, raw_message.length);

            if(message.getSender_id() == Peer.id)
                return;

            if(message.getMessage_type() == Message_type.CHUNK)
                RestoreProtocol.receive_chunk((ChunkMessage) message);
            else System.out.println("Invalid message type for Control Channel: " + message.getMessage_type());


        } catch (InvalidMessageException e) {
            e.printStackTrace();
        }
    }
}