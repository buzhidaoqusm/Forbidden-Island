package com.island.models.island;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Position class in the Forbidden Island game.
 * This class contains unit tests to verify the creation and behavior of Position objects,
 * which represent coordinates on the game board.
 */
class PositionTest {
    /**
     * Tests the creation of a Position object.
     * Verifies that the x and y coordinates are correctly stored and retrieved.
     */
    @Test
    void testPositionCreation() {
        Position pos = new Position(2, 3);
        assertEquals(2, pos.getX());
        assertEquals(3, pos.getY());
    }

    /**
     * Tests the equals method of Position objects.
     * Verifies that positions with the same coordinates are considered equal,
     * and positions with different coordinates are considered different.
     * Also tests edge cases like null and self-comparison.
     */
    @Test
    void testEquals() {
        Position pos1 = new Position(2, 3);
        Position pos2 = new Position(2, 3);
        Position pos3 = new Position(3, 2);
        Position pos4 = new Position(2, 4);
        
        assertTrue(pos1.equals(pos2));
        assertTrue(pos2.equals(pos1));
        assertFalse(pos1.equals(pos3));
        assertFalse(pos1.equals(pos4));
        assertFalse(pos1.equals(null));
        assertTrue(pos1.equals(pos1)); // Same object
    }

    /**
     * Tests the hashCode method of Position objects.
     * Verifies that positions with the same coordinates have the same hash code,
     * and positions with different coordinates have different hash codes.
     */
    @Test
    void testHashCode() {
        Position pos1 = new Position(2, 3);
        Position pos2 = new Position(2, 3);
        Position pos3 = new Position(3, 2);
        
        assertEquals(pos1.hashCode(), pos2.hashCode());
        assertNotEquals(pos1.hashCode(), pos3.hashCode());
    }

    /**
     * Tests the toString method of Position objects.
     * Verifies that the string representation of a position is in the format "x,y".
     */
    @Test
    void testToString() {
        Position pos = new Position(2, 3);
        assertEquals("2,3", pos.toString());
    }
} 