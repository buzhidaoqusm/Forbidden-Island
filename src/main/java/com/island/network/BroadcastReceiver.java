package com.island.network;

import com.island.util.EncryptionUtil;
import javafx.application.Platform;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * BroadcastReceiver handles UDP broadcast messages for game communication.
 * It listens for both heartbeat messages and game-specific messages on a dedicated port.
 */
public class BroadcastReceiver implements Runnable {
    /** UDP socket for receiving broadcast messages */
    private final DatagramSocket socket;
    
    /** Reference to the room controller for handling received messages */
    private final RoomController roomController;
    
    /** Flag indicating whether the receiver is currently running */
    private volatile boolean running;

    /**
     * Constructor that initializes the broadcast receiver
     * @param roomController The room controller to handle received messages
     * @throws RuntimeException if socket creation fails
     */
    public BroadcastReceiver(RoomController roomController) {
        try {
            this.socket = new DatagramSocket(null);  // Create unbound socket
            this.socket.setReuseAddress(true);       // Enable address reuse
            this.socket.bind(new java.net.InetSocketAddress(8888));  // Bind to specific port
            this.roomController = roomController;
            this.running = true;
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Main loop that continuously listens for incoming broadcast messages
     * Handles message decryption and processing
     */
    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String encryptedMessage = new String(packet.getData(), 0, packet.getLength());
                
                // Decrypt the received message
                String message = EncryptionUtil.decrypt(encryptedMessage);
                
                handleMessage(message, packet.getAddress());
            } catch (IOException e) {
                if (!running) {
                    // Socket was intentionally closed, exit gracefully
                    break;
                } else {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Processes received messages and routes them to appropriate handlers
     * @param message The decrypted message content
     * @param sender The address of the message sender
     */
    private void handleMessage(String message, InetAddress sender) {
        String[] parts = message.split("\\|");
        if (parts[0].equals("HEARTBEAT")) {
            // Handle heartbeat messages
            int roomId = Integer.parseInt(parts[1]);
            // Check if room ID matches
            if (roomId != roomController.getRoomId()) {
                return;
            }
            String username = parts[2];
            roomController.updatePlayerHeartbeat(username);
        } else {
            // Handle game messages
            try {
                Message gameMessage = Message.fromString(message);
                // Check if room ID matches
                if (gameMessage.getRoomId() != roomController.getRoomId()) {
                    return;
                }
                if (gameMessage.getTo() != null
                        && !gameMessage.getTo().equals(roomController.getRoom().getCurrentProgramPlayer().getName())
                        && !gameMessage.getTo().equals("system") // When draw flood cards in a turn end, the message is from a system, but it's actually for the host player
                        && !gameMessage.getType().equals(MessageType.TURN_START) // Turn start message is sent to all players
                ) {
                    return;
                }
                // Notify game manager to process message
                Platform.runLater(() -> {
                    // Update UI in JavaFX thread
                    try {
                        handleGameMessage(gameMessage);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Forwards game messages to the RoomController for processing
     * @param message The game message to be processed
     * @throws Exception if message handling fails
     */
    private void handleGameMessage(Message message) throws Exception {
        // Forward message to RoomManager for processing
        roomController.handleGameMessage(message);
    }

    /**
     * Stops the broadcast receiver and cleans up resources
     * Ensures proper socket disconnection and closure
     */
    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}