package com.forbiddenisland.models.adventurers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessengerTest {
    private Messenger messenger;
    private List<Player> players;

    @BeforeEach
    void setUp() {
        messenger = new Messenger("TestMessenger");
        players = new ArrayList<>();
        players.add(messenger);
        players.add(new Player("Player1"));
        players.add(new Player("Player2"));
    }

    @Test
    void testMessengerCreation() {
        assertEquals("TestMessenger", messenger.getName());
        assertEquals(PlayerRole.MESSENGER, messenger.getRole());
    }

    @Test
    void testGetGiveCardPlayers() {
        List<Player> eligiblePlayers = messenger.getGiveCardPlayers(players);
        
        // Should be able to give cards to all players except self
        assertEquals(2, eligiblePlayers.size());
        assertFalse(eligiblePlayers.contains(messenger));
        assertTrue(eligiblePlayers.stream().anyMatch(p -> p.getName().equals("Player1")));
        assertTrue(eligiblePlayers.stream().anyMatch(p -> p.getName().equals("Player2")));
    }

    @Test
    void testGetGiveCardPlayersWithSinglePlayer() {
        List<Player> singlePlayer = new ArrayList<>();
        singlePlayer.add(messenger);
        
        List<Player> eligiblePlayers = messenger.getGiveCardPlayers(singlePlayer);
        assertTrue(eligiblePlayers.isEmpty());
    }

    @Test
    void testGetGiveCardPlayersWithEmptyList() {
        List<Player> emptyList = new ArrayList<>();
        List<Player> eligiblePlayers = messenger.getGiveCardPlayers(emptyList);
        assertTrue(eligiblePlayers.isEmpty());
    }
} 