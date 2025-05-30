package com.island.models.adventurers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Navigator player role in the Forbidden Island game.
 * This class contains unit tests to verify the Navigator's ability to move other players
 * and manage their movement state.
 */
class NavigatorTest {
    private Navigator navigator;
    private Player targetPlayer;

    /**
     * Sets up the test environment before each test.
     * Creates a new Navigator player and a target player for testing.
     */
    @BeforeEach
    void setUp() {
        navigator = new Navigator("TestNavigator");
        targetPlayer = new Player("TargetPlayer");
    }

    /**
     * Tests the basic creation and initialization of a Navigator player.
     * Verifies that the player has the correct role, name, and initial state.
     */
    @Test
    void testNavigatorCreation() {
        assertEquals("TestNavigator", navigator.getName());
        assertEquals(PlayerRole.NAVIGATOR, navigator.getRole());
        assertNull(navigator.getNavigatorTarget());
        assertEquals(0, navigator.getNavigatorMoves());
    }

    /**
     * Tests the Navigator's ability to set a target player and number of moves.
     * Verifies that the target player and moves are correctly stored.
     */
    @Test
    void testSetNavigatorTarget() {
        navigator.setNavigatorTarget(targetPlayer, 2);
        assertEquals(targetPlayer, navigator.getNavigatorTarget());
        assertEquals(2, navigator.getNavigatorMoves());
    }

    /**
     * Tests the Navigator's ability to reset their target and remaining moves.
     * Verifies that both the target player and moves are cleared.
     */
    @Test
    void testResetTargetAndMoves() {
        // First set a target and moves
        navigator.setNavigatorTarget(targetPlayer, 2);
        
        // Reset
        navigator.resetTargetAndMoves();
        
        // Check that everything is reset
        assertNull(navigator.getNavigatorTarget());
        assertEquals(0, navigator.getNavigatorMoves());
    }

    /**
     * Tests the Navigator's state reset functionality.
     * Verifies that all state variables are properly reset at the start of a new turn.
     */
    @Test
    void testResetState() {
        // First set some state
        navigator.setNavigatorTarget(targetPlayer, 2);
        navigator.setHasDrawnTreasureCards(true);
        navigator.addDrawnFloodCards(2);
        
        // Reset the state
        navigator.resetState();
        
        // Check that everything is reset
        assertNull(navigator.getNavigatorTarget());
        assertFalse(navigator.isHasDrawnTreasureCards());
        assertEquals(0, navigator.getDrawnFloodCards());
    }
} 