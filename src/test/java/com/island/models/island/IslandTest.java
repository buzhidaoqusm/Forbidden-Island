package com.forbiddenisland.models.island;

import com.forbiddenisland.models.treasure.TreasureType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Island class in the Forbidden Island game.
 * This class contains unit tests to verify the creation and management of the game board,
 * including tile placement, retrieval, and flooding functionality.
 */
class IslandTest {
    private Island island;
    private Position pos1;
    private Position pos2;
    private Tile tile1;
    private Tile tile2;

    /**
     * Sets up the test environment before each test.
     * Creates a new Island and adds two test tiles with different positions and treasure types.
     */
    @BeforeEach
    void setUp() {
        island = new Island();
        pos1 = new Position(2, 3);
        pos2 = new Position(3, 4);
        tile1 = new Tile("Tile1", pos1, TreasureType.FIRE_CRYSTAL);
        tile2 = new Tile("Tile2", pos2, TreasureType.OCEAN_CHALICE);
        
        // Add tiles to the island
        Map<Position, Tile> tiles = island.getTiles();
        tiles.put(pos1, tile1);
        tiles.put(pos2, tile2);
    }

    /**
     * Tests the creation of a new Island.
     * Verifies that a new island is created with an empty tile map.
     */
    @Test
    void testIslandCreation() {
        Island emptyIsland = new Island();
        assertNotNull(emptyIsland.getTiles());
        assertTrue(emptyIsland.getTiles().isEmpty());
    }

    /**
     * Tests retrieving all tiles from the island.
     * Verifies that the tile map contains the correct number of tiles
     * and that tiles can be retrieved by their positions.
     */
    @Test
    void testGetTiles() {
        Map<Position, Tile> tiles = island.getTiles();
        assertEquals(2, tiles.size());
        assertEquals(tile1, tiles.get(pos1));
        assertEquals(tile2, tiles.get(pos2));
    }

    /**
     * Tests retrieving a specific tile from the island.
     * Verifies that:
     * 1. Existing tiles can be retrieved by their position
     * 2. Attempting to get a non-existent tile returns null
     */
    @Test
    void testGetTile() {
        // Test getting existing tile
        assertEquals(tile1, island.getTile(pos1));
        assertEquals(tile2, island.getTile(pos2));

        // Test getting non-existent tile
        Position nonExistentPos = new Position(0, 0);
        assertNull(island.getTile(nonExistentPos));
    }

    /**
     * Tests finding a tile's position by its name.
     * Verifies that:
     * 1. Existing tiles can be found by their name
     * 2. Attempting to find a non-existent tile returns null
     */
    @Test
    void testFindTile() {
        // Test finding existing tile
        assertEquals(pos1, island.findTile("Tile1"));
        assertEquals(pos2, island.findTile("Tile2"));

        // Test finding non-existent tile
        assertNull(island.findTile("NonExistentTile"));
    }

    /**
     * Tests flooding a tile on the island.
     * Verifies that:
     * 1. Existing tiles can be flooded
     * 2. Attempting to flood a non-existent tile has no effect
     */
    @Test
    void testFloodTile() {
        // Test flooding existing tile
        island.floodTile(pos1);
        assertEquals(Tile.TileState.FLOODED, tile1.getState());

        // Test flooding non-existent tile
        Position nonExistentPos = new Position(0, 0);
        island.floodTile(nonExistentPos); // Should not throw exception
    }

    /**
     * Tests flooding a tile twice.
     * Verifies that flooding a tile twice causes it to sink.
     */
    @Test
    void testFloodTileTwice() {
        // Test flooding a tile twice (should sink)
        island.floodTile(pos1);
        island.floodTile(pos1);
        assertEquals(Tile.TileState.SUNK, tile1.getState());
    }
} 