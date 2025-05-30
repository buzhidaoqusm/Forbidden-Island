package com.island.models.adventurers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.island.models.island.Position;
import com.island.models.island.Tile;

/**
 * The Explorer class represents a player with the Explorer role in the Forbidden Island game.
 * Explorers have the unique ability to move and shore up tiles diagonally, in addition to
 * the standard orthogonal movements available to all players.
 */
public class Explorer extends Player {
    /**
     * Creates a new Explorer player with the specified username.
     * @param username The name of the player
     */
    public Explorer(String username) {
        super(username);
        setRole(PlayerRole.EXPLORER);
    }

    /**
     * Gets all valid positions the Explorer can move to.
     * Explorers can move in all 8 directions (orthogonal and diagonal) to any non-sunk tile.
     * @param tiles A map of all tiles on the board with their positions
     * @return A list of valid positions the Explorer can move to
     */
    @Override
    public List<Position> getMovePositions(Map<Position, Tile> tiles) {
        List<Position> positions = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                Position newPosition = new Position(getPosition().getX() + dx, getPosition().getY() + dy);
                if (tiles.containsKey(newPosition) && tiles.get(newPosition).getState() != Tile.TileState.SUNK) {
                    positions.add(newPosition);
                }
            }
        }
        return positions;
    }

    /**
     * Gets all valid positions the Explorer can shore up.
     * Explorers can shore up flooded tiles in all 8 directions (orthogonal and diagonal).
     * @param tiles A map of all tiles on the board with their positions
     * @return A list of valid positions the Explorer can shore up
     */
    @Override
    public List<Position> getShorePositions(Map<Position, Tile> tiles) {
        List<Position> positions = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                Position newPosition = new Position(getPosition().getX() + dx, getPosition().getY() + dy);
                if (tiles.containsKey(newPosition) && tiles.get(newPosition).getState() == Tile.TileState.FLOODED) {
                    positions.add(newPosition);
                }
            }
        }
        return positions;
    }
}
