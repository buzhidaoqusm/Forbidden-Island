package com.island.controller;

import com.island.models.Room;
import com.island.models.adventurers.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * The ActionBarControllerTest class tests the functionality of the ActionBarController
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
     * Test set/getCurrentPlayer
     */
    @Test
    void testSetGetCurrentPlayer() {
        actionBarController.setCurrentPlayer(player);
        assertEquals(player, actionBarController.getCurrentPlayer());
    }

    /**
     * Test getRemainingActions
     */
    @Test
    void testGetRemainingActions() {
        when(gameController.getRemainingActions()).thenReturn(2);
        assertEquals(2, actionBarController.getRemainingActions());
    }

    /**
     * Test getRoom
     */
    @Test
    void testGetRoom() {
        assertEquals(room, actionBarController.getRoom());
    }

    /**
     * Test getGameController
     */
    @Test
    void testGetGameController() {
        assertEquals(gameController, actionBarController.getGameController());
    }

    /**
     * Test canPlaySpecialCard
     */
    @Test
    void testCanPlaySpecialCard() {
        when(gameController.getPlayerController()).thenReturn(mock(PlayerController.class));
        when(gameController.getPlayerController().canPlaySpecialCard(player)).thenReturn(true);
        assertTrue(actionBarController.canPlaySpecialCard(player));
    }

    /**
     * Test canShoreUpTile
     */
    @Test
    void testCanShoreUpTile() {
        when(gameController.getPlayerController()).thenReturn(mock(PlayerController.class));
        when(gameController.getPlayerController().canShoreUpTile(player)).thenReturn(true);
        assertTrue(actionBarController.canShoreUpTile(player));
    }

    /**
     * Test canGiveCard
     */
    @Test
    void testCanGiveCard() {
        when(gameController.getPlayerController()).thenReturn(mock(PlayerController.class));
        when(gameController.getPlayerController().canGiveCard(player)).thenReturn(true);
        assertTrue(actionBarController.canGiveCard(player));
    }

    /**
     * Test canCaptureTreasure
     */
    @Test
    void testCanCaptureTreasure() {
        when(gameController.getPlayerController()).thenReturn(mock(PlayerController.class));
        when(gameController.getPlayerController().canCaptureTreasure(player)).thenReturn(true);
        assertTrue(actionBarController.canCaptureTreasure(player));
    }

    /**
     * Test hasDrawnTreasureCards
     */
    @Test
    void testHasDrawnTreasureCards() {
        when(gameController.getPlayerController()).thenReturn(mock(PlayerController.class));
        when(gameController.getPlayerController().hasDrawnTreasureCards()).thenReturn(true);
        assertTrue(actionBarController.hasDrawnTreasureCards());
    }

    /**
     * Test getDrawnFloodCards
     */
    @Test
    void testGetDrawnFloodCards() {
        when(gameController.getPlayerController()).thenReturn(mock(PlayerController.class));
        when(gameController.getPlayerController().getDrawnFloodCards()).thenReturn(2);
        assertEquals(2, actionBarController.getDrawnFloodCards());
    }

    /**
     * Test setHasDrawnTreasureCards
     */
    @Test
    void testSetHasDrawnTreasureCards() {
        PlayerController playerController = mock(PlayerController.class);
        when(gameController.getPlayerController()).thenReturn(playerController);
        doNothing().when(playerController).setHasDrawnTreasureCards(true);
        
        actionBarController.setHasDrawnTreasureCards(true);
        
        verify(playerController).setHasDrawnTreasureCards(true);
    }
}
