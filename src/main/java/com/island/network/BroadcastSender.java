package com.island.network;

import java.io.IOException;
import java.net.*;

public class BroadcastSender {
    private DatagramSocket socket;
    private final String broadcastAddress;
    private final int port;

    // Constructor (requires specifying broadcast address and port)
    public BroadcastSender(String broadcastAddress, int port) throws SocketException {
        this.broadcastAddress = broadcastAddress;
        this.port = port;
        this.socket = new DatagramSocket();
        this.socket.setBroadcast(true); // Enable broadcast mode
    }

    // Core broadcast method
    public void broadcast(Message message) {
        try {
            // 1. serialize messages into bytes
            byte[] data = message.toBytes();

            // 2. Create a broadcast packet
            InetAddress address = InetAddress.getByName(broadcastAddress);
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);

            // 3. Send a broadcast
            socket.send(packet);
        } catch (UnknownHostException e) {
            System.err.println("Invalid broadcast address: " + broadcastAddress);
        } catch (IOException e) {
            System.err.println("Broadcast transmission failed: " + e.getMessage());
        }
    }

    // Resource cleaning methods
    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    // Optional tool method
    public static boolean isValidBroadcastAddress(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address.isAnyLocalAddress() || address.isMulticastAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }
}