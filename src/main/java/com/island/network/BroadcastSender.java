package com.island.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.island.util.EncryptionUtil;

/**
 * BroadcastSender handles sending UDP broadcast messages for game communication.
 * It manages the broadcast socket and encrypts messages before transmission.
 */
public class BroadcastSender {
    /** UDP socket for sending broadcast messages */
    private final DatagramSocket socket;
    
    /** Network broadcast address for message distribution */
    private final String broadcastAddress;

    /**
     * Constructor that initializes the broadcast sender
     * Creates a broadcast-enabled socket and calculates the broadcast address
     * @throws RuntimeException if the broadcast sender cannot be created
     */
    public BroadcastSender() {
        try {
            this.socket = new DatagramSocket();
            this.socket.setBroadcast(true);
            this.broadcastAddress = BroadcastAddressCalculator.getLocalIpAndSubnet();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create broadcast sender", e);
        }
    }

    /**
     * Broadcasts a string message to all listeners on the network
     * @param message The message to broadcast
     * @throws Exception if broadcasting fails
     * @throws IllegalStateException if broadcast address is not available
     */
    public void broadcast(String message) throws Exception {
        if (broadcastAddress == null) {
            throw new IllegalStateException("Unable to get broadcast address");
        }
        
        // Encrypt the message before sending
        String encryptedMessage = EncryptionUtil.encrypt(message);
        
        InetAddress address = InetAddress.getByName(broadcastAddress);
        DatagramPacket packet = new DatagramPacket(
                encryptedMessage.getBytes(),
                encryptedMessage.length(),
                address,
                8888
        );
        socket.send(packet);
    }

    /**
     * Broadcasts a Message object to all listeners on the network
     * @param message The Message object to broadcast
     * @throws Exception if broadcasting fails
     */
    public void broadcast(Message message) throws Exception {
        broadcast(message.toString());
    }

    /**
     * Closes the broadcast socket and releases resources
     */
    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
