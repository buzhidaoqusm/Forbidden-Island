package com.island.models.island;

/**
 * Represents a position on the game board using x and y coordinates.
 * This class is used to track the location of tiles and players on the island.
 */
public class Position {
    private int x;
    private int y;

    /**
     * Creates a new position with the specified coordinates.
     * @param x The x-coordinate
     * @param y The y-coordinate
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the x-coordinate.
     * @return The x-coordinate
     */
    public int getX() { return x; }

    /**
     * Gets the y-coordinate.
     * @return The y-coordinate
     */
    public int getY() { return y; }

    /**
     * Checks if this position is equal to another object.
     * Two positions are equal if they have the same x and y coordinates.
     * @param obj The object to compare with
     * @return true if the positions are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return x == position.x && y == position.y;
    }

    /**
     * Generates a hash code for this position.
     * @return A hash code value for this position
     */
    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    /**
     * Returns a string representation of this position.
     * @return A string in the format "x,y"
     */
    @Override
    public String toString() {
        return x + "," + y;
    }
}