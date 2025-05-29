package com.forbiddenisland.models.network;

import java.util.HashSet;
import java.util.Set;

public class UnconfirmedMessage {
    private final Message message;
    private final Set<String> pendingReceivers; // 等待确认的接收者
    private int retryCount = 0;
    
    public UnconfirmedMessage(Message message, Set<String> receivers) {
        this.message = message;
        this.pendingReceivers = new HashSet<>(receivers);
    }

    public void removeReceiver(String receiver) {
        pendingReceivers.remove(receiver);
    }

    public boolean hasPendingReceivers() {
        return !pendingReceivers.isEmpty();
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void incrementRetryCount() {
        retryCount++;
    }

    public Message getMessage() {
        return new Message(message);
    }

    public Set<String> getPendingReceivers() {
        return pendingReceivers;
    }
}
