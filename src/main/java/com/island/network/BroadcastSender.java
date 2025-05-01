package com.island.network;

import java.io.IOException;
import java.net.*;

public class BroadcastSender {
    private DatagramSocket socket;
    private final String broadcastAddress;
    private final int port; // 需补充端口配置

    //-------------------------
    // 构造函数（需指定广播地址和端口）
    //-------------------------
    public BroadcastSender(String broadcastAddress, int port) throws SocketException {
        this.broadcastAddress = broadcastAddress;
        this.port = port;
        this.socket = new DatagramSocket();
        this.socket.setBroadcast(true); // 启用广播模式
    }

    //-------------------------
    // 核心广播方法
    //-------------------------
    public void broadcast(Message message) {
        try {
            // 1. 序列化消息为字节
            byte[] data = message.toBytes(); // 假设 Message 类有 toBytes() 方法
            // byte[] data = message.toString().getBytes(); // 备选方案（需确保Message有toString序列化）

            // 2. 创建广播数据包
            InetAddress address = InetAddress.getByName(broadcastAddress);
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);

            // 3. 发送广播
            socket.send(packet);
        } catch (UnknownHostException e) {
            System.err.println("无效广播地址: " + broadcastAddress);
        } catch (IOException e) {
            System.err.println("广播发送失败: " + e.getMessage());
        }
    }

    //-------------------------
    // 资源清理方法
    //-------------------------
    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    //-------------------------
    // 可选工具方法
    //-------------------------
    public static boolean isValidBroadcastAddress(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address.isAnyLocalAddress() || address.isMulticastAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }
}