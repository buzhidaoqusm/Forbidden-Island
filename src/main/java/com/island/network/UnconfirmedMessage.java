package com.island.network;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UnconfirmedMessage {
    private final Message message;
    private final Set<String> pendingReceivers;
    private int retryCount;

    //-------------------------
    // 构造函数（接收消息和待确认接收者集合）
    //-------------------------
    public UnconfirmedMessage(Message message, Set<String> receivers) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        this.message = message;
        // 使用防御性拷贝防止外部修改
        this.pendingReceivers = new HashSet<>(receivers != null ? receivers : Collections.emptySet());
        this.retryCount = 0;
    }

    //-------------------------
    // 接收者管理方法
    //-------------------------
    public synchronized void removeReceiver(String receiver) {
        pendingReceivers.remove(receiver);
    }

    public synchronized boolean hasPendingReceivers() {
        return !pendingReceivers.isEmpty();
    }

    //-------------------------
    // 重试计数管理方法
    //-------------------------
    public synchronized void incrementRetryCount() {
        retryCount++;
    }

    public synchronized int getRetryCount() {
        return retryCount;
    }

    //-------------------------
    // Getter 方法
    //-------------------------
    public Message getMessage() {
        return message;
    }

    /**
     * 获取不可修改的接收者集合视图
     */
    public Set<String> getPendingReceivers() {
        return Collections.unmodifiableSet(pendingReceivers);
    }
}