package com.island.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class BroadcastReceiver implements Runnable {
    private volatile boolean running = true;
    private DatagramSocket socket;
    private final RoomController roomController;
    private final byte[] buffer = new byte[1024]; // Receive buffer

    // Constructor (requires passing RoomController and port)
    public BroadcastReceiver(RoomController roomController, int port) throws SocketException {
        this.roomController = roomController;
        this.socket = new DatagramSocket(port);
    }

    @Override
    public void run() {
        try {
            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet); // Block receiving data
                String received = new String(packet.getData(), 0, packet.getLength());
                handleMessage(received); // process Messages
            }
        } catch (IOException e) {
            if (running) { // Printing errors only when not actively stopped
                System.err.println("Receiving error: " + e.getMessage());
            }
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    // Handle the message
    private void handleMessage(String messageStr) {
        try {
            Message message = Message.fromString(messageStr);
            roomController.getMessageHandler().handleMessage(message); // Leave it to Room Controller for processing
        } catch (Exception e) {
            System.err.println("Message parsing failed: " + e.getMessage());
        }
    }

    // Stop receiving
    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close(); // Close socket to unblock
        }
    }

}