package com.island.model;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private List<Player> players;
    private Player hostPlayer;
    private Player currentProgramPlayer;
    private String roomId;

    public Room() {
        this.players = new ArrayList<>();
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Player getHostPlayer() {
        return hostPlayer;
    }

    public void setHostPlayer(Player hostPlayer) {
        this.hostPlayer = hostPlayer;
    }

    public Player getCurrentProgramPlayer() {
        return currentProgramPlayer;
    }

    public void setCurrentProgramPlayer(Player currentProgramPlayer) {
        this.currentProgramPlayer = currentProgramPlayer;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public boolean isHost(String playerName) {
        return hostPlayer != null && hostPlayer.getName().equals(playerName);
    }

    public boolean addPlayer(Player player) {
        if (player != null && !players.contains(player)) {
            players.add(player);
        }
        return false;
    }

    public void removePlayer(Player player) {
        if (player != null) {
            players.remove(player);
        }
    }

    public void startGame() {
        // Check if the room meets the conditions to start the game
        if (players.size() < 2 || players.size() > 4) {
            throw new IllegalStateException("Game requires 2-4 players to start");
        }

        // Check if all players are ready
        for (Player player : players) {
            if (!player.isReady()) {
                throw new IllegalStateException("Some players are not ready");
            }
        }

        // Initialize player states
        for (Player player : players) {
            player.resetState();
            player.setInGame(true);
        }
    }

    /**
     * Sets a player as ready
     * @param playerName The name of the player to set as ready
     * @param isReady The ready status to set
     */
    public void setPlayerReady(String playerName, boolean isReady) {
        for (Player player : players) {
            if (player.getName().equals(playerName)) {
                player.setReady(isReady);
                break;
    }
        }
    }

    /**
     * Gets a player by their name
     * @param playerName The name of the player to find
     * @return The player if found, null otherwise
     */
    public Player getPlayerByName(String playerName) {
        for (Player player : players) {
            if (player.getName().equals(playerName)) {
                return player;
    }
        }
        return null;
    }

    /**
     * Checks if all players are ready
     * @return true if all players are ready, false otherwise
     */
    public boolean areAllPlayersReady() {
        for (Player player : players) {
            if (!player.isReady()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the number of players in the room
     * @return The number of players
     */
    public int getPlayerCount() {
        return players.size();
    }

    /**
     * Checks if the room is full
     * @return true if the room has 4 players, false otherwise
     */
    public boolean isFull() {
        return players.size() >= 4;
    }
}