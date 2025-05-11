package com.island.model;

public class Tile {
    private String name;
    private Position position;
    private TileState state;
    private TreasureType treasureType;
    private boolean isShoredUp;  // New field to track if tile is shored up

    public Tile(String name, Position position, TreasureType treasureType) {
        this.name = name;
        this.position = position;
        this.treasureType = treasureType;
        this.state = TileState.NORMAL;
        this.isShoredUp = false;
    }

    /**
     * Flood the tile, changing its state from NORMAL to FLOODED, or from FLOODED to SUNK
     * @return true if the tile's state was changed, false otherwise
     */
    public boolean flood() {
        if (state == TileState.NORMAL) {
            state = TileState.FLOODED;
            isShoredUp = false;  // Reset shored up status when flooded
            return true;
        } else if (state == TileState.FLOODED) {
            state = TileState.SUNK;
            return true;
        }
        return false;
    }

    /**
     * Shore up the tile, changing its state from FLOODED to NORMAL
     * @return true if the tile was successfully shored up, false otherwise
     */
    public boolean shoreUp() {
        if (state == TileState.FLOODED) {
            state = TileState.NORMAL;
            isShoredUp = true;
            return true;
        }
        return false;
    }

    /**
     * Check if the tile is sunk
     * @return true if the tile is sunk, false otherwise
     */
    public boolean isSunk() {
        return state == TileState.SUNK;
    }

    /**
     * Check if the tile is flooded
     * @return true if the tile is flooded, false otherwise
     */
    public boolean isFlooded() {
        return state == TileState.FLOODED;
    }

    /**
     * Check if the tile is in normal state
     * @return true if the tile is in normal state, false otherwise
     */
    public boolean isNormal() {
        return state == TileState.NORMAL;
    }

    /**
     * Check if the tile is shored up
     * @return true if the tile is shored up, false otherwise
     */
    public boolean isShoredUp() {
        return isShoredUp;
    }

    /**
     * Get the name of the tile
     * @return the tile's name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the position of the tile
     * @return the tile's position
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Get the current state of the tile
     * @return the tile's state
     */
    public TileState getState() {
        return state;
    }

    /**
     * Get the treasure type associated with the tile
     * @return the tile's treasure type, or null if no treasure
     */
    public TreasureType getTreasureType() {
        return treasureType;
    }

    /**
     * Set the treasure type for the tile
     * @param treasureType the treasure type to set
     */
    public void setTreasureType(TreasureType treasureType) {
        this.treasureType = treasureType;
    }

    /**
     * Check if the tile is safe for players to be on
     * @return true if the tile is safe (not sunk), false otherwise
     */
    public boolean isSafe() {
        return !isSunk();
    }

    /**
     * Check if the tile can be flooded
     * @return true if the tile can be flooded (not sunk), false otherwise
     */
    public boolean canBeFlooded() {
        return !isSunk();
    }

    /**
     * Check if the tile can be shored up
     * @return true if the tile can be shored up (is flooded), false otherwise
     */
    public boolean canBeShoredUp() {
        return isFlooded();
    }
}