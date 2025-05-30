package com.island.models.island;

import java.util.*;

/**
 * Represents the game board of Forbidden Island.
 * The island consists of a collection of tiles arranged in a grid pattern.
 */
public class Island {
    private final Map<Position, Tile> tiles; // Stores all tiles on the island

    /**
     * Creates a new empty island.
     */
    public Island() {
        this.tiles = new HashMap<>();
    }

    /**
     * Gets all tiles on the island.
     * @return A map of positions to tiles
     */
    public Map<Position, Tile> getTiles() {
        return tiles;
    }

    /**
     * Gets the tile at the specified position.
     * @param position The position to check
     * @return The tile at the position, or null if no tile exists
     */
    public Tile getTile(Position position) {
        try {
            return tiles.get(position);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Finds a tile by its name.
     * @param name The name of the tile to find
     * @return The position of the tile, or null if not found
     */
    public Position findTile(String name) {
        for (Position pos : tiles.keySet()) {
            if (tiles.get(pos).getName().equals(name)) {
                return pos;
            }
        }
        return null;
    }

    /**
     * Floods the tile at the specified position.
     * If the tile is already flooded, it will sink.
     * @param position The position of the tile to flood
     */
    public void floodTile(Position position) {
        Tile tile = tiles.get(position);
        if (tile != null) {
            tile.flood();
        }
    }
}
