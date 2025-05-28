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
    private final byte[] buffer = new byte[4096]; // 增加缓冲区大小以适应加密后的消息
    private final ExecutorService messageProcessorPool;
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    private static final int MAX_QUEUE_SIZE = 1000;
    private final BlockingQueue<Runnable> messageQueue;
    private final AtomicInteger droppedMessages = new AtomicInteger(0);

    /**
     * 构造函数
     * @param roomController 房间控制器
     * @param port 监听端口
     * @throws SocketException 如果socket创建失败
     */
    public BroadcastReceiver(RoomController roomController, int port) throws SocketException {
        this.roomController = roomController;
        this.socket = new DatagramSocket(port);
        
        // 创建有界队列
        this.messageQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
        
        // 创建自定义线程池
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "MessageProcessor-" + threadNumber.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        };

        // 使用自定义的线程池配置
        this.messageProcessorPool = new ThreadPoolExecutor(
            THREAD_POOL_SIZE, // 核心线程数
            THREAD_POOL_SIZE, // 最大线程数
            60L, TimeUnit.SECONDS, // 空闲线程存活时间
            messageQueue, // 使用有界队列
            threadFactory, // 自定义线程工厂
            new RejectedExecutionHandler() { // 自定义拒绝策略
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    droppedMessages.incrementAndGet();
                    System.err.println("Message queue full, message dropped. Total dropped: " + droppedMessages.get());
                }
            }
        );
    }

    @Override
    public void run() {
        try {
            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet); // 阻塞接收数据
                
                // 提交到线程池处理
                final byte[] messageData = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), messageData, 0, packet.getLength());
                
                messageProcessorPool.submit(() -> {
                    try {
                        String encryptedMessage = new String(messageData);
                        handleMessage(encryptedMessage);
                    } catch (Exception e) {
                        System.err.println("Error processing message: " + e.getMessage());
                    }
                });
            }
        } catch (IOException e) {
            if (running) { // 仅在非主动停止时打印错误
                System.err.println("接收错误: " + e.getMessage());
            }
        } finally {
            cleanup();
        }
    }

    /**
     * 处理接收到的消息
     * @param encryptedMessage 加密的消息字符串
     */
    private void handleMessage(String encryptedMessage) {
        try {
            // 1. 解密消息
            String decryptedMessage = EncryptionUtil.decrypt(encryptedMessage);
            
            // 2. 解析消息
            Message message = Message.fromString(decryptedMessage);
            
            // 3. 处理消息
            roomController.getMessageHandler().handleMessage(message);
        } catch (Exception e) {
            System.err.println("Message processing failed: " + e.getMessage());
        }
    }

    /**
     * 停止接收
     */
    public void stop() {
        running = false;
        cleanup();
    }

    /**
     * 清理资源
     */
    private void cleanup() {
        // 关闭线程池
        if (messageProcessorPool != null && !messageProcessorPool.isShutdown()) {
            messageProcessorPool.shutdown();
            try {
                // 等待所有任务完成或超时
                if (!messageProcessorPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    messageProcessorPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                messageProcessorPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // 关闭socket
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    /**
     * 获取已丢弃的消息数量
     * @return 丢弃的消息数量
     */
    public int getDroppedMessageCount() {
        return droppedMessages.get();
    }
}