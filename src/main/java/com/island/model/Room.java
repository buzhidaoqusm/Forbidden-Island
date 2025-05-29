package com.island.model;

import com.forbiddenisland.models.adventurers.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a game room that contains players and manages the game session.
 * Each room has a unique ID, a list of players, and a host player who controls the game.
 */
public class Room {
    private int id;
    private List<Player> players;
    private Player hostPlayer;
    private int currentProgramPlayerIndex;

    /**
     * Creates a new room with the specified ID and initial player.
     * @param id The unique identifier for the room
     * @param player The first player to join the room
     */
    public Room(int id, Player player) {
        this.id = id;
        this.hostPlayer = null;
        this.players = new ArrayList<>();
        this.players.add(player);
        this.currentProgramPlayerIndex = 0;
    }

    /**
     * Sets the host player for this room.
     * @param hostPlayer The player to be set as host
     */
    public void setHostPlayer(Player hostPlayer) {
        this.hostPlayer = hostPlayer;
    }

    /**
     * Gets the list of players in this room.
     * @return List of players in the room
     */
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * Gets the room's unique identifier.
     * @return The room ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the current program player.
     * @return The current program player
     */
    public Player getCurrentProgramPlayer() {
        return players.get(currentProgramPlayerIndex);
    }

    /**
     * Checks if a player with the given username is the host.
     * @param username The username to check
     * @return true if the player is the host, false otherwise
     */
    public boolean isHost(String username) {
        return hostPlayer != null && hostPlayer.getName().equals(username);
    }

    /**
     * Adds a new player to the room.
     * @param player The player to add
     */
    public void addPlayer(Player player) {
        players.add(player);
    }

    /**
     * Removes a player from the room.
     * @param player The player to remove
     */
    public void removePlayer(Player player) {
        players.remove(player);
    }

    /**
     * Gets the host player of the room.
     * @return The host player
     */
    public Player getHostPlayer() {
        return hostPlayer;
    }

    /**
     * Sets the list of players and updates the current program player index and host.
     * @param players The new list of players
     */
    public void setPlayers(ArrayList<Player> players) {
        Player player = this.players.get(currentProgramPlayerIndex);
        this.players = players;
        // Find the index of the current program player
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getName().equals(player.getName())) {
                currentProgramPlayerIndex = i;
                break;
            }
        }
        // Update the host player
        hostPlayer = players.get(0);
    }

    /**
     * Gets a player by their username.
     * @param username The username to search for
     * @return The player with the matching username, or null if not found
     */
    public Player getPlayerByUsername(String username) {
        for (Player player : players) {
            if (player.getName().equals(username)) {
                return player;
            }
        }
        return null;
    }
}
