package com.island.models.island;

import com.island.models.treasure.TreasureType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Tile class in the Forbidden Island game.
 * This class contains unit tests to verify the creation and state management of tiles,
 * including flooding, sinking, and shoring up functionality.
 */
class TileTest {
    private Tile tile;
    private Position position;

    /**
     * Sets up the test environment before each test.
     * Creates a new Tile with a test position and treasure type.
     */
    @BeforeEach
    void setUp() {
        position = new Position(2, 3);
        tile = new Tile("TestTile", position, TreasureType.FIRE_CRYSTAL);
    }

    /**
     * Tests the creation of a Tile object.
     * Verifies that the tile is created with the correct name, position,
     * treasure type, and initial state (NORMAL).
     */
    @Test
    void testTileCreation() {
        assertEquals("TestTile", tile.getName());
        assertEquals(position, tile.getPosition());
        assertEquals(TreasureType.FIRE_CRYSTAL, tile.getTreasureType());
        assertEquals(Tile.TileState.NORMAL, tile.getState());
    }

    /**
     * Tests the flooding functionality of a tile.
     * Verifies that:
     * 1. First flood changes state to FLOODED
     * 2. Second flood changes state to SUNK
     * 3. State flags (isFlooded, isSunk, isNormal) are correctly updated
     */
    @Test
    void testFlood() {
        // Test first flood
        tile.flood();
        assertEquals(Tile.TileState.FLOODED, tile.getState());
        assertTrue(tile.isFlooded());
        assertFalse(tile.isSunk());
        assertFalse(tile.isNormal());

        // Test second flood (sinking)
        tile.flood();
        assertEquals(Tile.TileState.SUNK, tile.getState());
        assertTrue(tile.isSunk());
        assertFalse(tile.isFlooded());
        assertFalse(tile.isNormal());
    }

    /**
     * Tests the shoring up functionality of a flooded tile.
     * Verifies that shoring up a flooded tile returns it to NORMAL state
     * and updates all state flags correctly.
     */
    @Test
    void testShoreUp() {
        // First flood the tile
        tile.flood();
        assertEquals(Tile.TileState.FLOODED, tile.getState());

        // Then shore it up
        tile.shoreUp();
        assertEquals(Tile.TileState.NORMAL, tile.getState());
        assertTrue(tile.isNormal());
        assertFalse(tile.isFlooded());
        assertFalse(tile.isSunk());
    }

    /**
     * Tests attempting to shore up a normal tile.
     * Verifies that shoring up a normal tile has no effect on its state.
     */
    @Test
    void testShoreUpOnNormalTile() {
        // Try to shore up a normal tile
        tile.shoreUp();
        assertEquals(Tile.TileState.NORMAL, tile.getState());
        assertTrue(tile.isNormal());
    }

    /**
     * Tests attempting to shore up a sunk tile.
     * Verifies that shoring up a sunk tile has no effect on its state.
     */
    @Test
    void testShoreUpOnSunkTile() {
        // First sink the tile
        tile.flood();
        tile.flood();
        assertEquals(Tile.TileState.SUNK, tile.getState());

        // Try to shore up a sunk tile
        tile.shoreUp();
        assertEquals(Tile.TileState.SUNK, tile.getState());
        assertTrue(tile.isSunk());
    }
} 