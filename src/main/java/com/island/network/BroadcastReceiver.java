package com.island.network;

import com.island.util.EncryptionUtil;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BroadcastReceiver implements Runnable {
    private volatile boolean running = true;
    private DatagramSocket socket;
    private final RoomController roomController;
    private final byte[] buffer = new byte[1024]; // Receive buffer
    /**
     * 构造函数
     * @param roomController 房间控制器
     * @param port 监听端口
     * @throws SocketException 如果socket创建失败
     */
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

    /**
     * 处理接收到的消息
     * @param messageStr 消息字符串
     */
    private void handleMessage(String messageStr) {
        try {
            Message message = Message.fromString(messageStr);
            
            // 3. 处理消息
            roomController.getMessageHandler().handleMessage(message);
        } catch (Exception e) {
            System.err.println("Message processing failed: " + e.getMessage());
        }
    }

    /**
     * 停止接收
     */
    // Stop receiving
    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close(); // Close socket to unblock
        }
    }
}