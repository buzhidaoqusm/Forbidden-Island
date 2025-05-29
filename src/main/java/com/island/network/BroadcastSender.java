package com.island.network;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import com.island.view.ActionLogView;
import com.island.util.EncryptionUtil;

public class BroadcastSender {
    private DatagramSocket socket;
    private final Set<String> broadcastAddresses;
    private final int port;
    private final ActionLogView logView;
    private final ExecutorService senderPool;
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int SEND_TIMEOUT_MS = 5000; // 发送超时时间
    
    /**
     * 构造函数 - 使用指定的广播地址和端口
     * @param broadcastAddress 单个广播地址
     * @param port 端口号
     * @param logView 日志视图
     * @throws IllegalArgumentException 如果广播地址无效
     * @throws SocketException 如果创建socket失败
     */
    public BroadcastSender(String broadcastAddress, int port, ActionLogView logView) throws SocketException {
        this(Collections.singleton(broadcastAddress), port, logView);
    }

    /**
     * 构造函数 - 使用多个广播地址和端口
     * @param broadcastAddresses 广播地址集合
     * @param port 端口号
     * @param logView 日志视图
     * @throws IllegalArgumentException 如果任何广播地址无效
     * @throws SocketException 如果创建socket失败
     */
    public BroadcastSender(Set<String> broadcastAddresses, int port, ActionLogView logView) throws SocketException {
        if (broadcastAddresses == null || broadcastAddresses.isEmpty()) {
            throw new IllegalArgumentException("At least one broadcast address must be provided");
        }
        
        // 验证所有广播地址
        for (String address : broadcastAddresses) {
            if (!isValidBroadcastAddress(address)) {
                throw new IllegalArgumentException("Invalid broadcast address: " + address);
            }
        }

        this.broadcastAddresses = new HashSet<>(broadcastAddresses);
        this.port = port;
        this.logView = logView;
        this.socket = new DatagramSocket();
        this.socket.setBroadcast(true);
        
        // 创建发送线程池
        this.senderPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE, r -> {
            Thread t = new Thread(r, "BroadcastSender-" + UUID.randomUUID().toString().substring(0, 8));
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * 构造函数 - 自动检测所有可用的广播地址
     * @param port 端口号
     * @param logView 日志视图
     * @throws SocketException 如果创建socket失败
     */
    public BroadcastSender(int port, ActionLogView logView) throws SocketException, BroadcastAddressCalculator.NetworkInterfaceException {
        this(BroadcastAddressCalculator.getBroadcastAddresses(), port, logView);
        if (this.broadcastAddresses.isEmpty()) {
            throw new IllegalStateException("No valid broadcast addresses found on any network interface");
        }
    }

    /**
     * 广播消息到所有配置的广播地址
     * @param message 要广播的消息
     * @throws BroadcastException 如果广播失败
     */
    public void broadcast(Message message) throws BroadcastException {
        if (message == null) {
            logView.error("无法广播空消息");
            throw new IllegalArgumentException("消息不能为空");
        }

        logView.log("准备广播消息: " + message.getType());

        // 1. 序列化消息
        byte[] serializedData;
        try {
            serializedData = message.toBytes();
            logView.log("消息序列化成功，大小: " + serializedData.length + " 字节");
        } catch (Exception e) {
            logView.error("消息序列化失败: " + e.getMessage());
            throw new BroadcastException("消息序列化失败", e);
        }

        // 2. 加密序列化后的数据
        String encryptedMessage;
        try {
            encryptedMessage = EncryptionUtil.encrypt(new String(serializedData));
            logView.log("消息加密成功");
        } catch (Exception e) {
            logView.error("消息加密失败: " + e.getMessage());
            throw new BroadcastException("消息加密失败", e);
        }

        // 3. 转换为字节数组准备发送
        byte[] dataToSend = encryptedMessage.getBytes();
        logView.log("准备发送到 " + broadcastAddresses.size() + " 个广播地址");
        
        List<Future<Boolean>> sendFutures = new ArrayList<>();
        List<String> failedAddresses = new ArrayList<>();

        // 4. 并行发送到所有地址
        for (String address : broadcastAddresses) {
            Future<Boolean> future = senderPool.submit(() -> {
                try {
                    logView.log("正在发送到地址: " + address);
                    InetAddress inetAddress = InetAddress.getByName(address);
                    DatagramPacket packet = new DatagramPacket(dataToSend, dataToSend.length, inetAddress, port);
            socket.send(packet);
                    logView.success("成功发送到地址: " + address);
                    return true;
                } catch (Exception e) {
                    logView.warning("发送到 " + address + " 失败: " + e.getMessage());
                    return false;
                }
            });
            sendFutures.add(future);
        }

        // 5. 等待所有发送完成或超时
        boolean hasSuccess = false;
        for (int i = 0; i < sendFutures.size(); i++) {
            try {
                Boolean result = sendFutures.get(i).get(SEND_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if (!result) {
                    failedAddresses.add(new ArrayList<>(broadcastAddresses).get(i));
                } else {
                    hasSuccess = true;
                }
            } catch (Exception e) {
                failedAddresses.add(new ArrayList<>(broadcastAddresses).get(i));
                logView.error("等待发送结果时出错: " + e.getMessage());
            }
        }

        // 6. 处理发送结果
        if (!hasSuccess) {
            String errorMsg = "所有广播地址发送失败: " + String.join(", ", failedAddresses);
            logView.error(errorMsg);
            throw new BroadcastException(errorMsg);
        } else if (!failedAddresses.isEmpty()) {
            logView.warning("部分广播地址发送失败: " + String.join(", ", failedAddresses));
        } else {
            logView.success("消息广播完成");
        }
    }

    /**
     * 获取当前配置的所有广播地址
     * @return 广播地址集合的不可修改视图
     */
    public Set<String> getBroadcastAddresses() {
        return Collections.unmodifiableSet(broadcastAddresses);
    }

    /**
     * 添加新的广播地址
     * @param address 要添加的广播地址
     * @throws IllegalArgumentException 如果地址无效
     */
    public void addBroadcastAddress(String address) {
        if (!isValidBroadcastAddress(address)) {
            throw new IllegalArgumentException("Invalid broadcast address: " + address);
        }
        broadcastAddresses.add(address);
        logView.success("Added broadcast address: " + address);
    }

    /**
     * 移除广播地址
     * @param address 要移除的广播地址
     * @return 如果地址存在并被移除则返回true
     */
    public boolean removeBroadcastAddress(String address) {
        boolean removed = broadcastAddresses.remove(address);
        if (removed) {
            logView.success("Removed broadcast address: " + address);
        }
        return removed;
    }

    /**
     * 关闭发送器并释放资源
     */
    public void close() {
        // 关闭线程池
        if (senderPool != null && !senderPool.isShutdown()) {
            senderPool.shutdown();
            try {
                // 等待所有任务完成或超时
                if (!senderPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    senderPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                senderPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // 关闭socket
        if (socket != null && !socket.isClosed()) {
            socket.close();
            logView.success("Broadcast sender closed successfully");
        }
    }

    /**
     * 检查给定的IP地址是否是有效的广播地址
     * 广播地址可以是：
     * 1. 有限广播地址 255.255.255.255
     * 2. 子网定向广播地址（基于网络掩码计算）
     * 3. 特定网络类别的广播地址：
     *    - A类网络：netid.255.255.255
     *    - B类网络：netid.netid.255.255
     *    - C类网络：netid.netid.netid.255
     * 
     * @param ip 要检查的IP地址
     * @param subnetMask 子网掩码（可选，格式如"255.255.255.0"）
     * @return 如果是有效的广播地址则返回true
     */
    public static boolean isValidBroadcastAddress(String ip, String subnetMask) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        try {
            // 检查是否是IPv4地址
            if (!ip.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")) {
                return false;
            }

            // 解析IP地址
            InetAddress address = InetAddress.getByName(ip);
            
            // 如果不是IPv4地址，返回false
            if (!(address instanceof Inet4Address)) {
                return false;
            }

            // 排除回环地址 (127.x.x.x)
            if (address.isLoopbackAddress() || ip.startsWith("127.")) {
                return false;
            }

            // 检查是否是有限广播地址 (255.255.255.255)
            if (ip.equals("255.255.255.255")) {
                return true;
            }

            String[] octets = ip.split("\\.");
            if (octets.length != 4) {
                return false;
            }

            // 检查每个字节是否在有效范围内 (0-255)
            int[] ipBytes = new int[4];
            for (int i = 0; i < 4; i++) {
                ipBytes[i] = Integer.parseInt(octets[i]);
                if (ipBytes[i] < 0 || ipBytes[i] > 255) {
                    return false;
                }
            }

            // 如果提供了子网掩码，使用子网掩码验证
            if (subnetMask != null && !subnetMask.isEmpty()) {
                return isValidSubnetBroadcast(ipBytes, subnetMask);
            }

            // 基于网络类别验证广播地址
            return isValidClassBasedBroadcast(ipBytes);
        } catch (UnknownHostException | NumberFormatException e) {
            return false;
        }
    }

    /**
     * 重载方法，不指定子网掩码时使用
     */
    public static boolean isValidBroadcastAddress(String ip) {
        return isValidBroadcastAddress(ip, null);
    }

    /**
     * 验证基于网络类别的广播地址
     */
    private static boolean isValidClassBasedBroadcast(int[] ipBytes) {
        // A类网络 (1-126)
        if (ipBytes[0] >= 1 && ipBytes[0] <= 126) {
            return ipBytes[1] == 255 && ipBytes[2] == 255 && ipBytes[3] == 255;
        }
        // B类网络 (128-191)
        else if (ipBytes[0] >= 128 && ipBytes[0] <= 191) {
            return ipBytes[2] == 255 && ipBytes[3] == 255;
        }
        // C类网络 (192-223)
        else if (ipBytes[0] >= 192 && ipBytes[0] <= 223) {
            return ipBytes[3] == 255;
        }
        return false;
    }

    /**
     * 验证基于子网掩码的广播地址
     */
    private static boolean isValidSubnetBroadcast(int[] ipBytes, String subnetMask) {
        try {
            String[] maskOctets = subnetMask.split("\\.");
            if (maskOctets.length != 4) {
                return false;
            }

            // 解析子网掩码
            int[] maskBytes = new int[4];
            for (int i = 0; i < 4; i++) {
                maskBytes[i] = Integer.parseInt(maskOctets[i]);
                if (maskBytes[i] < 0 || maskBytes[i] > 255) {
                    return false;
                }
            }

            // 验证子网掩码的有效性（必须是连续的1followed by连续的0）
            int maskInt = (maskBytes[0] << 24) | (maskBytes[1] << 16) | 
                         (maskBytes[2] << 8) | maskBytes[3];
            int hostPart = ~maskInt;
            if ((hostPart & (hostPart + 1)) != 0) {
                return false; // 无效的子网掩码
            }

            // 检查IP地址的主机部分是否全为1（广播地址特征）
            for (int i = 0; i < 4; i++) {
                if ((ipBytes[i] & ~maskBytes[i]) != (~maskBytes[i] & 0xFF)) {
                    return false;
                }
            }

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static class BroadcastException extends Exception {
        public BroadcastException(String message) {
            super(message);
        }

        public BroadcastException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}