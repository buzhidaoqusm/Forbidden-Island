package com.island.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class BroadcastReceiver implements Runnable {
    private volatile boolean running = true;
    private DatagramSocket socket;
    private final RoomController roomController;
    private final byte[] buffer = new byte[1024]; // 接收缓冲区

    // 构造函数（需传入RoomController和端口）
    public BroadcastReceiver(RoomController roomController, int port) throws SocketException {
        this.roomController = roomController;
        this.socket = new DatagramSocket(port);
    }

    @Override
    public void run() {
        try {
            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet); // 阻塞接收数据
                String received = new String(packet.getData(), 0, packet.getLength());
                handleMessage(received); // 处理消息
            }
        } catch (IOException e) {
            if (running) { // 仅在未主动停止时打印错误
                System.err.println("接收错误: " + e.getMessage());
            }
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    // 私有方法处理消息
    private void handleMessage(String messageStr) {
        try {
            Message message = Message.fromString(messageStr);
            roomController.getMessageHandler().handleMessage(message); // 交给RoomController处理
        } catch (Exception e) {
            System.err.println("消息解析失败: " + e.getMessage());
        }
    }

    // 停止接收
    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close(); // 关闭socket解除阻塞
        }
    }
}