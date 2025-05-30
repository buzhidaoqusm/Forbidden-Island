package com.island.controller;

import com.island.models.Player;
import com.island.models.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * ActionBarController 的单元测试，覆盖所有方法，使用 Mockito 进行依赖mock。
 */
public class ActionBarControllerTest {
    @Mock
    private GameController gameController;
    @Mock
    private Player player;
    @Mock
    private Room room;

    private ActionBarController actionBarController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        actionBarController = new ActionBarController();
        when(gameController.getCurrentPlayer()).thenReturn(player);
        when(gameController.getRoom()).thenReturn(room);
        actionBarController.setGameController(gameController);
    }

    /**
     * 测试 set/getCurrentPlayer
     */
    @Test
    void testSetGetCurrentPlayer() {
        actionBarController.setCurrentPlayer(player);
        assertEquals(player, actionBarController.getCurrentPlayer());
    }

    /**
     * 测试 getRemainingActions
     */
    @Test
    void testGetRemainingActions() {
        when(gameController.getRemainingActions()).thenReturn(2);
        assertEquals(2, actionBarController.getRemainingActions());
    }

    /**
     * 测试 getRoom
     */
    @Test
    void testGetRoom() {
        assertEquals(room, actionBarController.getRoom());
    }

    /**
     * 测试 getGameController
     */
    @Test
    void testGetGameController() {
        assertEquals(gameController, actionBarController.getGameController());
    }

    /**
     * 测试 canPlaySpecialCard
     */
    @Test
    void testCanPlaySpecialCard() {
        when(gameController.getPlayerController()).thenReturn(mock(PlayerController.class));
        when(gameController.getPlayerController().canPlaySpecialCard(player)).thenReturn(true);
        assertTrue(actionBarController.canPlaySpecialCard(player));
    }

    /**
     * 测试 canShoreUpTile
     */
    @Test
    void testCanShoreUpTile() {
        when(gameController.getPlayerController()).thenReturn(mock(PlayerController.class));
        when(gameController.getPlayerController().canShoreUpTile(player)).thenReturn(true);
        assertTrue(actionBarController.canShoreUpTile(player));
    }

    /**
     * 测试 canGiveCard
     */
    @Test
    void testCanGiveCard() {
        when(gameController.getPlayerController()).thenReturn(mock(PlayerController.class));
        when(gameController.getPlayerController().canGiveCard(player)).thenReturn(true);
        assertTrue(actionBarController.canGiveCard(player));
    }

    /**
     * 测试 canCaptureTreasure
     */
    @Test
    void testCanCaptureTreasure() {
        when(gameController.getPlayerController()).thenReturn(mock(PlayerController.class));
        when(gameController.getPlayerController().canCaptureTreasure(player)).thenReturn(true);
        assertTrue(actionBarController.canCaptureTreasure(player));
    }

    /**
     * 测试 hasDrawnTreasureCards
     */
    @Test
    void testHasDrawnTreasureCards() {
        when(gameController.getPlayerController()).thenReturn(mock(PlayerController.class));
        when(gameController.getPlayerController().hasDrawnTreasureCards()).thenReturn(true);
        assertTrue(actionBarController.hasDrawnTreasureCards());
    }

    /**
     * 测试 getDrawnFloodCards
     */
    @Test
    void testGetDrawnFloodCards() {
        when(gameController.getPlayerController()).thenReturn(mock(PlayerController.class));
        when(gameController.getPlayerController().getDrawnFloodCards()).thenReturn(2);
        assertEquals(2, actionBarController.getDrawnFloodCards());
    }

    /**
     * 测试 setHasDrawnTreasureCards
     */
    @Test
    void testSetHasDrawnTreasureCards() {
        when(gameController.getPlayerController()).thenReturn(mock(PlayerController.class));
        doNothing().when(gameController.getPlayerController()).setHasDrawnTreasureCards(true);
        actionBarController.setHasDrawnTreasureCards(true);
        verify(gameController.getPlayerController()).setHasDrawnTreasureCards(true);
    }
}
