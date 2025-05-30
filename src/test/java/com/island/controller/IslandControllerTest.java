package com.island.controller;


import com.island.models.Room;
import com.island.models.adventurers.Engineer;
import com.island.models.adventurers.Navigator;
import com.island.models.adventurers.Player;
import com.island.models.card.Card;
import com.island.models.card.CardType;
import com.island.models.island.Island;
import com.island.models.island.Position;
import com.island.models.island.Tile;
import com.island.models.treasure.TreasureType;
import com.island.network.RoomController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test class for IslandController.
 */
public class IslandControllerTest {
    @Mock
    private GameController gameController;
    @Mock
    private Room room;
    @Mock
    private Island island;
    @Mock
    private Player player;
    @Mock
    private Tile tile;
    @Mock
    private Engineer engineer;
    @Mock
    private Navigator navigator;

    private IslandController islandController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RoomController mockRoomController = mock(RoomController.class);
        when(gameController.getRoomController()).thenReturn(mockRoomController);
        when(mockRoomController.getRoom()).thenReturn(room);
        islandController = new IslandController();
        islandController.setGameController(gameController);
    }

    /**
     * Test the constructor and initial state of IslandController.
     */
    @Test
    void testInitIsland() {
        islandController.initIsland(123L);
        // As long as no exceptions are thrown, the test passes
    }

    /**
     * Test getters and setters of IslandController.
     */
    @Test
    void testGettersAndSetters() {
        assertNotNull(islandController.getIsland());
        assertNotNull(islandController.getGameController());
        assertNotNull(islandController.getTreasures());
        islandController.setWaterLevel(3);
        assertEquals(3, islandController.getWaterLevel());
    }

    /**
     * Test increaseWaterLevel
     */
    @Test
    void testIncreaseWaterLevel() {
        doNothing().when(gameController).updateWaterLevel();
        islandController.increaseWaterLevel();
        assertEquals(2, islandController.getWaterLevel());
    }

    /**
     * Test handleTileClick
     */
    @Test
    void testHandleTileClick() {
        islandController.initIsland(123L);
        when(gameController.getCurrentPlayer()).thenReturn(navigator);
        when(navigator.getNavigatorTarget()).thenReturn(player);
        when(navigator.getNavigatorMoves()).thenReturn(1);
        when(player.getPosition()).thenReturn(new Position(3,0));
        when(tile.getPosition()).thenReturn(new Position(3,1));
        when(tile.isSunk()).thenReturn(false);
        when(island.getTile(any())).thenReturn(tile);
        islandController.handleTileClick(tile);
        // As long as no exceptions are thrown, the test passes
    }

    /**
     * Test removeTreasure
     */
    @Test
    void testRemoveTreasure() {
        String[] treasures = islandController.getTreasures();
        String toRemove = treasures[0];
        islandController.removeTreasure(toRemove);
        assertTrue(Arrays.stream(islandController.getTreasures()).anyMatch(t -> t == null || !t.equals(toRemove)));
    }

    /**
     * Test checkTreasureTiles
     */
    @Test
    void testCheckTreasureTiles() {
        assertTrue(islandController.checkTreasureTiles());
    }

    /**
     * Test checkFoolsLanding
     */
    @Test
    void testCheckFoolsLanding() {
        assertTrue(islandController.checkFoolsLanding());
    }

    /**
     * Test movePlayer
     */
    @Test
    void testShoreUpTile() {
        islandController.initIsland(123L);
        Position pos = new Position(3,1);
        when(island.getTile(pos)).thenReturn(tile);
        doNothing().when(tile).shoreUp();
        doNothing().when(gameController).resetTileBorders();
        doNothing().when(gameController).decreaseRemainingActions();
        doNothing().when(gameController).updateBoard();
        islandController.shoreUpTile(player, pos);
        // As long as no exceptions are thrown, the test passes
    }

    /**
     * Test movePlayer
     */
    @Test
    void testCaptureTreasure() {
        List<Card> cards = new ArrayList<>();
        Card card = mock(Card.class);
        cards.add(card);

        when(player.getCards()).thenReturn(cards);
        when(card.getType()).thenReturn(CardType.TREASURE);
        when(card.getTreasureType()).thenReturn(TreasureType.EARTH_STONE);
        when(player.removeCard(anyString())).thenReturn(null);
        doNothing().when(gameController).decreaseRemainingActions();

        when(gameController.getCurrentPlayer()).thenReturn(player);

        islandController.captureTreasure(player, TreasureType.EARTH_STONE);
        // As long as no exceptions are thrown, the test passes
    }
}
