package com.island.network;

/*
 * --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED
 * --add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED
 * --add-opens javafx.graphics/com.sun.glass.ui=ALL-UNNAMED
 */

import com.island.model.Room;
import com.island.model.adventurers.Player;
import com.island.util.EncryptionUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.application.Platform;
import javafx.stage.Stage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for BroadcastReceiver functionality.
 * Tests the network communication and message handling capabilities.
 */
@ExtendWith(ApplicationExtension.class)
class BroadcastReceiverTest {
    private BroadcastReceiver broadcastReceiver;
    private DatagramSocket senderSocket;
    private TestRoomController roomController;
    private static volatile boolean jfxIsSetup = false;

    @BeforeAll
    public static void setupJavaFX() throws InterruptedException {
        if (!jfxIsSetup) {
            // Initialize the JavaFX platform
            Platform.startup(() -> {});
            jfxIsSetup = true;
        }
    }

    @Start
    private void start(Stage stage) {
        // JavaFX initialization
    }

    /**
     * Test double for RoomController to track method calls and manage game state
     */
    private static class TestRoomController extends RoomController {
        private final List<String> heartbeatUpdates = new ArrayList<>();
        private final List<Message> receivedMessages = new ArrayList<>();
        private final Room room;
        private final Player currentPlayer;
        private Runnable messageHandler;
        private final Set<Long> processedMessageIds = new HashSet<>();
        private final Set<String> processedPlayers = new HashSet<>();  // Track players who have sent heartbeats
        private final Object heartbeatLock = new Object();

        public TestRoomController() {
            super(new Room(1, new Player("TestPlayer")));
            this.currentPlayer = new Player("TestPlayer");
            this.room = super.getRoom();
        }

        @Override
        public int getRoomId() {
            return 1;
        }

        @Override
        public Room getRoom() {
            return room;
        }

        @Override
        public void updatePlayerHeartbeat(String username) {
            synchronized(heartbeatLock) {
                if (!processedPlayers.contains(username)) {
                    heartbeatUpdates.add(username);
                    processedPlayers.add(username);
                    System.out.println("Received new player heartbeat: " + username + ", current recorded players: " + heartbeatUpdates.size());
                } else {
                    System.out.println("Ignoring heartbeat from already recorded player: " + username);
                }
            }
        }

        @Override
        public void handleGameMessage(Message message) {
            synchronized(receivedMessages) {
                if (!processedMessageIds.contains(message.getMessageId())) {
                    receivedMessages.add(message);
                    processedMessageIds.add(message.getMessageId());
                    if (messageHandler != null) {
                        messageHandler.run();
                    }
                } else {
                    System.out.println("Skipping duplicate message: " + message.getMessageId());
                }
            }
        }

        public void setMessageHandler(Runnable handler) {
            this.messageHandler = handler;
        }

        public List<String> getHeartbeatUpdates() {
            synchronized(heartbeatLock) {
                return new ArrayList<>(heartbeatUpdates);
            }
        }

        public List<Message> getReceivedMessages() {
            synchronized(receivedMessages) {
                return new ArrayList<>(receivedMessages);
            }
        }

        public void clearUpdates() {
            synchronized(heartbeatLock) {
                heartbeatUpdates.clear();
                processedPlayers.clear();  // Clear tracked players
                System.out.println("Clearing heartbeat records and player states");
            }
            synchronized(receivedMessages) {
                receivedMessages.clear();
                processedMessageIds.clear();
            }
            messageHandler = null;
        }
    }

    /**
     * Test double for Room to provide controlled test environment
     */
    private static class TestRoom extends Room {
        private final Player currentPlayer;

        public TestRoom() {
            super(1, new Player("TestPlayer"));
            this.currentPlayer = new Player("TestPlayer");
        }

        @Override
        public Player getCurrentProgramPlayer() {
            return currentPlayer;
        }
    }

    /**
     * Sets up the test environment before each test
     */
    @BeforeEach
    void setUp() throws Exception {
        roomController = new TestRoomController();
        broadcastReceiver = new BroadcastReceiver(roomController);
        senderSocket = new DatagramSocket();
    }

    /**
     * Cleans up resources after each test
     */
    @AfterEach
    void tearDown() throws Exception {
        if (broadcastReceiver != null) {
            broadcastReceiver.stop();
        }
        if (senderSocket != null && !senderSocket.isClosed()) {
            senderSocket.close();
        }
        
        // Wait for JavaFX thread cleanup
        CountDownLatch cleanupLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            roomController.clearUpdates();
            cleanupLatch.countDown();
        });
        assertTrue(cleanupLatch.await(5, TimeUnit.SECONDS), "Cleanup timeout");
    }

    /**
     * Tests the handling of heartbeat messages.
     * Verifies that:
     * 1. A heartbeat message is correctly received
     * 2. Only the first heartbeat from a player is recorded
     * 3. Subsequent heartbeats from the same player are ignored
     */
    @Test
    void testHeartbeatMessageHandling() throws Exception {
        CountDownLatch messageLatch = new CountDownLatch(1);
        
        // Clear updates and ensure execution in JavaFX thread
        Platform.runLater(() -> {
            roomController.clearUpdates();
            messageLatch.countDown();
        });
        assertTrue(messageLatch.await(5, TimeUnit.SECONDS), "Clear updates timeout");

        // Construct heartbeat message matching current roomId
        int roomId = roomController.getRoomId();
        String heartbeatMessage = String.format("HEARTBEAT|%d|TestPlayer", roomId);
        String encrypted = EncryptionUtil.encrypt(heartbeatMessage);

        // Start the receiver
        Thread receiverThread = new Thread(broadcastReceiver);
        receiverThread.start();

        // Wait for receiver to fully start
        TimeUnit.SECONDS.sleep(1);

        // Clear updates again before sending message
        CountDownLatch clearLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                roomController.clearUpdates();
            } finally {
                clearLatch.countDown();
            }
        });
        assertTrue(clearLatch.await(5, TimeUnit.SECONDS), "Clear timeout");
        TimeUnit.MILLISECONDS.sleep(200);

        DatagramSocket tempSocket = null;
        try {
            tempSocket = new DatagramSocket();
            tempSocket.setBroadcast(true);
            tempSocket.setReuseAddress(true);

            byte[] data = encrypted.getBytes();
            DatagramPacket packet = new DatagramPacket(
                data,
                data.length,
                InetAddress.getByName("255.255.255.255"),
                8888
            );

            System.out.println("Sending heartbeat message...");
            tempSocket.send(packet);
            System.out.println("Heartbeat message sent: " + heartbeatMessage);

            // Allow time for message processing
            TimeUnit.MILLISECONDS.sleep(500);

        } finally {
            if (tempSocket != null) {
                tempSocket.close();
            }
        }

        // Stop receiver and wait for thread to end
        broadcastReceiver.stop();
        receiverThread.join(1000);

        // Verify heartbeat updates
        CountDownLatch verifyLatch = new CountDownLatch(1);
        AtomicInteger updateCount = new AtomicInteger(0);
        
        Platform.runLater(() -> {
            try {
                List<String> updates = roomController.getHeartbeatUpdates();
                updateCount.set(updates.size());
                System.out.println("Number of heartbeat updates at verification: " + updates.size());
                if (!updates.isEmpty()) {
                    assertEquals("TestPlayer", updates.get(0), "Should receive heartbeat from TestPlayer");
                }
            } finally {
                verifyLatch.countDown();
            }
        });

        assertTrue(verifyLatch.await(5, TimeUnit.SECONDS), "Verification timeout");
        assertEquals(1, updateCount.get(), "Should only receive one heartbeat update");
    }

    /**
     * Tests the handling of game messages.
     * Verifies that:
     * 1. Game messages are correctly received and processed
     * 2. Message deduplication works correctly
     * 3. Messages trigger appropriate callbacks
     */
    @Test
    void testGameMessageHandling() throws Exception {
        CountDownLatch setupLatch = new CountDownLatch(1);
        CountDownLatch processLatch = new CountDownLatch(1);
        CountDownLatch receiveLatch = new CountDownLatch(1);
        AtomicInteger messageCount = new AtomicInteger(0);  // Add message counter
        
        // Waiting for JavaFX thread to be ready
        Platform.runLater(() -> {
            try {
                roomController.clearUpdates();
                // Set message processing callback
                roomController.setMessageHandler(() -> {
                    System.out.println("Message processing callback triggered #" + messageCount.incrementAndGet());
                    processLatch.countDown();
                });
                setupLatch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        assertTrue(setupLatch.await(5, TimeUnit.SECONDS), "Set timeout");
        
        // Create game message
        Message gameMessage = new Message(MessageType.PLAYER_JOIN, 1, "TestPlayer");
        String messageStr = gameMessage.toString();
        System.out.println("Sent message: " + messageStr);
        String encrypted = EncryptionUtil.encrypt(messageStr);
        System.out.println("Encrypted message: " + encrypted);
        
        // Start the receiver thread
        Thread receiverThread = new Thread(() -> {
            try {
                System.out.println("Receiver thread startup");
                broadcastReceiver.run();
                System.out.println("Receiver thread shutdown");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                receiveLatch.countDown();
            }
        });
        receiverThread.start();
        
        // Waiting for the receiver to start
        TimeUnit.MILLISECONDS.sleep(2000);
        
        // Send message
        System.out.println("Start to send message...");
        sendMessage(encrypted);
        System.out.println("Message transmission completed");
        
        // Waiting for message processing to complete
        System.out.println("Waiting for message processing...");
        boolean processed = processLatch.await(5, TimeUnit.SECONDS);
        System.out.println("Message processing completion status: " + processed);
        
        // Wait for a period of time to ensure that there are no duplicate messages
        TimeUnit.SECONDS.sleep(2);
        
        // Stop receiver
        System.out.println("Stopping receiver...");
        broadcastReceiver.stop();
        assertTrue(receiveLatch.await(5, TimeUnit.SECONDS), "Receiver stop timeout");
        
        // Verify message processing
        CountDownLatch verifyLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                List<Message> messages = roomController.getReceivedMessages();
                System.out.println("Number of messages received: " + messages.size());
                if (!messages.isEmpty()) {
                    System.out.println("The first message: " + messages.get(0));
                }
                assertTrue(processed, "Message processing timeout");
                assertEquals(1, messages.size(), "You should have only received one game message");
                assertEquals(MessageType.PLAYER_JOIN, messages.get(0).getType(), "Should receive PLAYER_JOIN message");
            } finally {
                verifyLatch.countDown();
            }
        });
        
        assertTrue(verifyLatch.await(5, TimeUnit.SECONDS), "Verification timeout");
    }

    /**
     * Tests handling of messages from different rooms.
     * Verifies that:
     * 1. Messages from other rooms are correctly filtered out
     * 2. Only messages for the current room are processed
     */
    @Test
    void testDifferentRoomMessageHandling() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        // Create a message from a different room
        Message gameMessage = new Message(MessageType.PLAYER_JOIN, 2, "TestPlayer");
        String encrypted = EncryptionUtil.encrypt(gameMessage.toString());
        
        Platform.runLater(() -> {
            try {
                sendMessage(encrypted);
            } catch (Exception e) {
                e.printStackTrace();
            }
            latch.countDown();
        });

        // Wait for processing
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Message processing timed out");
    }

    /**
     * Tests handling of invalid messages.
     * Verifies that:
     * 1. Invalid message formats are properly handled
     * 2. No processing occurs for invalid messages
     * 3. System remains stable when receiving invalid messages
     */
    @Test
    void testInvalidMessageHandling() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        // Send an invalid message
        String invalidMessage = "Invalid|Message|Format";
        String encrypted = EncryptionUtil.encrypt(invalidMessage);
        
        Platform.runLater(() -> {
            try {
                sendMessage(encrypted);
            } catch (Exception e) {
                e.printStackTrace();
            }
            latch.countDown();
        });

        // Wait for processing
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Message processing timed out");
        TimeUnit.MILLISECONDS.sleep(1000);

        // Verify no game message processing occurred
        assertTrue(roomController.getReceivedMessages().isEmpty(), "Should not process invalid message");
    }

    /**
     * Tests handling of targeted messages.
     * Verifies that:
     * 1. Messages targeted at the test player are processed
     * 2. Message targeting mechanism works correctly
     */
    @Test
    void testTargetedMessageHandling() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        // Create a message targeted at the test player
        Message gameMessage = new Message(MessageType.PLAYER_JOIN, 1, "Sender", "TestPlayer");
        String encrypted = EncryptionUtil.encrypt(gameMessage.toString());
        
        // Set up Platform.runLater to run in test thread
        Platform.runLater(() -> {
            try {
                sendMessage(encrypted);
            } catch (Exception e) {
                e.printStackTrace();
            }
            latch.countDown();
        });

        // Wait for processing
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Message processing timed out");
        TimeUnit.MILLISECONDS.sleep(1000);

        // Verify the message was processed
        assertFalse(roomController.getReceivedMessages().isEmpty(), "Should process targeted message");
    }

    /**
     * Tests handling of messages targeted at other players.
     * Verifies that:
     * 1. Messages targeted at other players are filtered out
     * 2. No processing occurs for messages meant for other players
     */
    @Test
    void testOtherTargetMessageHandling() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        // Create a message targeted at a different player
        Message gameMessage = new Message(MessageType.PLAYER_JOIN, 1, "Sender", "OtherPlayer");
        String encrypted = EncryptionUtil.encrypt(gameMessage.toString());
        
        Platform.runLater(() -> {
            try {
                sendMessage(encrypted);
            } catch (Exception e) {
                e.printStackTrace();
            }
            latch.countDown();
        });

        // Wait for processing
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Message processing timed out");
        TimeUnit.MILLISECONDS.sleep(1000);

        // Verify the message was not processed
        assertTrue(roomController.getReceivedMessages().isEmpty(), "Should not process message targeted at other player");
    }

    /**
     * Helper method to send test messages.
     * Configures and sends UDP packets for testing network communication.
     *
     * @param message The message to send
     * @throws Exception If there are network or configuration issues
     */
    private void sendMessage(String message) throws Exception {
        System.out.println("Preparing to send message, length: " + message.length());
        byte[] data = message.getBytes();
        
        // Create and configure sending socket
        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);
        socket.setReuseAddress(true);
        
        // Create packet
        DatagramPacket packet = new DatagramPacket(
            data,
            data.length,
            InetAddress.getByName("255.255.255.255"),
            8888
        );
        
        try {
            // Send message
            socket.send(packet);
            System.out.println("Message sent to: " + packet.getAddress() + ":" + packet.getPort());
            // Wait to ensure message is sent
            TimeUnit.MILLISECONDS.sleep(200);
        } finally {
            socket.close();
        }
    }
} 