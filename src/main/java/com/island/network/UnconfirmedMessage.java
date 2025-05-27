package com.island.network;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UnconfirmedMessage {
    private final Message message;
    private final Set<String> pendingReceivers;
    private int retryCount;

    // Constructor (collection of received messages and pending recipients)
    public UnconfirmedMessage(Message message, Set<String> receivers) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        this.message = message;
        // Use defensive copying to prevent external modifications
        this.pendingReceivers = new HashSet<>(receivers != null ? receivers : Collections.emptySet());
        this.retryCount = 0;
    }

    // Receiver management methods
    public synchronized void removeReceiver(String receiver) {
        pendingReceivers.remove(receiver);
    }

    public synchronized boolean hasPendingReceivers() {
        return !pendingReceivers.isEmpty();
    }

    // Management method for retry count
    public synchronized void incrementRetryCount() {
        retryCount++;
    }

    public synchronized int getRetryCount() {
        return retryCount;
    }

    public Message getMessage() {
        return message;
    }

    /**
     * Get an immutable receiver collection view
     */
    public Set<String> getPendingReceivers() {
        return Collections.unmodifiableSet(pendingReceivers);
    }
}