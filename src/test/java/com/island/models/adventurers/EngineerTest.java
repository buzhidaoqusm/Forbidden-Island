package com.island.models.adventurers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Engineer player role in the Forbidden Island game.
 * This class contains unit tests to verify the Engineer's ability to shore up two tiles
 * at once and manage their special ability state.
 */
class EngineerTest {
    private Engineer engineer;

    /**
     * Sets up the test environment before each test.
     * Creates a new Engineer player for testing.
     */
    @BeforeEach
    void setUp() {
        engineer = new Engineer("TestEngineer");
    }

    /**
     * Tests the basic creation and initialization of an Engineer player.
     * Verifies that the player has the correct role, name, and initial state.
     */
    @Test
    void testEngineerCreation() {
        assertEquals("TestEngineer", engineer.getName());
        assertEquals(PlayerRole.ENGINEER, engineer.getRole());
        assertTrue(engineer.isFirstShoreUp());
    }

    /**
     * Tests the Engineer's ability to set and check their special ability state.
     * Verifies that the firstShoreUp flag can be properly toggled.
     */
    @Test
    void testSetFirstShoreUp() {
        engineer.setFirstShoreUp(false);
        assertFalse(engineer.isFirstShoreUp());
        
        engineer.setFirstShoreUp(true);
        assertTrue(engineer.isFirstShoreUp());
    }

    /**
     * Tests the Engineer's state reset functionality.
     * Verifies that all state variables are properly reset at the start of a new turn.
     */
    @Test
    void testResetState() {
        // First set some state
        engineer.setFirstShoreUp(false);
        engineer.setHasDrawnTreasureCards(true);
        engineer.addDrawnFloodCards(2);
        
        // Reset the state
        engineer.resetState();
        
        // Check that everything is reset
        assertTrue(engineer.isFirstShoreUp());
        assertFalse(engineer.isHasDrawnTreasureCards());
        assertEquals(0, engineer.getDrawnFloodCards());
    }
} 