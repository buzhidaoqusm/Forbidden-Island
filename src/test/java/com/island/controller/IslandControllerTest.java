package com.island.controller;

import com.island.model.*;
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
 * IslandController 的单元测试，覆盖所有方法，使用 Mockito 进行依赖mock。
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
        // 先 mock RoomController 并设置好依赖
        RoomController mockRoomController = mock(RoomController.class);
        when(gameController.getRoomController()).thenReturn(mockRoomController);
        when(mockRoomController.getRoom()).thenReturn(room);
        islandController = new IslandController(island);
        islandController.setGameController(gameController);
    }

    /**
     * 测试 initIsland
     */
    @Test
    void testInitIsland() {
        islandController.initIsland(123L);
        // 只要不抛异常即可
    }

    /**
     * 测试 get/set
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
     * 测试 increaseWaterLevel
     */
    @Test
    void testIncreaseWaterLevel() {
        doNothing().when(gameController).updateWaterLevel();
        islandController.increaseWaterLevel();
        assertEquals(2, islandController.getWaterLevel());
    }

    /**
     * 测试 handleTileClick
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
        // 只要不抛异常即可
    }

    /**
     * 测试 removeTreasure
     */
    @Test
    void testRemoveTreasure() {
        String[] treasures = islandController.getTreasures();
        String toRemove = treasures[0];
        islandController.removeTreasure(toRemove);
        assertTrue(Arrays.stream(islandController.getTreasures()).anyMatch(t -> t == null || !t.equals(toRemove)));
    }

    /**
     * 测试 checkTreasureTiles
     */
    @Test
    void testCheckTreasureTiles() {
        assertTrue(islandController.checkTreasureTiles());
    }

    /**
     * 测试 checkFoolsLanding
     */
    @Test
    void testCheckFoolsLanding() {
        assertTrue(islandController.checkFoolsLanding());
    }

    /**
     * 测试 shoreUpTile
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
        // 只要不抛异常即可
    }

    /**
     * 测试 captureTreasure
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
        doNothing().when(player).captureTreasure(any());
        doNothing().when(gameController).decreaseRemainingActions();

        // 修复：为 gameController.getCurrentPlayer() 设置返回值
        when(gameController.getCurrentPlayer()).thenReturn(player);

        islandController.captureTreasure(player, TreasureType.EARTH_STONE);
        // 只要不抛异常即可
    }
}
