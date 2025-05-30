package com.island.models.adventurers;

import java.util.*;

import com.island.models.island.Position;
import com.island.models.island.Tile;

/**
 * The Diver class represents a player with the Diver role in the Forbidden Island game.
 * Divers have the unique ability to swim through flooded tiles and move through them
 * as if they were normal tiles. They can only move orthogonally (up, down, left, right).
 */
public class Diver extends Player{
    /**
     * Creates a new Diver player with the specified name.
     * @param name The name of the player
     */
    public Diver(String name) {
        super(name);
        setRole(PlayerRole.DIVER);
    }

    /**
     * Gets all valid positions the Diver can move to.
     * The Diver can move through flooded tiles and can only move orthogonally.
     * Uses breadth-first search to find all reachable positions.
     * @param tiles A map of all tiles on the board with their positions
     * @return A list of valid positions the Diver can move to
     */
    @Override
    public List<Position> getMovePositions(Map<Position, Tile> tiles) {
        // Use breadth-first search
        List<Position> movePositions = new ArrayList<>();
        List<Position> visited = new ArrayList<>();
        Queue<Position> queue = new LinkedList<>();
        queue.add(getPosition());
        visited.add(getPosition());

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            List<Position> neighbors = getAdjacentPositions(current);
            for (Position neighbor : neighbors) {
                Tile tile = tiles.get(neighbor);
                if (!visited.contains(neighbor) && tile != null) {
                    visited.add(neighbor);
                    if (tile.getState() != Tile.TileState.NORMAL) {
                        queue.add(neighbor);
                    }
                    if (tile.getState() != Tile.TileState.SUNK) {
                        movePositions.add(neighbor);
                    }
                }
            }
        }
        return movePositions;
    }

    /**
     * Gets the adjacent positions (up, down, left, right) for a given position.
     * The Diver can only move orthogonally, not diagonally.
     * @param position The current position
     * @return A list of adjacent positions
     */
    private List<Position> getAdjacentPositions(Position position) {
        List<Position> neighbors = new ArrayList<>();

        // Up
        neighbors.add(new Position(position.getX(), position.getY() - 1));
        // Down
        neighbors.add(new Position(position.getX(), position.getY() + 1));
        // Left
        neighbors.add(new Position(position.getX() - 1, position.getY()));
        // Right
        neighbors.add(new Position(position.getX() + 1, position.getY()));

        return neighbors;
    }
}
