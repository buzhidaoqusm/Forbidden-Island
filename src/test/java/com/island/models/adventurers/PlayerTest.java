package com.island.models.adventurers;

import com.island.models.card.Card;
import com.island.models.card.CardType;
import com.island.models.island.Position;
import com.island.models.island.Tile;
import com.island.models.treasure.TreasureType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the base Player class in the Forbidden Island game.
 * This class contains unit tests to verify the common functionality shared by all player roles,
 * including movement, card management, and treasure collection.
 */
class PlayerTest {
    private Player player;
    private Position position;
    private Card card1;
    private Card card2;

    /**
     * Sets up the test environment before each test.
     * Creates a new Player and initializes test cards and position.
     */
    @BeforeEach
    void setUp() {
        player = new Player("TestPlayer");
        position = new Position(2, 2);
        player.setPosition(position);
        card1 = Card.createSpecialCard(CardType.HELICOPTER);
        card2 = Card.createSpecialCard(CardType.SANDBAGS);
    }

    /**
     * Tests the basic creation and initialization of a Player.
     * Verifies that the player has the correct name and initial state.
     */
    @Test
    void testPlayerCreation() {
        assertEquals("TestPlayer", player.getName());
        assertNotNull(player.getCards());
        assertTrue(player.getCards().isEmpty());
        assertFalse(player.isHasDrawnTreasureCards());
        assertEquals(0, player.getDrawnFloodCards());
        assertTrue(player.getCapturedTreasures().isEmpty());
        assertNotNull(player.getPosition());
        assertEquals(position, player.getPosition());
    }

    /**
     * Tests the ability to change a player's name.
     * Verifies that the name is correctly updated.
     */
    @Test
    void testSetName() {
        player.setName("NewName");
        assertEquals("NewName", player.getName());
        assertNotNull(player.getCards());
        assertTrue(player.getCards().isEmpty());
    }

    /**
     * Tests the ability to change a player's position.
     * Verifies that the position is correctly updated.
     */
    @Test
    void testSetPosition() {
        Position newPosition = new Position(3, 3);
        player.setPosition(newPosition);
        assertEquals(newPosition, player.getPosition());
    }

    /**
     * Tests adding a card to a player's hand.
     * Verifies that the card is correctly added and associated with the player.
     */
    @Test
    void testAddCard() {
        player.addCard(card1);
        assertEquals(1, player.getCards().size());
        assertEquals(card1, player.getCards().get(0));
        assertEquals("TestPlayer", card1.getBelongingPlayer());
    }

    /**
     * Tests removing a card from a player's hand by name.
     * Verifies that the correct card is removed and its ownership is cleared.
     */
    @Test
    void testRemoveCardByName() {
        player.addCard(card1);
        player.addCard(card2);
        Card removed = player.removeCard("Helicopter");
        assertEquals(card1, removed);
        assertEquals(1, player.getCards().size());
        assertEquals("", card1.getBelongingPlayer());
    }

    /**
     * Tests removing a card from a player's hand by index.
     * Verifies that the correct card is removed and its ownership is cleared.
     */
    @Test
    void testRemoveCardByIndex() {
        player.addCard(card1);
        player.addCard(card2);
        Card removed = player.removeCard(0);
        assertEquals(card1, removed);
        assertEquals(1, player.getCards().size());
        assertEquals("", card1.getBelongingPlayer());
    }

    /**
     * Tests attempting to remove a non-existent card by name.
     * Verifies that null is returned and no cards are removed.
     */
    @Test
    void testRemoveCardByNameNotFound() {
        player.addCard(card1);
        Card removed = player.removeCard("NonExistentCard");
        assertNull(removed);
        assertEquals(1, player.getCards().size());
    }

    /**
     * Tests attempting to remove a card with an invalid index.
     * Verifies that an IndexOutOfBoundsException is thrown.
     */
    @Test
    void testRemoveCardByInvalidIndex() {
        player.addCard(card1);
        assertThrows(IndexOutOfBoundsException.class, () -> {
            player.removeCard(1);
        });
    }

    /**
     * Tests adding a captured treasure to a player's collection.
     * Verifies that the treasure is correctly added.
     */
    @Test
    void testAddCapturedTreasure() {
        player.addCapturedTreasure(TreasureType.FIRE_CRYSTAL);
        assertEquals(1, player.getCapturedTreasures().size());
        assertEquals(TreasureType.FIRE_CRYSTAL, player.getCapturedTreasures().get(0));
    }

    /**
     * Tests resetting a player's state at the start of a new turn.
     * Verifies that all turn-specific state variables are reset.
     */
    @Test
    void testResetState() {
        player.setHasDrawnTreasureCards(true);
        player.addDrawnFloodCards(2);
        player.resetState();
        assertFalse(player.isHasDrawnTreasureCards());
        assertEquals(0, player.getDrawnFloodCards());
    }

    /**
     * Tests a player's movement capabilities on normal tiles.
     * Verifies that the player can move orthogonally to adjacent normal tiles.
     */
    @Test
    void testGetMovePositions() {
        Map<Position, Tile> tiles = new HashMap<>();
        // Add tiles around the player's position
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                Position pos = new Position(position.getX() + dx, position.getY() + dy);
                tiles.put(pos, new Tile("TestTile", pos, TreasureType.NONE));
            }
        }
        List<Position> movePositions = player.getMovePositions(tiles);
        assertEquals(4, movePositions.size()); // Should be able to move in 4 directions
        assertTrue(movePositions.contains(new Position(2, 1))); // Up
        assertTrue(movePositions.contains(new Position(2, 3))); // Down
        assertTrue(movePositions.contains(new Position(1, 2))); // Left
        assertTrue(movePositions.contains(new Position(3, 2))); // Right
    }

    /**
     * Tests a player's movement restrictions with sunk tiles.
     * Verifies that the player cannot move to sunk tiles.
     */
    @Test
    void testGetMovePositionsWithSunkTiles() {
        Map<Position, Tile> tiles = new HashMap<>();
        // Add tiles around the player's position
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                Position pos = new Position(position.getX() + dx, position.getY() + dy);
                Tile tile = new Tile("TestTile", pos, TreasureType.NONE);
                if (dx == 1 && dy == 0) { // Right tile
                    tile.flood();
                    tile.flood(); // Make it sunk
                }
                tiles.put(pos, tile);
            }
        }
        List<Position> movePositions = player.getMovePositions(tiles);
        assertEquals(3, movePositions.size()); // Should not be able to move to sunk tile
        assertFalse(movePositions.contains(new Position(3, 2))); // Right (sunk)
    }

    /**
     * Tests a player's ability to shore up flooded tiles.
     * Verifies that the player can shore up adjacent flooded tiles.
     */
    @Test
    void testGetShorePositions() {
        Map<Position, Tile> tiles = new HashMap<>();
        // Add flooded tiles around the player's position
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                Position pos = new Position(position.getX() + dx, position.getY() + dy);
                Tile tile = new Tile("TestTile", pos, TreasureType.NONE);
                tile.flood(); // This will set the state to FLOODED
                tiles.put(pos, tile);
            }
        }
        List<Position> shorePositions = player.getShorePositions(tiles);
        assertEquals(4, shorePositions.size()); // Should be able to shore up in 8 directions
    }

    /**
     * Tests a player's shoring up restrictions with sunk tiles.
     * Verifies that the player cannot shore up sunk tiles.
     */
    @Test
    void testGetShorePositionsWithSunkTiles() {
        Map<Position, Tile> tiles = new HashMap<>();
        // Add tiles around the player's position
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                Position pos = new Position(position.getX() + dx, position.getY() + dy);
                Tile tile = new Tile("TestTile", pos, TreasureType.NONE);
                if (dx == 1 && dy == 0) { // Right tile
                    tile.flood();
                    tile.flood(); // Make it sunk
                } else {
                    tile.flood(); // Make others flooded
                }
                tiles.put(pos, tile);
            }
        }
        List<Position> shorePositions = player.getShorePositions(tiles);
        assertEquals(3, shorePositions.size()); // Should not be able to shore up sunk tile
        assertFalse(shorePositions.contains(new Position(3, 2))); // Right (sunk)
    }

    /**
     * Tests a player's ability to give cards to adjacent players.
     * Verifies that cards can only be given to players on the same tile.
     */
    @Test
    void testGetGiveCardPlayers() {
        List<Player> players = new ArrayList<>();
        Player otherPlayer = new Player("OtherPlayer");
        otherPlayer.setPosition(position);
        players.add(player);
        players.add(otherPlayer);
        
        List<Player> eligiblePlayers = player.getGiveCardPlayers(players);
        assertEquals(1, eligiblePlayers.size());
        assertEquals(otherPlayer, eligiblePlayers.get(0));
    }

    /**
     * Tests a player's card giving restrictions with non-adjacent players.
     * Verifies that cards cannot be given to players on different tiles.
     */
    @Test
    void testGetGiveCardPlayersWithNoAdjacentPlayers() {
        List<Player> players = new ArrayList<>();
        Player otherPlayer = new Player("OtherPlayer");
        otherPlayer.setPosition(new Position(4, 4)); // Far away position
        players.add(player);
        players.add(otherPlayer);
        
        List<Player> eligiblePlayers = player.getGiveCardPlayers(players);
        assertTrue(eligiblePlayers.isEmpty());
    }
} 