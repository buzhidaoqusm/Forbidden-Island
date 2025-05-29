package com.island.network;

import java.util.HashSet;
import java.util.Set;

/**
 * UnconfirmedMessage class represents a message that requires acknowledgment from multiple receivers.
 * It tracks the pending receivers and retry attempts for message delivery.
 */
public class UnconfirmedMessage {
    /** The original message that needs acknowledgment */
    private final Message message;
    
    /** Set of receivers who haven't acknowledged the message yet */
    private final Set<String> pendingReceivers;
    
    /** Counter for tracking the number of retry attempts */
    private int retryCount = 0;
    
    /**
     * Constructor for creating an unconfirmed message
     * @param message The message to be tracked
     * @param receivers Set of receivers who need to acknowledge the message
     */
    public UnconfirmedMessage(Message message, Set<String> receivers) {
        this.message = message;
        this.pendingReceivers = new HashSet<>(receivers);
    }

    /**
     * Removes a receiver from the pending receivers list after acknowledgment
     * @param receiver The receiver to be removed
     */
    public void removeReceiver(String receiver) {
        pendingReceivers.remove(receiver);
    }

    /**
     * Checks if there are any pending receivers who haven't acknowledged the message
     * @return true if there are pending receivers, false otherwise
     */
    public boolean hasPendingReceivers() {
        return !pendingReceivers.isEmpty();
    }

    /**
     * Gets the number of retry attempts made for this message
     * @return The current retry count
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * Increments the retry counter when a message delivery retry is attempted
     */
    public void incrementRetryCount() {
        retryCount++;
    }

    /**
     * Gets a copy of the original message
     * @return A new copy of the message
     */
    public Message getMessage() {
        return new Message(message);
    }

    /**
     * Gets the set of receivers who haven't acknowledged the message yet
     * @return Set of pending receivers
     */
    public Set<String> getPendingReceivers() {
        return pendingReceivers;
    }
}
