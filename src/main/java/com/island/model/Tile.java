package com.island.model;

import com.island.util.Constant;

public class Tile {
    private String name;
    private Position position;
    private TileState state;
    private TreasureType treasureType;
    private boolean isShoredUp;

    public Tile(String name, Position position, TreasureType treasureType) {
        this.name = Constant.standardizeTileName(name);
        this.position = position;
        this.treasureType = treasureType;
        this.state = TileState.NORMAL;
        this.isShoredUp = false;
        System.out.println("Created new tile: " + this.name + " at position " + position + 
                          (treasureType != null ? " with treasure type " + treasureType : ""));
    }

    /**
     * Flood the tile, changing its state from NORMAL to FLOODED, or from FLOODED to SUNK
     * @return true if the tile's state was changed, false otherwise
     */
    public boolean flood() {
        if (state == TileState.NORMAL) {
            state = TileState.FLOODED;
            isShoredUp = false;
            System.out.println("Tile " + name + " at " + position + " is now flooded");
            return true;
        } else if (state == TileState.FLOODED) {
            state = TileState.SUNK;
            System.out.println("Tile " + name + " at " + position + " has sunk!");
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
            System.out.println("Tile " + name + " at " + position + " has been shored up");
            return true;
        }
        System.out.println("Cannot shore up tile " + name + " at " + position + " (current state: " + state + ")");
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
     * Set the state of the tile
     * @param state the new state for the tile
     */
    public void setState(TileState state) {
        TileState oldState = this.state;
        this.state = state;
        System.out.println("Tile " + name + " at " + position + " state changed from " + oldState + " to " + state);
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

    /**
     * Set the shored up status of the tile
     * @param shoredUp the new shored up status for the tile
     */
    public void setShoredUp(boolean shoredUp) {
        this.isShoredUp = shoredUp;
        System.out.println("Tile " + name + " at " + position + " shored up status set to " + shoredUp);
    }

    @Override
    public String toString() {
        return "Tile{" +
               "name='" + name + '\'' +
               ", position=" + position +
               ", state=" + state +
               ", treasureType=" + treasureType +
               ", isShoredUp=" + isShoredUp +
               '}';
    }
}