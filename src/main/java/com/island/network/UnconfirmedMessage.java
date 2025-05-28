package com.island.network;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 表示一个待确认的消息，跟踪其接收者状态和重试次数
 */
public class UnconfirmedMessage {
    private final Message message;
    private final Set<ReceiverInfo> pendingReceivers;
    private int retryCount;
    private final Instant creationTime;
    
    // 重试相关的常量
    public static final int MAX_RETRY_COUNT = 3;
    public static final long RETRY_INTERVAL_MS = 1000; // 重试间隔（毫秒）
    public static final long MESSAGE_TIMEOUT_MS = 10000; // 消息超时时间（毫秒）

    public static class ReceiverInfo {
        private final String id;
        private final InetAddress address;
        private final int port;
        private Instant lastAttempt;

        public ReceiverInfo(String id, InetAddress address, int port) {
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("接收者ID不能为空");
            }
            if (address == null) {
                throw new IllegalArgumentException("接收者地址不能为null");
            }
            if (port <= 0 || port > 65535) {
                throw new IllegalArgumentException("无效的端口号");
            }
            
            this.id = id;
            this.address = address;
            this.port = port;
            this.lastAttempt = null;
        }

        public String getId() {
            return id;
        }

        public InetAddress getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }

        public synchronized Instant getLastAttempt() {
            return lastAttempt;
        }

        public synchronized void updateLastAttempt() {
            this.lastAttempt = Instant.now();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ReceiverInfo that = (ReceiverInfo) o;
            return id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return String.format("Receiver[id=%s, address=%s, port=%d]", 
                id, address.getHostAddress(), port);
        }
    }

    /**
     * 构造函数
     * @param message 要发送的消息
     * @param receivers 接收者集合
     * @throws IllegalArgumentException 如果消息为null或接收者集合无效
     */
    public UnconfirmedMessage(Message message, Set<ReceiverInfo> receivers) {
        if (message == null) {
            throw new IllegalArgumentException("消息不能为null");
        }
        if (receivers == null || receivers.isEmpty()) {
            throw new IllegalArgumentException("接收者集合不能为空");
        }
        
        this.message = message;
        this.pendingReceivers = new HashSet<>(receivers);
        this.retryCount = 0;
        this.creationTime = Instant.now();
    }

    /**
     * 移除已确认接收的接收者
     * @param receiverId 接收者ID
     * @return 如果接收者存在并被移除则返回true
     */
    public synchronized boolean removeReceiver(String receiverId) {
        return pendingReceivers.removeIf(receiver -> receiver.getId().equals(receiverId));
    }

    /**
     * 检查是否还有未确认的接收者
     */
    public synchronized boolean hasPendingReceivers() {
        return !pendingReceivers.isEmpty();
    }

    /**
     * 增加重试次数
     * @return 如果达到最大重试次数则返回true
     */
    public synchronized boolean incrementRetryCount() {
        retryCount++;
        return retryCount >= MAX_RETRY_COUNT;
    }

    /**
     * 检查消息是否已超时
     */
    public boolean isTimeout() {
        return Instant.now().isAfter(creationTime.plusMillis(MESSAGE_TIMEOUT_MS));
    }

    /**
     * 检查是否可以进行下一次重试
     */
    public boolean canRetry() {
        return retryCount < MAX_RETRY_COUNT && !isTimeout();
    }

    /**
     * 获取下一次重试的延迟时间（使用指数退避）
     * @return 延迟毫秒数
     */
    public long getNextRetryDelay() {
        return RETRY_INTERVAL_MS * (1L << retryCount); // 指数退避：1s, 2s, 4s
    }

    // Getters
    public Message getMessage() {
        return message;
    }

    public synchronized int getRetryCount() {
        return retryCount;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    /**
     * 获取待确认接收者的不可修改视图
     */
    public Set<ReceiverInfo> getPendingReceivers() {
        return Collections.unmodifiableSet(pendingReceivers);
    }
}