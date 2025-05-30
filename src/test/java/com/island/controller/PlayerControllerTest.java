package com.island.controller;

import com.island.models.*;
import com.island.network.RoomController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * PlayerController 的单元测试，覆盖所有方法，使用 Mockito 进行依赖mock。
 */
public class PlayerControllerTest {
    @Mock
    private GameController gameController;
    @Mock
    private Room room;
    @Mock
    private Player player;
    @Mock
    private Card card;
    @Mock
    private Island island;
    @Mock
    private Tile tile;

    private PlayerController playerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        playerController = new PlayerController();
        when(gameController.getRoomController()).thenReturn(mock(RoomController.class));
        when(gameController.getRoomController().getRoom()).thenReturn(room);
        when(gameController.getIslandController()).thenReturn(mock(IslandController.class));
        when(gameController.getIslandController().getIsland()).thenReturn(island);
        playerController.setGameController(gameController);
    }

    /**
     * 测试 initPlayers
     */
    @Test
    void testInitPlayers() {
        List<Player> players = new ArrayList<>();
        players.add(new Diver("A"));
        players.add(new Diver("B"));
        when(room.getPlayers()).thenReturn(players);
        when(gameController.getCurrentPlayer()).thenReturn(players.get(0));
        when(room.isHost(anyString())).thenReturn(true);
        playerController.initPlayers(123L);
        // 只要不抛异常即可
    }

    /**
     * 测试 dealCards
     */
    @Test
    void testDealCards() {
        Deque<Card> deck = new ArrayDeque<>();
        Card c = mock(Card.class); // 使用 mock 确保 c 不为 null
        when(c.getType()).thenReturn(CardType.TREASURE);
        deck.add(c);

        List<Player> players = new ArrayList<>();
        players.add(player);

        when(room.getPlayers()).thenReturn(players);
        when(player.getCards()).thenReturn(new ArrayList<>());
        doNothing().when(player).addCard(any());

        playerController.dealCards(deck);

        // 验证 player.addCard 被调用
        verify(player).addCard(c);
    }

    /**
     * 测试 canPlaySpecialCard
     */
    @Test
    void testCanPlaySpecialCard() {
        List<Card> cards = new ArrayList<>();
        Card c = Card.createSpecialCard(CardType.HELICOPTER);
        cards.add(c);
        when(player.getCards()).thenReturn(cards);
        assertTrue(playerController.canPlaySpecialCard(player));
    }

    /**
     * 测试 canShoreUpTile
     */
    @Test
    void testCanShoreUpTile() {
        when(player.getShorePositions(any())).thenReturn(List.of(new Position(0,0)));
        assertTrue(playerController.canShoreUpTile(player));
    }

    /**
     * 测试 canGiveCard
     */
    @Test
    void testCanGiveCard() {
        when(player.getCards()).thenReturn(List.of(card));
        when(player.getRole()).thenReturn(PlayerRole.MESSENGER);
        assertTrue(playerController.canGiveCard(player));
    }

    /**
     * 测试 canCaptureTreasure
     */
    @Test
    void testCanCaptureTreasure() {
        when(player.getPosition()).thenReturn(new Position(0,0));
        when(island.getTile(any())).thenReturn(tile);
        when(tile.getTreasureType()).thenReturn(TreasureType.EARTH_STONE);
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Card c = Card.createTreasureCard(TreasureType.EARTH_STONE, "");
            cards.add(c);
        }
        when(player.getCards()).thenReturn(cards);
        when(gameController.getIsland()).thenReturn(island);
        assertTrue(playerController.canCaptureTreasure(player));
    }

    /**
     * 测试 hasDrawnTreasureCards
     */
    @Test
    void testHasDrawnTreasureCards() {
        when(room.getCurrentProgramPlayer()).thenReturn(player);
        when(player.hasDrawnTreasureCards()).thenReturn(true);
        assertTrue(playerController.hasDrawnTreasureCards());
    }

    /**
     * 测试 getDrawnFloodCards
     */
    @Test
    void testGetDrawnFloodCards() {
        when(room.getCurrentProgramPlayer()).thenReturn(player);
        when(player.getDrawFloodCards()).thenReturn(2);
        assertEquals(2, playerController.getDrawnFloodCards());
    }

    /**
     * 测试 setHasDrawnTreasureCards
     */
    @Test
    void testSetHasDrawnTreasureCards() {
        when(room.getCurrentProgramPlayer()).thenReturn(player);
        doNothing().when(player).setHasDrawnTreasureCards(true);
        playerController.setHasDrawnTreasureCards(true);
        verify(player).setHasDrawnTreasureCards(true);
    }

    /**
     * 测试 addDrawnFloodCards
     */
    @Test
    void testAddDrawnFloodCards() {
        when(room.getCurrentProgramPlayer()).thenReturn(player);
        doNothing().when(player).addDrawnFloodCards(1);
        playerController.addDrawnFloodCards(1);
        verify(player).addDrawnFloodCards(1);
    }

    /**
     * 测试 resetPlayerState
     */
    @Test
    void testResetPlayerState() {
        when(room.getCurrentProgramPlayer()).thenReturn(player);
        doNothing().when(player).resetState();
        playerController.resetPlayerState();
        verify(player).resetState();
    }
}
