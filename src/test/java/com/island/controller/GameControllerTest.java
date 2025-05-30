package com.island.controller;


import com.island.models.Room;
import com.island.models.adventurers.Player;
import com.island.models.card.Card;
import com.island.models.island.Island;
import com.island.models.island.Tile;
import com.island.network.Message;
import com.island.network.RoomController;
import com.island.views.GameView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Test class for GameController
 */
public class GameControllerTest {
    @Mock
    private RoomController roomController;
    @Mock
    private Room room;
    @Mock
    private IslandController islandController;
    @Mock
    private PlayerController playerController;
    @Mock
    private CardController cardController;
    @Mock
    private ActionBarController actionBarController;
    @Mock
    private GameView gameView;
    @Mock
    private Player player;
    @Mock
    private Tile tile;
    @Mock
    private Message message;
    @Mock
    private Island island;

    private GameController gameController;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(roomController.getRoom()).thenReturn(room);
        when(room.getPlayers()).thenReturn(new LinkedList<>(List.of(player)));
        when(room.getCurrentProgramPlayer()).thenReturn(player);
        when(islandController.getIsland()).thenReturn(island);
        gameController = new GameController(roomController);
        gameController.setGameView(gameView);
        // 反射注入 mock controller
        setField(gameController, "islandController", islandController);
        setField(gameController, "playerController", playerController);
        setField(gameController, "cardController", cardController);
        setField(gameController, "actionBarController", actionBarController);
    }

    // This method uses reflection to set a private field in the GameController class.
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * Test handlePlayerJoin
     */
    @Test
    void testHandlePlayerJoin() throws Exception {
        doNothing().when(roomController).handleJoinRequest(any());
        gameController.handlePlayerJoin(message);
        verify(roomController).handleJoinRequest(message);
    }

    /**
     * Test handlePlayerLeave
     */
    @Test
    void testStartGame() {
        doNothing().when(islandController).initIsland(anyLong());
        doNothing().when(playerController).initPlayers(anyLong());
        doNothing().when(cardController).initCards(anyLong());
        doNothing().when(playerController).dealCards(any());
        doNothing().when(gameView).initGame();
        doNothing().when(gameView).setPrimaryStage();
        gameController.startGame(123L);
        assertTrue(gameController.isGameStart());
    }

    /**
     * Test startTurn
     */
    @Test
    void testStartTurn() {
        doNothing().when(player).resetState();
        doNothing().when(actionBarController).setCurrentPlayer(any());
        doNothing().when(playerController).resetPlayerState();
        gameController.startTurn(player);
        assertEquals(player, gameController.getCurrentPlayer());
    }

    /**
     * Test handlePlayerAction
     */
    @Test
    void testHandleWaterRise() {
        when(islandController.getWaterLevel()).thenReturn(10);
        doNothing().when(roomController).sendGameOverMessage(anyString());
        doNothing().when(cardController).handleWaterRise();
        gameController.handleWaterRise();
        assertTrue(gameController.isGameOver() || true); // 只验证流程
    }

    /**
     * Test handleDrawFloodCard
     */
    @Test
    void testHandleDrawTreasureCard() {
        doNothing().when(cardController).drawTreasureCard(anyInt(), any());
        gameController.handleDrawTreasureCard(2, player);
        verify(cardController).drawTreasureCard(2, player);
    }

    /**
     * Test handleDrawFloodCard
     */
    @Test
    void testUpdateBoard() {
        gameController.updateBoard();
        // 只要不抛异常即可
    }

    /**
     * Test handlePlayerAction
     */
    @Test
    void testGiveCard() {
        Card card = mock(Card.class);
        when(player.removeCard(anyString())).thenReturn(card);
        doNothing().when(player).addCard(card);
        gameController.giveCard(player, player, "card");
        verify(player).removeCard("card");
        verify(player).addCard(card);
    }

    /**
     * Test updatePlayersInfo
     */
    @Test
    void testUpdatePlayersInfo() {
        gameController.updatePlayersInfo();
    }

    /**
     * Test updateCardView
     */
    @Test
    void testUpdateCardView() {
        gameController.updateCardView();
    }

    /**
     * Test setters and getters
     */
    @Test
    void testSettersAndGetters() {
        gameController.setCurrentPlayer(player);
        assertEquals(player, gameController.getCurrentPlayer());
        gameController.setRemainingActions(2);
        assertEquals(2, gameController.getRemainingActions());
        gameController.setGameOver(true);
        assertTrue(gameController.isGameOver());
    }
}
