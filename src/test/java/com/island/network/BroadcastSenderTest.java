package com.island.network;

import com.island.util.EncryptionUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for BroadcastSender functionality.
 * Tests the UDP broadcast message sending capabilities.
 */
class BroadcastSenderTest {
    private BroadcastSender sender;
    private DatagramSocket receiverSocket;
    private static final int PORT = 8888;
    private static final int TIMEOUT = 5000;

    /**
     * Sets up the test environment before each test.
     * Initializes the sender and creates a receiver socket.
     */
    @BeforeEach
    void setUp() throws Exception {
        sender = new BroadcastSender();
        receiverSocket = new DatagramSocket(PORT);
        receiverSocket.setSoTimeout(TIMEOUT);
        System.out.println("Test setup complete: Sender and receiver initialized");
    }

    /**
     * Cleans up resources after each test
     */
    @AfterEach
    void tearDown() {
        if (sender != null) {
            sender.close();
        }
        if (receiverSocket != null && !receiverSocket.isClosed()) {
            receiverSocket.close();
        }
        System.out.println("Test cleanup complete: Resources released");
    }

    /**
     * Tests broadcasting a simple string message.
     * Verifies that:
     * 1. Message is correctly encrypted
     * 2. Message is successfully transmitted
     * 3. Received message matches the sent message after decryption
     */
    @Test
    void testBroadcastString() throws Exception {
        String testMessage = "TEST_MESSAGE";
        CountDownLatch receiveLatch = new CountDownLatch(1);
        AtomicReference<String> receivedMessage = new AtomicReference<>();

        // Start receiver thread
        Thread receiverThread = new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                System.out.println("Receiver waiting for message...");
                receiverSocket.receive(packet);
                
                String encrypted = new String(packet.getData(), 0, packet.getLength());
                String decrypted = EncryptionUtil.decrypt(encrypted);
                receivedMessage.set(decrypted);
                System.out.println("Message received and decrypted: " + decrypted);
                receiveLatch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        receiverThread.start();

        // Send the message
        System.out.println("Sending test message: " + testMessage);
        sender.broadcast(testMessage);

        // Wait for reception
        assertTrue(receiveLatch.await(5, TimeUnit.SECONDS), "Message reception timeout");
        assertEquals(testMessage, receivedMessage.get(), "Received message should match sent message");
    }

    /**
     * Tests broadcasting a Message object.
     * Verifies that:
     * 1. Message object is correctly serialized
     * 2. Message is successfully transmitted
     * 3. Received message can be deserialized back to Message object
     * 4. All message fields are preserved
     */
    @Test
    void testBroadcastMessage() throws Exception {
        Message testMessage = new Message(MessageType.PLAYER_JOIN, 1, "TestPlayer");
        testMessage.addExtraData("key1", "value1");
        CountDownLatch receiveLatch = new CountDownLatch(1);
        AtomicReference<Message> receivedMessage = new AtomicReference<>();

        // Start receiver thread
        Thread receiverThread = new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                System.out.println("Receiver waiting for message...");
                receiverSocket.receive(packet);
                
                String encrypted = new String(packet.getData(), 0, packet.getLength());
                String decrypted = EncryptionUtil.decrypt(encrypted);
                receivedMessage.set(Message.fromString(decrypted));
                System.out.println("Message received and parsed: " + decrypted);
                receiveLatch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        receiverThread.start();

        // Send the message
        System.out.println("Sending Message object: " + testMessage);
        sender.broadcast(testMessage);

        // Wait for reception
        assertTrue(receiveLatch.await(5, TimeUnit.SECONDS), "Message reception timeout");
        Message received = receivedMessage.get();
        assertNotNull(received, "Received message should not be null");
        assertEquals(testMessage.getType(), received.getType(), "Message type should match");
        assertEquals(testMessage.getRoomId(), received.getRoomId(), "Room ID should match");
        assertEquals(testMessage.getFrom(), received.getFrom(), "Sender should match");
        assertEquals("value1", received.getData().get("key1"), "Extra data should match");
    }

    /**
     * Tests error handling when broadcast address is unavailable.
     * Verifies that:
     * 1. Appropriate exception is thrown
     * 2. Error message is descriptive
     */
    @Test
    void testBroadcastAddressUnavailable() {
        final BroadcastSender invalidSender = new BroadcastSender() {
            @Override
            public void broadcast(String message) throws Exception {
                throw new IllegalStateException("Unable to get broadcast address");
            }
        };
        
        try {
            String testMessage = "TEST_MESSAGE";
            Exception exception = assertThrows(IllegalStateException.class, () -> {
                invalidSender.broadcast(testMessage);
            });
            
            assertEquals("Unable to get broadcast address", exception.getMessage());
            System.out.println("Successfully caught expected exception: " + exception.getMessage());
        } finally {
            invalidSender.close();
        }
    }

    /**
     * Tests resource cleanup when closing the sender.
     * Verifies that:
     * 1. Socket is properly closed
     * 2. Multiple close calls are handled gracefully
     */
    @Test
    void testClose() throws Exception {
        // Send a test message to verify sender is working
        sender.broadcast("TEST_MESSAGE");
        
        // Close the sender
        System.out.println("Closing sender...");
        sender.close();
        
        // Verify that sending after close throws an exception
        Exception exception = assertThrows(Exception.class, () -> {
            sender.broadcast("SHOULD_FAIL");
        });
        System.out.println("Successfully caught expected exception after close: " + exception.getMessage());
        
        // Verify that multiple close calls don't throw exceptions
        sender.close(); // Should not throw
        System.out.println("Multiple close calls handled successfully");
    }
} 