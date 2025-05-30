package com.island.controller;

import com.island.controller.factory.CardFactory;
import com.island.controller.factory.StandardCardFactory;
import com.island.models.adventurers.Player;
import com.island.models.card.Card;
import com.island.models.card.CardType;
import com.island.models.island.Island;
import com.island.models.island.Position;
import com.island.models.island.Tile;
import com.island.models.treasure.TreasureType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CardController 的单元测试，覆盖所有方法，使用 Mockito 进行依赖mock。
 */
public class CardControllerTest {
    @Mock
    private GameController gameController;
    @Mock
    private IslandController islandController;
    @Mock
    private Island island;
    @Mock
    private Player player;
    @Mock
    private Tile tile;

    private CardController cardController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        CardFactory StandardCardFactory = new StandardCardFactory();
        cardController = new CardController(StandardCardFactory);
        when(gameController.getIslandController()).thenReturn(islandController);
        when(islandController.getIsland()).thenReturn(island);
        cardController.setGameController(gameController);
    }

    /**
     * 测试初始化卡牌方法，包含岛屿板块
     */
    @Test
    void testInitCards() {
        Map<Position, Tile> tiles = new HashMap<>();
        // mock 10 个 tile，保证 floodDeck 足够
        for (int i = 0; i < 10; i++) {
            tiles.put(new Position(i, i), mock(Tile.class));
        }
        when(island.getTiles()).thenReturn(tiles);
        cardController.initCards(123L);
        assertFalse(cardController.getTreasureDeck().isEmpty());
        assertFalse(cardController.getFloodDeck().isEmpty());
        verify(gameController, atLeastOnce()).updateCardView();
    }

    /**
     * 测试抽取洪水卡，正常流程
     */
    @Test
    void testDrawFloodCards() {
        Card floodCard = Card.createFloodCard("tile", new Position(0,0), "");
        cardController.getFloodDeck().add(floodCard);
        doNothing().when(island).floodTile(any());
        List<Position> result = cardController.drawFloodCards(1);
        assertEquals(1, result.size());
        verify(gameController, atLeastOnce()).updateCardView();
        verify(gameController, atLeastOnce()).updateBoard();
    }

    /**
     * 测试抽取洪水卡，floodDeck为空但floodDiscardPile有卡
     */
    @Test
    void testDrawFloodCardsWithReshuffle() {
        Card floodCard = Card.createFloodCard("tile", new Position(0,0), "");
        cardController.getFloodDiscardPile().add(floodCard);
        doNothing().when(island).floodTile(any());
        List<Position> result = cardController.drawFloodCards(1);
        // floodDiscardPile被洗回floodDeck后抽取
        assertEquals(1, result.size());
    }

    /**
     * 测试抽取宝藏卡，正常流程
     */
    @Test
    void testDrawTreasureCard() {
        Card treasureCard = Card.createTreasureCard(TreasureType.EARTH_STONE, "");
        cardController.getTreasureDeck().add(treasureCard);
        doNothing().when(player).addCard(any(Card.class));
        cardController.drawTreasureCard(1, player);
        verify(player, atLeastOnce()).addCard(any(Card.class));
        verify(gameController, atLeastOnce()).updateCardView();
        verify(gameController, atLeastOnce()).updatePlayersInfo();
    }

    /**
     * 测试抽取宝藏卡，treasureDeck为空但treasureDiscardPile有卡
     */
    @Test
    void testDrawTreasureCardWithReshuffle() {
        Card treasureCard = Card.createTreasureCard(TreasureType.EARTH_STONE, "");
        cardController.getTreasureDiscardPile().add(treasureCard);
        doNothing().when(player).addCard(any(Card.class));
        cardController.drawTreasureCard(1, player);
        verify(player, atLeastOnce()).addCard(any(Card.class));
    }

    /**
     * 测试抽取宝藏卡遇到WATER_RISE
     */
    @Test
    void testDrawTreasureCardWithWaterRise() {
        Card waterRise = Card.createSpecialCard(CardType.WATER_RISE);
        cardController.getTreasureDeck().add(waterRise);
        doNothing().when(gameController).handleWaterRise();
        cardController.drawTreasureCard(1, player);
        verify(gameController, atLeastOnce()).handleWaterRise();
    }

    /**
     * 测试 get/set 方法
     */
    @Test
    void testGettersAndAdders() {
        Card card = Card.createTreasureCard(TreasureType.EARTH_STONE, "");
        cardController.addTreasureDiscardPile(card);
        assertTrue(cardController.getTreasureDiscardPile().contains(card));
        assertNotNull(cardController.getFloodDeck());
        assertNotNull(cardController.getTreasureDeck());
        assertNotNull(cardController.getFloodDiscardPile());
    }

    /**
     * 测试水位上升处理
     */
    @Test
    void testHandleWaterRise() {
        Card card = Card.createFloodCard("tile", new Position(0,0), "");
        cardController.getFloodDiscardPile().add(card);
        cardController.handleWaterRise();
        assertTrue(cardController.getTreasureDiscardPile().stream().anyMatch(c -> c.getType() == CardType.WATER_RISE));
    }

    /**
     * 测试资源清理
     */
    @Test
    void testShutdown() {
        cardController.getTreasureDeck().add(Card.createTreasureCard(TreasureType.EARTH_STONE, ""));
        cardController.getFloodDeck().add(Card.createFloodCard("tile", new Position(0,0), ""));
        cardController.getTreasureDiscardPile().add(Card.createTreasureCard(TreasureType.EARTH_STONE, ""));
        cardController.getFloodDiscardPile().add(Card.createFloodCard("tile", new Position(0,0), ""));
        cardController.shutdown();
        assertTrue(cardController.getTreasureDeck().isEmpty());
        assertTrue(cardController.getFloodDeck().isEmpty());
        assertTrue(cardController.getTreasureDiscardPile().isEmpty());
        assertTrue(cardController.getFloodDiscardPile().isEmpty());
    }
}
