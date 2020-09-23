package project.channel;

import project.message.*;
import project.peer.Peer;
import project.protocols.BackupProtocol;

import java.io.IOException;
import java.net.DatagramPacket;

public class MulticastDataBackupChannel extends Channel {

    public MulticastDataBackupChannel(String address, int port) {
        super(address, port);
    }

    @Override
    public void readable_message(DatagramPacket packet) {
        try {

            final byte[] raw_message = packet.getData();

            System.out.println("PUTCHUNK RECEIVED RAW (" + raw_message.length + ")");
            System.out.println("PUTCHUNK RECEIVED PACKET (" + packet.getLength() + ")");
            BaseMessage message = MessageParser.parseMessage(raw_message, packet.getLength());

            if(message.getSender_id() == Peer.id){
                return;
            }

            if(message.getMessage_type() == Message_type.PUTCHUNK){
                BackupProtocol.receive_putchunk((PutChunkMessage) message);
            }
            else System.out.println("Invalid message type for Control Channel: " + message.getMessage_type());

        } catch (InvalidMessageException e) {
            e.printStackTrace();
        }
    }
}