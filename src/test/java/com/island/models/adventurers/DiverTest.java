package com.island.models.adventurers;

import com.island.models.island.Position;
import com.island.models.island.Tile;
import com.island.models.treasure.TreasureType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Diver player role in the Forbidden Island game.
 * This class contains unit tests to verify the Diver's ability to swim through flooded tiles
 * and move through them as if they were normal tiles.
 */
class DiverTest {
    private Diver diver;
    private Position startPosition;
    private Map<Position, Tile> tiles;

    /**
     * Sets up the test environment before each test.
     * Creates a new Diver player and initializes a 5x5 grid of tiles.
     */
    @BeforeEach
    void setUp() {
        diver = new Diver("TestDiver");
        startPosition = new Position(2, 2);
        diver.setPosition(startPosition);
        tiles = new HashMap<>();
        
        // Create a 5x5 grid of tiles around the diver
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                Position pos = new Position(x, y);
                tiles.put(pos, new Tile("Tile" + x + y, pos, TreasureType.NONE));
            }
        }
    }

    /**
     * Tests the basic creation and initialization of a Diver player.
     * Verifies that the player has the correct role and name.
     */
    @Test
    void testDiverCreation() {
        assertEquals("TestDiver", diver.getName());
        assertEquals(PlayerRole.DIVER, diver.getRole());
    }

    /**
     * Tests the Diver's movement capabilities on normal tiles.
     * Verifies that the Diver can move orthogonally to adjacent normal tiles.
     */
    @Test
    void testGetMovePositionsNormalTiles() {
        // All tiles are normal, diver should be able to move to adjacent tiles
        List<Position> movePositions = diver.getMovePositions(tiles);
        assertEquals(4, movePositions.size()); // Should be able to move in 4 directions
        assertTrue(movePositions.contains(new Position(2, 1))); // Up
        assertTrue(movePositions.contains(new Position(2, 3))); // Down
        assertTrue(movePositions.contains(new Position(1, 2))); // Left
        assertTrue(movePositions.contains(new Position(3, 2))); // Right
    }

    /**
     * Tests the Diver's ability to move through flooded tiles.
     * Verifies that the Diver can move through flooded tiles as if they were normal.
     */
    @Test
    void testGetMovePositionsWithFloodedTiles() {
        // Make some tiles flooded
        tiles.get(new Position(2, 1)).flood(); // Up
        tiles.get(new Position(2, 3)).flood(); // Down
        
        List<Position> movePositions = diver.getMovePositions(tiles);
        assertTrue(movePositions.contains(new Position(2, 1))); // Can move through flooded tiles
        assertTrue(movePositions.contains(new Position(2, 3))); // Can move through flooded tiles
        assertTrue(movePositions.contains(new Position(1, 2))); // Normal tile
        assertTrue(movePositions.contains(new Position(3, 2))); // Normal tile
    }

    /**
     * Tests the Diver's movement restrictions with sunk tiles.
     * Verifies that the Diver cannot move through sunk tiles.
     */
    @Test
    void testGetMovePositionsWithSunkTiles() {
        // Make some tiles sunk
        Tile upTile = tiles.get(new Position(2, 1));
        upTile.flood(); // First flood
        upTile.flood(); // Then sink
        
        List<Position> movePositions = diver.getMovePositions(tiles);
        assertFalse(movePositions.contains(new Position(2, 1))); // Cannot move through sunk tiles
        assertTrue(movePositions.contains(new Position(2, 3))); // Can move to normal tile
        assertTrue(movePositions.contains(new Position(1, 2))); // Can move to normal tile
        assertTrue(movePositions.contains(new Position(3, 2))); // Can move to normal tile
    }

    /**
     * Tests the Diver's ability to move through multiple flooded tiles.
     * Verifies that the Diver can move through a path of flooded tiles.
     */
    @Test
    void testGetMovePositionsThroughMultipleFloodedTiles() {
        // Create a path of flooded tiles
        tiles.get(new Position(2, 1)).flood(); // Up
        tiles.get(new Position(2, 0)).flood(); // Up-Up
        
        List<Position> movePositions = diver.getMovePositions(tiles);
        assertTrue(movePositions.contains(new Position(2, 0))); // Can move through multiple flooded tiles
        assertTrue(movePositions.contains(new Position(2, 1))); // Can move through flooded tile
    }
} 