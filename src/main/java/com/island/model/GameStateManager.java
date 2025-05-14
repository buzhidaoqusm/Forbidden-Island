package com.island.model;

/**
 * Singleton class to manage the game state
 */
public class GameStateManager {
    private static GameStateManager instance;
    private Island island;

    private GameStateManager() {
        this.island = new Island();
    }

    public static GameStateManager getInstance() {
        if (instance == null) {
            instance = new GameStateManager();
        }
        return instance;
    }

    public Island getIsland() {
        return island;
    }

    public void setIsland(Island island) {
        this.island = island;
    }

    public void handleShoreUp(Player player, Position position) {
        // Handle shore up event
        if (player != null && position != null) {
            Tile tile = island.getTile(position);
            if (tile != null) {
                tile.shoreUp();
            }
        }
    }
} 