package com.island.models.island;

import com.island.models.treasure.TreasureType;

/**
 * Represents a tile on the game board.
 * Each tile has a name, position, state, and may contain a treasure.
 */
public class Tile {

    /**
     * Represents the possible states of a tile.
     */
    public enum TileState {
        NORMAL,     // Normal state
        FLOODED,    // Flooded state
        SUNK        // Sunk state
    }

    private final String name;           // Name of the tile
    private final Position position;     // Position of the tile
    private TileState state;            // Current state of the tile
    private final TreasureType treasureType;  // Type of treasure (if any)

    /**
     * Creates a new tile with the specified properties.
     * @param name The name of the tile
     * @param position The position of the tile
     * @param treasureType The type of treasure on the tile (if any)
     */
    public Tile(String name, Position position, TreasureType treasureType) {
        this.name = name;
        this.position = position;
        this.state = TileState.NORMAL;
        this.treasureType = treasureType;
    }

    /**
     * Gets the name of the tile.
     * @return The tile name
     */
    public String getName() { return name; }

    /**
     * Gets the position of the tile.
     * @return The tile position
     */
    public Position getPosition() { return position; }

    /**
     * Gets the current state of the tile.
     * @return The tile state
     */
    public TileState getState() { return state; }

    /**
     * Gets the type of treasure on the tile.
     * @return The treasure type, or null if no treasure
     */
    public TreasureType getTreasureType() { return treasureType; }

    /**
     * Floods the tile. If the tile is already flooded, it will sink.
     */
    public void flood() {
        if (state == TileState.NORMAL) {
            state = TileState.FLOODED;
        } else if (state == TileState.FLOODED) {
            state = TileState.SUNK;
        }
    }

    /**
     * Checks if the tile is sunk.
     * @return true if the tile is sunk, false otherwise
     */
    public boolean isSunk() {
        return state == TileState.SUNK;
    }

    /**
     * Checks if the tile is flooded.
     * @return true if the tile is flooded, false otherwise
     */
    public boolean isFlooded() {
        return state == TileState.FLOODED;
    }

    /**
     * Shores up the tile, returning it to normal state if it was flooded.
     */
    public void shoreUp() {
        if (this.state == TileState.FLOODED) {
            this.state = TileState.NORMAL;
        }
    }

    /**
     * Checks if the tile is in normal state.
     * @return true if the tile is normal, false otherwise
     */
    public boolean isNormal() {
        return state == TileState.NORMAL;
    }
}