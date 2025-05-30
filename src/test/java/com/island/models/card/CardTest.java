package com.island.models.card;

import com.island.models.island.Position;
import com.island.models.treasure.TreasureType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Card class in the Forbidden Island game.
 * This class contains unit tests to verify the creation and management of different types of cards,
 * including treasure cards, flood cards, and special action cards.
 */
class CardTest {
    /**
     * Tests the creation of a treasure card.
     * Verifies that a treasure card is created with the correct type, name, owner,
     * and treasure type, and that flood position is null.
     */
    @Test
    void testCreateTreasureCard() {
        Card card = Card.createTreasureCard(TreasureType.FIRE_CRYSTAL, "Player1");
        
        assertEquals(CardType.TREASURE, card.getType());
        assertEquals("Fire", card.getName());
        assertEquals("Player1", card.getBelongingPlayer());
        assertNull(card.getFloodPosition());
        assertEquals(TreasureType.FIRE_CRYSTAL, card.getTreasureType());
    }

    /**
     * Tests the creation of a flood card.
     * Verifies that a flood card is created with the correct type, name, owner,
     * and flood position, and that treasure type is null.
     */
    @Test
    void testCreateFloodCard() {
        Position pos = new Position(2, 3);
        Card card = Card.createFloodCard("TestTile", pos, "Player1");
        
        assertEquals(CardType.FLOOD, card.getType());
        assertEquals("TestTile", card.getName());
        assertEquals("Player1", card.getBelongingPlayer());
        assertEquals(pos, card.getFloodPosition());
        assertNull(card.getTreasureType());
    }

    /**
     * Tests the creation of special action cards.
     * Verifies that different types of special cards (Water Rise, Helicopter, Sandbags)
     * are created with the correct type and name, and that all other properties are null.
     */
    @Test
    void testCreateSpecialCard() {
        // Test Water Rise card
        Card waterRiseCard = Card.createSpecialCard(CardType.WATER_RISE);
        assertEquals(CardType.WATER_RISE, waterRiseCard.getType());
        assertEquals("WaterRise", waterRiseCard.getName());
        assertNull(waterRiseCard.getBelongingPlayer());
        assertNull(waterRiseCard.getFloodPosition());
        assertNull(waterRiseCard.getTreasureType());

        // Test Helicopter card
        Card helicopterCard = Card.createSpecialCard(CardType.HELICOPTER);
        assertEquals(CardType.HELICOPTER, helicopterCard.getType());
        assertEquals("Helicopter", helicopterCard.getName());
        assertNull(helicopterCard.getBelongingPlayer());
        assertNull(helicopterCard.getFloodPosition());
        assertNull(helicopterCard.getTreasureType());

        // Test Sandbags card
        Card sandbagsCard = Card.createSpecialCard(CardType.SANDBAGS);
        assertEquals(CardType.SANDBAGS, sandbagsCard.getType());
        assertEquals("Sandbags", sandbagsCard.getName());
        assertNull(sandbagsCard.getBelongingPlayer());
        assertNull(sandbagsCard.getFloodPosition());
        assertNull(sandbagsCard.getTreasureType());
    }

    /**
     * Tests the ability to change a card's owner.
     * Verifies that the belonging player can be set to a new player or cleared.
     */
    @Test
    void testSetBelongingPlayer() {
        Card card = Card.createTreasureCard(TreasureType.FIRE_CRYSTAL, "Player1");
        
        card.setBelongingPlayer("Player2");
        assertEquals("Player2", card.getBelongingPlayer());
        
        card.setBelongingPlayer("");
        assertEquals("", card.getBelongingPlayer());
    }

    /**
     * Tests the creation of special cards with invalid types.
     * Verifies that attempting to create a special card with TREASURE or FLOOD type
     * throws an IllegalArgumentException.
     */
    @Test
    void testCreateSpecialCardWithInvalidType() {
        assertThrows(IllegalArgumentException.class, () -> {
            Card.createSpecialCard(CardType.TREASURE);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            Card.createSpecialCard(CardType.FLOOD);
        });
    }
} 