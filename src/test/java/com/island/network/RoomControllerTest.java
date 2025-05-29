/**
 * To run this test class, the following JVM parameters need to be added:
 * (The current version of ByteBuddy does not support Java 23 by default)
 * --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED
 * --add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED
 * --add-opens javafx.graphics/com.sun.glass.ui=ALL-UNNAMED
 * --add-exports javafx.graphics/com.sun.glass.ui=ALL-UNNAMED
 * -Dnet.bytebuddy.experimental=true
 * -Djdk.module.illegalAccess=deny
 */
package com.island.network;

import com.island.model.Room;
import com.island.model.adventurers.Player;
import com.island.model.card.Card;
import com.island.model.island.Island;
import com.island.model.island.Position;
import com.island.model.island.Tile;
import com.island.model.treasure.TreasureType;
import com.island.controller.GameController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.api.FxToolkit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Test classes for RoomController
 * Use the Application Extension provided by TestFX to manage the JavaFX environment
 */
@ExtendWith(ApplicationExtension.class)
class RoomControllerTest {
    private RoomController roomController;

    @Mock
    private Room room;
    
    @Mock
    private MessageHandler messageHandler;
    
    @Mock
    private Island island;
    
    @Mock
    private Player player;
    
    @Mock
    private Tile tile;
    
    @Mock
    private GameController gameController;

    @BeforeAll
    public static void setupClass() throws Exception {
        // Register the main stage using the methods provided by TestFX
        FxToolkit.registerPrimaryStage();
    }

    @BeforeEach
    void setUp() throws Exception {
        // Initialize  mocks
        MockitoAnnotations.openMocks(this);
        
        // Set basic mock behavior
        when(room.getId()).thenReturn(1);
        when(room.getCurrentProgramPlayer()).thenReturn(player);
        when(room.getHostPlayer()).thenReturn(player);
        when(player.getName()).thenReturn("TestPlayer");
        when(gameController.isGameStart()).thenReturn(false);

        // Manually injecting dependencies
        roomController = new RoomController(room);
        roomController.setMessageHandler(messageHandler);
        roomController.setIsland(island);
        roomController.setGameController(gameController);
        
        // Configure default message processing behavior
        doNothing().when(messageHandler).putUnconfirmedMessage(anyLong(), any());
        doNothing().when(messageHandler).scheduleMessageRetry(anyLong());
    }

    /**
     * Test handling join request when room is not full
     */
    @Test
    void testHandleJoinRequest() throws Exception {
        // Setup
        String newPlayerName = "NewPlayer";
        Message joinMessage = new Message(MessageType.PLAYER_JOIN, room.getId(), newPlayerName, true);  // Settings need to be confirmed
        joinMessage.addExtraData("isRequest", true);
        
        // Simulate room status
        List<Player> players = new ArrayList<>();
        players.add(player);
        when(room.getPlayers()).thenReturn(players);
        when(room.isHost("TestPlayer")).thenReturn(true);
        when(room.getCurrentProgramPlayer().getName()).thenReturn("TestPlayer");
        when(gameController.isGameStart()).thenReturn(false);

        // Simulate message sending
        doNothing().when(messageHandler).putUnconfirmedMessage(anyLong(), any());
        when(messageHandler.getUnconfirmedMessages()).thenReturn(new ConcurrentHashMap<>());

        // Execute
        roomController.broadcast(joinMessage);

        // Verify
        verify(messageHandler).putUnconfirmedMessage(anyLong(), any());
    }

    /**
     * Test heartbeat update
     */
    @Test
    void testUpdatePlayerHeartbeat() {
        // Execute
        roomController.updatePlayerHeartbeat("TestPlayer");
    }

    /**
     * Test sending treasure cards message
     */
    @Test
    void testSendDrawTreasureCardsMessage() throws Exception {
        // Execute
        roomController.sendDrawTreasureCardsMessage(2, player);

        // Verify
        verify(messageHandler).putUnconfirmedMessage(anyLong(), any());
    }

    /**
     * Test sending move message
     */
    @Test
    void testSendMoveMessage() throws Exception {
        // Setup
        Position position = new Position(1, 1);
        when(island.getTile(position)).thenReturn(tile);
        when(tile.getName()).thenReturn("TestTile");

        // Execute
        roomController.sendMoveMessage(player, position);

        // Verify
        verify(messageHandler).putUnconfirmedMessage(anyLong(), any());
    }

    /**
     * Test sending shore up message
     */
    @Test
    void testSendShoreUpMessage() throws Exception {
        // Setup
        Position position = new Position(1, 1);
        when(island.getTile(position)).thenReturn(tile);
        when(tile.getName()).thenReturn("TestTile");

        // Execute
        roomController.sendShoreUpMessage(player, position);

        // Verify
        verify(messageHandler).putUnconfirmedMessage(anyLong(), any());
    }

    /**
     * Test sending give card message
     */
    @Test
    void testSendGiveCardMessage() throws Exception {
        // Setup
        Player receiver = mock(Player.class);
        Card card = mock(Card.class);
        when(card.getName()).thenReturn("TestCard");
        when(receiver.getName()).thenReturn("Receiver");

        // Execute
        roomController.sendGiveCardMessage(player, receiver, card);

        // Verify
        verify(messageHandler).putUnconfirmedMessage(anyLong(), any());
    }

    /**
     * Test sending helicopter move message
     */
    @Test
    void testSendHelicopterMoveMessage() throws Exception {
        // Setup
        Position position = new Position(1, 1);
        List<Player> players = new ArrayList<>();
        players.add(player);
        when(island.getTile(position)).thenReturn(tile);
        when(tile.getName()).thenReturn("TestTile");

        // Execute
        roomController.sendHelicopterMoveMessage(players, player, position, 0);

        // Verify
        verify(messageHandler).putUnconfirmedMessage(anyLong(), any());
    }

    /**
     * Test sending capture treasure message
     */
    @Test
    void testSendCaptureTreasureMessage() throws Exception {
        // Execute
        roomController.sendCaptureTreasureMessage(player, TreasureType.EARTH_STONE);

        // Verify
        verify(messageHandler).putUnconfirmedMessage(anyLong(), any());
    }

    /**
     * Test sending game over message
     */
    @Test
    void testSendGameOverMessage() throws Exception {
        // Execute
        roomController.sendGameOverMessage("Game Over!");

        // Verify
        verify(messageHandler).putUnconfirmedMessage(anyLong(), any());
    }

    /**
     * Test sending start game message
     */
    @Test
    void testSendStartGameMessage() throws Exception {
        // Setup
        AtomicInteger waterLevel = new AtomicInteger(2);

        // Execute
        roomController.sendStartGameMessage(player, waterLevel);

        // Verify
        verify(messageHandler).putUnconfirmedMessage(anyLong(), any());
    }

    /**
     * Test shutdown
     */
    @Test
    void testShutdown() {
        // Execute
        roomController.shutdown();
    }
} 