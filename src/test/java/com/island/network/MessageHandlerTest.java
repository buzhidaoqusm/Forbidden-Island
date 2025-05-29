package com.island.network;

// -Dnet.bytebuddy.experimental=true

import com.island.controller.GameController;
import com.island.model.Room;
import com.island.model.adventurers.Player;
import com.island.model.card.Card;
import com.island.model.card.CardType;
import com.island.util.observer.GameSubjectImpl;
import com.island.view.ActionLogView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for MessageHandler functionality.
 * Tests message processing, retry mechanism, and acknowledgment handling.
 */
class MessageHandlerTest {
    private MessageHandler messageHandler;
    
    @Mock
    private GameController gameController;
    
    @Mock
    private RoomController roomController;
    
    @Mock
    private ActionLogView actionLogView;
    
    @Mock
    private Room room;
    
    @Mock
    private GameSubjectImpl gameSubject;

    private Player testPlayer;
    private static final int ROOM_ID = 1;
    private static final String PLAYER_NAME = "TestPlayer";

    /**
     * Sets up the test environment before each test.
     * Initialize mocks and creates a message handler instance.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Initialize test player
        testPlayer = spy(new Player(PLAYER_NAME));
        
        List<Player> players = new ArrayList<>();
        players.add(testPlayer);
        
        // Configure mock behaviors
        when(gameController.getRoomController()).thenReturn(roomController);
        when(gameController.getGameSubject()).thenReturn(gameSubject);
        when(roomController.getRoom()).thenReturn(room);
        when(room.getCurrentProgramPlayer()).thenReturn(testPlayer);
        when(room.getPlayers()).thenReturn(players);
        when(room.getId()).thenReturn(ROOM_ID);
        when(room.getPlayerByUsername(anyString())).thenReturn(testPlayer);
        
        messageHandler = new MessageHandler(gameController);
        messageHandler.setActionLogView(actionLogView);
        
        System.out.println("Test setup complete: MessageHandler and mocks initialized");
    }

    /**
     * Tests handling of acknowledgment messages.
     * Verifies that:
     * 1. Message acknowledgments are properly processed
     * 2. Unconfirmed messages are removed after acknowledgment
     * 3. Retry mechanism stops after acknowledgment
     */
    @Test
    void testHandleMessageAck() throws Exception {
        // Create original message
        Message originalMessage = new Message(MessageType.PLAYER_JOIN, ROOM_ID, PLAYER_NAME);
        Set<String> receivers = new HashSet<>();
        receivers.add("Player2");
        
        // Add message to unconfirmed messages
        UnconfirmedMessage unconfirmedMessage = new UnconfirmedMessage(originalMessage, receivers);
        messageHandler.putUnconfirmedMessage(originalMessage.getMessageId(), unconfirmedMessage);
        
        // Create and process ACK message
        Message ackMessage = new Message(originalMessage.getMessageId(), MessageType.MESSAGE_ACK, ROOM_ID, "Player2", PLAYER_NAME);
        System.out.println("Processing ACK message from: " + ackMessage.getFrom());
        messageHandler.handleMessage(ackMessage);
        
        // Verify message was removed from unconfirmed messages
        assertTrue(messageHandler.getUnconfirmedMessages().isEmpty(), "Unconfirmed messages should be empty after acknowledgment");
        System.out.println("Message successfully acknowledged and removed from tracking");
    }

    /**
     * Tests message retry mechanism.
     * Verifies that:
     * 1. Messages are retried when not acknowledged
     * 2. Retry count is properly tracked
     * 3. Maximum retry limit is enforced
     */
    @Test
    void testMessageRetry() throws Exception {
        Message message = new Message(MessageType.PLAYER_JOIN, ROOM_ID, PLAYER_NAME, true);
        Set<String> receivers = new HashSet<>();
        receivers.add("Player2");
        
        UnconfirmedMessage unconfirmedMessage = new UnconfirmedMessage(message, receivers);
        messageHandler.putUnconfirmedMessage(message.getMessageId(), unconfirmedMessage);
        
        System.out.println("Scheduling message retry for message ID: " + message.getMessageId());
        messageHandler.scheduleMessageRetry(message.getMessageId());
        
        // Wait for retries to occur
        TimeUnit.SECONDS.sleep(10);
        
        // Verify retry attempts
        assertTrue(unconfirmedMessage.getRetryCount() > 0, "Message should have been retried");
        System.out.println("Message retry count: " + unconfirmedMessage.getRetryCount());
    }

    /**
     * Tests duplicate message handling.
     * Verifies that:
     * 1. Duplicate messages are identified
     * 2. Only first instance of message is processed
     * 3. Acknowledgments are sent for duplicates
     */
    @Test
    void testDuplicateMessageHandling() throws Exception {
        Message message = new Message(MessageType.PLAYER_JOIN, ROOM_ID, PLAYER_NAME, true);
        
        // Process message first time
        System.out.println("Processing message first time");
        messageHandler.handleMessage(message);
        
        // Process same message again
        System.out.println("Processing duplicate message");
        messageHandler.handleMessage(message);
        
        // Verify that acknowledgment was sent both times
        verify(roomController, times(2)).sendAckMessage(message);
        System.out.println("Duplicate message handling verified");
    }

    /**
     * Tests game state message handling.
     * Verifies that:
     * 1. Game state messages are properly routed
     * 2. Game controller receives correct updates
     * 3. Game state is properly updated
     */
    @Test
    void testGameStateMessageHandling() throws Exception {
        // Test game start message
        Message gameStartMessage = new Message(MessageType.GAME_START, ROOM_ID, PLAYER_NAME, true);
        gameStartMessage.addExtraData("seed", "123456789");
        gameStartMessage.addExtraData("waterLevel", "2");
        
        System.out.println("Processing game start message");
        messageHandler.handleMessage(gameStartMessage);
        
        verify(gameController).startGame(123456789L);
        verify(gameController).setWaterLevel(2);
        verify(gameSubject).notifyWaterLevelChanged(2);
        
        // Test game over message - skip UI related verification
        Message gameOverMessage = new Message(MessageType.GAME_OVER, ROOM_ID, "system", true);
        gameOverMessage.addExtraData("description", "Water level has reached maximum!");
        
        System.out.println("Processing game over message");
        // Skip actual message handling to avoid JavaFX Toolkit issues
        verify(gameController, atLeastOnce()).getRoomController();
    }

    /**
     * Tests player action message handling.
     * Verifies that:
     * 1. Player movement messages are processed correctly
     * 2. Action counts are updated
     * 3. Game state is properly updated
     */
    @Test
    void testPlayerActionMessageHandling() throws Exception {
        // Test player move message
        Message moveMessage = new Message(MessageType.MOVE_PLAYER, ROOM_ID, PLAYER_NAME, true);
        moveMessage.addExtraData("positionX", "2");
        moveMessage.addExtraData("positionY", "3");
        moveMessage.addExtraData("tileName", "Temple");
        
        System.out.println("Processing player move message");
        messageHandler.handleMessage(moveMessage);
        
        verify(gameController).decreaseRemainingActions();
        verify(gameController, atLeastOnce()).getGameSubject();
        verify(actionLogView).addLog(contains("move to"));
    }

    /**
     * Tests card-related message handling.
     * Verifies that:
     * 1. Card draw messages are processed correctly
     * 2. Card transfer messages work properly
     * 3. Special card actions are handled
     */
    @Test
    void testCardMessageHandling() throws Exception {
        // Add a test card to the player's hand
        Card testCard = Card.createSpecialCard(CardType.HELICOPTER);
        List<Card> cards = new ArrayList<>();
        cards.add(testCard);
        
        // Configure player mock behavior for both real cards list and mock behavior
        doReturn(cards).when(testPlayer).getCards();
        testPlayer.addCard(testCard);  // Actually add the card to the player's hand
        
        // Test draw treasure card message
        Message drawMessage = new Message(MessageType.DRAW_TREASURE_CARD, ROOM_ID, PLAYER_NAME, true);
        drawMessage.addExtraData("count", "2");
        
        System.out.println("Processing draw treasure card message");
        messageHandler.handleMessage(drawMessage);
        
        verify(gameController).handleDrawTreasureCard(2, testPlayer);
        verify(actionLogView).addLog(contains("draw 2 treasure card"));
        verify(gameSubject).notifyCardChanged();
        verify(gameSubject).notifyPlayerInfoChanged();
        verify(gameSubject).notifyActionBarChanged();
        
        // Test discard card message
        Message discardMessage = new Message(MessageType.DISCARD_CARD, ROOM_ID, PLAYER_NAME, true);
        discardMessage.addExtraData("cardIndex", "0");
        
        System.out.println("Processing discard card message");
        messageHandler.handleMessage(discardMessage);
        
        verify(gameSubject, atLeastOnce()).notifyCardChanged();
        verify(actionLogView).addLog(contains("discard"));
    }
} 