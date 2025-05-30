package com.island.models.adventurers;

import com.forbiddenisland.models.island.Position;
import com.forbiddenisland.models.island.Tile;
import com.forbiddenisland.models.treasure.TreasureType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Pilot player role in the Forbidden Island game.
 * This class contains unit tests to verify the Pilot's ability to fly to any non-sunk tile
 * on the board once per turn.
 */
class PilotTest {
    private Pilot pilot;
    private Position startPosition;
    private Map<Position, Tile> tiles;

    /**
     * Sets up the test environment before each test.
     * Creates a new Pilot player and initializes a 5x5 grid of tiles.
     */
    @BeforeEach
    void setUp() {
        pilot = new Pilot("TestPilot");
        startPosition = new Position(2, 2);
        pilot.setPosition(startPosition);
        tiles = new HashMap<>();
        
        // Create a 5x5 grid of tiles
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                Position pos = new Position(x, y);
                tiles.put(pos, new Tile("Tile" + x + y, pos, TreasureType.NONE));
            }
        }
    }

    /**
     * Tests the basic creation and initialization of a Pilot player.
     * Verifies that the player has the correct role, name, and initial flying ability.
     */
    @Test
    void testPilotCreation() {
        assertEquals("TestPilot", pilot.getName());
        assertEquals(PlayerRole.PILOT, pilot.getRole());
        // Test initial state by checking move positions
        List<Position> movePositions = pilot.getMovePositions(tiles);
        assertEquals(25, movePositions.size()); // Should be able to move to any non-sunk tile
    }

    /**
     * Tests the Pilot's movement capabilities before using their flying ability.
     * Verifies that the Pilot can move to any non-sunk tile on the board.
     */
    @Test
    void testGetMovePositionsBeforeFlying() {
        List<Position> movePositions = pilot.getMovePositions(tiles);
        
        // Should be able to move to any non-sunk tile
        assertEquals(25, movePositions.size()); // 5x5 grid
        assertTrue(movePositions.contains(new Position(0, 0))); // Far corner
        assertTrue(movePositions.contains(new Position(4, 4))); // Far corner
    }

    /**
     * Tests the Pilot's movement capabilities after using their flying ability.
     * Verifies that the Pilot can only move orthogonally after flying.
     */
    @Test
    void testGetMovePositionsAfterFlying() {
        // Set hasFlewThisTurn to true
        pilot.setHasFlewThisTurn(true);
        
        List<Position> movePositions = pilot.getMovePositions(tiles);
        
        // Should only be able to move to adjacent tiles
        assertEquals(4, movePositions.size()); // Up, down, left, right
        assertTrue(movePositions.contains(new Position(2, 1))); // Up
        assertTrue(movePositions.contains(new Position(2, 3))); // Down
        assertTrue(movePositions.contains(new Position(1, 2))); // Left
        assertTrue(movePositions.contains(new Position(3, 2))); // Right
    }

    /**
     * Tests the Pilot's movement restrictions with sunk tiles.
     * Verifies that the Pilot cannot move to sunk tiles, even with their flying ability.
     */
    @Test
    void testGetMovePositionsWithSunkTiles() {
        // Make some tiles sunk
        Tile cornerTile = tiles.get(new Position(0, 0));
        cornerTile.flood(); // First flood
        cornerTile.flood(); // Then sink
        
        List<Position> movePositions = pilot.getMovePositions(tiles);
        
        // Should not be able to move to sunk tiles
        assertEquals(24, movePositions.size()); // 25 - 1 sunk tile
        assertFalse(movePositions.contains(new Position(0, 0))); // Sunk tile
    }

    /**
     * Tests the Pilot's state reset functionality.
     * Verifies that all state variables are properly reset at the start of a new turn.
     */
    @Test
    void testResetState() {
        // First set some state
        pilot.setHasFlewThisTurn(true);
        pilot.setHasDrawnTreasureCards(true);
        pilot.addDrawnFloodCards(2);
        
        // Reset the state
        pilot.resetState();
        
        // Check that everything is reset by verifying move positions
        List<Position> movePositions = pilot.getMovePositions(tiles);
        assertEquals(25, movePositions.size()); // Should be able to move to any non-sunk tile again
        assertFalse(pilot.isHasDrawnTreasureCards());
        assertEquals(0, pilot.getDrawnFloodCards());
    }
} 