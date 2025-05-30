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
 * Test class for the Explorer player role in the Forbidden Island game.
 * This class contains unit tests to verify the unique abilities of the Explorer,
 * including diagonal movement and shoring up capabilities.
 */
class ExplorerTest {
    private Explorer explorer;
    private Map<Position, Tile> tiles;

    /**
     * Sets up the test environment before each test.
     * Creates a new Explorer player and initializes a 5x5 grid of tiles.
     */
    @BeforeEach
    void setUp() {
        // Create explorer
        explorer = new Explorer("TestExplorer");
        
        // Create a 5x5 grid of tiles
        tiles = new HashMap<>();
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                Position pos = new Position(x, y);
                tiles.put(pos, new Tile("Tile" + x + y, pos, TreasureType.NONE));
            }
        }
        
        // Set explorer's position to center of grid
        Position centerPos = new Position(2, 2);
        explorer.setPosition(centerPos);
    }

    /**
     * Tests the basic creation and initialization of an Explorer player.
     * Verifies that the player has the correct role, name, and initial state.
     */
    @Test
    void testExplorerCreation() {
        // Test basic properties
        assertEquals("TestExplorer", explorer.getName());
        assertEquals(PlayerRole.EXPLORER, explorer.getRole());
        
        // Test inherited properties
        assertNotNull(explorer.getPosition());
        assertNotNull(explorer.getCards());
        assertTrue(explorer.getCards().isEmpty());
        assertFalse(explorer.isHasDrawnTreasureCards());
        assertEquals(0, explorer.getDrawnFloodCards());
        assertTrue(explorer.getCapturedTreasures().isEmpty());
    }

    /**
     * Tests the Explorer's movement capabilities.
     * Verifies that the Explorer can move in all 8 directions (orthogonal and diagonal)
     * from a center position on the board.
     */
    @Test
    void testGetMovePositions() {
        // Get all possible move positions
        List<Position> movePositions = explorer.getMovePositions(tiles);
        
        // Explorer should be able to move in all 8 directions
        assertEquals(9, movePositions.size());
        
        // Verify all 8 directions are available
        Position currentPos = explorer.getPosition();
        assertTrue(movePositions.contains(new Position(currentPos.getX() - 1, currentPos.getY() - 1))); // Up-Left
        assertTrue(movePositions.contains(new Position(currentPos.getX(), currentPos.getY() - 1)));     // Up
        assertTrue(movePositions.contains(new Position(currentPos.getX() + 1, currentPos.getY() - 1))); // Up-Right
        assertTrue(movePositions.contains(new Position(currentPos.getX() - 1, currentPos.getY())));     // Left
        assertTrue(movePositions.contains(new Position(currentPos.getX() + 1, currentPos.getY())));     // Right
        assertTrue(movePositions.contains(new Position(currentPos.getX() - 1, currentPos.getY() + 1))); // Down-Left
        assertTrue(movePositions.contains(new Position(currentPos.getX(), currentPos.getY() + 1)));     // Down
        assertTrue(movePositions.contains(new Position(currentPos.getX() + 1, currentPos.getY() + 1))); // Down-Right
    }

    /**
     * Tests the Explorer's movement restrictions when adjacent to sunk tiles.
     * Verifies that the Explorer cannot move to sunk tiles.
     */
    @Test
    void testGetMovePositionsWithSunkTiles() {
        // Make some tiles sunk
        Position sunkPos = new Position(3, 2); // Right of explorer
        Tile sunkTile = tiles.get(sunkPos);
        sunkTile.flood();
        sunkTile.flood(); // Make it sunk
        
        // Get move positions
        List<Position> movePositions = explorer.getMovePositions(tiles);
        
        // Should not be able to move to sunk tile
        assertFalse(movePositions.contains(sunkPos));
        assertEquals(8, movePositions.size());
    }

    /**
     * Tests the Explorer's ability to shore up flooded tiles.
     * Verifies that the Explorer can shore up tiles in all 8 directions.
     */
    @Test
    void testGetShorePositions() {
        // Flood some tiles
        Position floodedPos1 = new Position(3, 2); // Right
        Position floodedPos2 = new Position(2, 3); // Down
        Position floodedPos3 = new Position(3, 3); // Down-Right
        
        tiles.get(floodedPos1).flood();
        tiles.get(floodedPos2).flood();
        tiles.get(floodedPos3).flood();
        
        // Get shore positions
        List<Position> shorePositions = explorer.getShorePositions(tiles);
        
        // Should be able to shore up all flooded tiles
        assertEquals(3, shorePositions.size());
        assertTrue(shorePositions.contains(floodedPos1));
        assertTrue(shorePositions.contains(floodedPos2));
        assertTrue(shorePositions.contains(floodedPos3));
    }

    /**
     * Tests the Explorer's shoring up restrictions when adjacent to sunk tiles.
     * Verifies that the Explorer cannot shore up sunk tiles.
     */
    @Test
    void testGetShorePositionsWithSunkTiles() {
        // Make a tile sunk
        Position sunkPos = new Position(3, 2); // Right
        Tile sunkTile = tiles.get(sunkPos);
        sunkTile.flood();
        sunkTile.flood(); // Make it sunk
        
        // Flood another tile
        Position floodedPos = new Position(2, 3); // Down
        tiles.get(floodedPos).flood();
        
        // Get shore positions
        List<Position> shorePositions = explorer.getShorePositions(tiles);
        
        // Should only be able to shore up flooded tile, not sunk tile
        assertEquals(1, shorePositions.size());
        assertFalse(shorePositions.contains(sunkPos));
        assertTrue(shorePositions.contains(floodedPos));
    }

    /**
     * Tests the Explorer's movement capabilities when at the edge of the board.
     * Verifies that the Explorer can only move to valid positions when in a corner.
     */
    @Test
    void testGetMovePositionsAtEdge() {
        // Move explorer to edge position
        Position edgePos = new Position(0, 0);
        explorer.setPosition(edgePos);
        
        // Get move positions
        List<Position> movePositions = explorer.getMovePositions(tiles);
        
        // Should only be able to move in 3 directions from corner
        assertEquals(4, movePositions.size());
        assertTrue(movePositions.contains(new Position(1, 0))); // Right
        assertTrue(movePositions.contains(new Position(0, 1))); // Down
        assertTrue(movePositions.contains(new Position(1, 1))); // Down-Right
    }

    /**
     * Tests the Explorer's shoring up capabilities when at the edge of the board.
     * Verifies that the Explorer can shore up all adjacent flooded tiles when in a corner.
     */
    @Test
    void testGetShorePositionsAtEdge() {
        // Move explorer to edge position
        Position edgePos = new Position(0, 0);
        explorer.setPosition(edgePos);
        
        // Flood adjacent tiles
        Position rightPos = new Position(1, 0);
        Position downPos = new Position(0, 1);
        Position diagPos = new Position(1, 1);
        
        tiles.get(rightPos).flood();
        tiles.get(downPos).flood();
        tiles.get(diagPos).flood();
        
        // Get shore positions
        List<Position> shorePositions = explorer.getShorePositions(tiles);
        
        // Should be able to shore up all adjacent flooded tiles
        assertEquals(3, shorePositions.size());
        assertTrue(shorePositions.contains(rightPos));
        assertTrue(shorePositions.contains(downPos));
        assertTrue(shorePositions.contains(diagPos));
    }
} 