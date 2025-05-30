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
 * The unit tests for the CardController class.
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
     * Test initialization of cards using the card factory
     */
    @Test
    void testInitCards() {
        Map<Position, Tile> tiles = new HashMap<>();
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
     * Test drawing flood cards from the flood deck
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
     * Test drawing flood cards when the flood deck is empty but the discard pile has cards
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
     * Test drawing a flood card when both floodDeck and floodDiscardPile are empty
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
     * Test drawing a treasure card when the treasure deck is empty but the discard pile has cards
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
     * Test drawing a treasure card that is a water rise card
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
     * Test getters and adders for various card collections
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
     * Test handleWaterRise method to move a flood card to the treasure discard pile
     */
    @Test
    void testHandleWaterRise() {
        Card card = Card.createFloodCard("tile", new Position(0,0), "");
        cardController.getFloodDiscardPile().add(card);
        cardController.handleWaterRise();
        assertTrue(cardController.getTreasureDiscardPile().stream().anyMatch(c -> c.getType() == CardType.WATER_RISE));
    }

    /**
     * Test shutdown method to clear all decks and discard piles
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
