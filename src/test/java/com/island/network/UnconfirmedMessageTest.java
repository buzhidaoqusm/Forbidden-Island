package com.island.network;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Test class for UnconfirmedMessage functionality
 */
class UnconfirmedMessageTest {
    private UnconfirmedMessage unconfirmedMessage;
    private Message testMessage;
    private Set<String> receivers;
    private static final String RECEIVER_1 = "player1";
    private static final String RECEIVER_2 = "player2";
    private static final String RECEIVER_3 = "player3";

    @BeforeEach
    void setUp() {
        testMessage = new Message(MessageType.PLAYER_JOIN, 1, "sender");
        receivers = new HashSet<>();
        receivers.add(RECEIVER_1);
        receivers.add(RECEIVER_2);
        receivers.add(RECEIVER_3);
        unconfirmedMessage = new UnconfirmedMessage(testMessage, receivers);
    }

    /**
     * Tests that the UnconfirmedMessage is properly initialized with the given message and receivers
     */
    @Test
    void testInitialization() {
        assertEquals(3, unconfirmedMessage.getPendingReceivers().size());
        assertTrue(unconfirmedMessage.hasPendingReceivers());
        assertEquals(0, unconfirmedMessage.getRetryCount());
    }

    /**
     * Tests removing receivers and verifying the pending receivers list is updated correctly
     */
    @Test
    void testRemoveReceiver() {
        unconfirmedMessage.removeReceiver(RECEIVER_1);
        assertEquals(2, unconfirmedMessage.getPendingReceivers().size());
        assertFalse(unconfirmedMessage.getPendingReceivers().contains(RECEIVER_1));
        assertTrue(unconfirmedMessage.getPendingReceivers().contains(RECEIVER_2));
        
        // Remove non-existent receiver
        unconfirmedMessage.removeReceiver("nonexistent");
        assertEquals(2, unconfirmedMessage.getPendingReceivers().size());
    }

    /**
     * Tests the hasPendingReceivers method in various scenarios
     */
    @Test
    void testHasPendingReceivers() {
        assertTrue(unconfirmedMessage.hasPendingReceivers());
        
        // Remove all receivers
        unconfirmedMessage.removeReceiver(RECEIVER_1);
        unconfirmedMessage.removeReceiver(RECEIVER_2);
        unconfirmedMessage.removeReceiver(RECEIVER_3);
        
        assertFalse(unconfirmedMessage.hasPendingReceivers());
    }

    /**
     * Tests the retry count increment functionality
     */
    @Test
    void testRetryCount() {
        assertEquals(0, unconfirmedMessage.getRetryCount());
        
        unconfirmedMessage.incrementRetryCount();
        assertEquals(1, unconfirmedMessage.getRetryCount());
        
        // Multiple increments
        unconfirmedMessage.incrementRetryCount();
        unconfirmedMessage.incrementRetryCount();
        assertEquals(3, unconfirmedMessage.getRetryCount());
    }

    /**
     * Tests that getMessage returns a new copy of the message
     */
    @Test
    void testGetMessageCopy() {
        Message retrievedMessage = unconfirmedMessage.getMessage();
        
        // Verify it's a different instance but with same content
        assertNotSame(testMessage, retrievedMessage);
        assertEquals(testMessage.getType(), retrievedMessage.getType());
        assertEquals(testMessage.getRoomId(), retrievedMessage.getRoomId());
        assertEquals(testMessage.getFrom(), retrievedMessage.getFrom());
    }

    /**
     * Tests the behavior with empty receiver set
     */
    @Test
    void testEmptyReceiverSet() {
        UnconfirmedMessage emptyMessage = new UnconfirmedMessage(testMessage, new HashSet<>());
        assertFalse(emptyMessage.hasPendingReceivers());
        assertEquals(0, emptyMessage.getPendingReceivers().size());
    }
} 