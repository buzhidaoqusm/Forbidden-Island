package com.island.models;

import com.island.models.adventurers.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Room class in the Forbidden Island game.
 * This class contains unit tests to verify the creation and management of game rooms,
 * including player management, host assignment, and player retrieval functionality.
 */
class RoomTest {
    private Room room;
    private Player player1;
    private Player player2;

    /**
     * Sets up the test environment before each test.
     * Creates two test players and initializes a room with the first player.
     */
    @BeforeEach
    void setUp() {
        player1 = new Player("Player1");
        player2 = new Player("Player2");
        room = new Room(1, player1);
    }

    /**
     * Tests the creation of a new Room.
     * Verifies that the room is created with the correct ID and initial player.
     */
    @Test
    void testRoomCreation() {
        assertEquals(1, room.getId());
        assertEquals(1, room.getPlayers().size());
        assertTrue(room.getPlayers().contains(player1));
    }

    /**
     * Tests setting and checking the host player of a room.
     * Verifies that:
     * 1. The host player can be set correctly
     * 2. The isHost method correctly identifies the host player
     */
    @Test
    void testSetHostPlayer() {
        room.setHostPlayer(player1);
        assertEquals(player1, room.getHostPlayer());
        assertTrue(room.isHost("Player1"));
        assertFalse(room.isHost("Player2"));
    }

    /**
     * Tests adding a player to the room.
     * Verifies that:
     * 1. The player is successfully added to the room
     * 2. The player count is updated correctly
     */
    @Test
    void testAddPlayer() {
        room.addPlayer(player2);
        assertEquals(2, room.getPlayers().size());
        assertTrue(room.getPlayers().contains(player2));
    }

    /**
     * Tests removing a player from the room.
     * Verifies that:
     * 1. The player is successfully removed from the room
     * 2. The player count is updated correctly
     */
    @Test
    void testRemovePlayer() {
        room.addPlayer(player2);
        room.removePlayer(player2);
        assertEquals(1, room.getPlayers().size());
        assertFalse(room.getPlayers().contains(player2));
    }

    /**
     * Tests retrieving the current program player.
     * Verifies that the correct player is returned as the current program player.
     */
    @Test
    void testGetCurrentProgramPlayer() {
        assertEquals(player1, room.getCurrentProgramPlayer());
    }

    /**
     * Tests setting a new list of players for the room.
     * Verifies that:
     * 1. The player list is updated correctly
     * 2. The first player in the new list becomes the host
     */
    @Test
    void testSetPlayers() {
        ArrayList<Player> newPlayers = new ArrayList<>();
        newPlayers.add(player2);
        newPlayers.add(player1);
        room.setPlayers(newPlayers);
        assertEquals(2, room.getPlayers().size());
        assertEquals(player2, room.getHostPlayer());
    }

    /**
     * Tests retrieving a player by their username.
     * Verifies that:
     * 1. Existing players can be found by their username
     * 2. Attempting to find a non-existent player returns null
     */
    @Test
    void testGetPlayerByUsername() {
        room.addPlayer(player2);
        assertEquals(player1, room.getPlayerByUsername("Player1"));
        assertEquals(player2, room.getPlayerByUsername("Player2"));
        assertNull(room.getPlayerByUsername("NonExistentPlayer"));
    }
} 