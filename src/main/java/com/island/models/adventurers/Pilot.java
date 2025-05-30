package com.island.models.adventurers;

import java.util.List;
import java.util.Map;

import com.island.models.island.Position;
import com.island.models.island.Tile;

/**
 * The Pilot class represents a player with the Pilot role in the Forbidden Island game.
 * Pilots have the unique ability to fly to any non-sunk tile on the board once per turn.
 */
public class Pilot extends Player{
    /** Flag indicating whether the Pilot has used their flying ability this turn */
    private boolean hasFlewThisTurn = false;

    /**
     * Creates a new Pilot player with the specified username.
     * @param username The name of the player
     */
    public Pilot(String username) {
        super(username);
        setRole(PlayerRole.PILOT);
    }

    /**
     * Gets all valid positions the Pilot can move to.
     * If the Pilot hasn't used their flying ability this turn, they can move to any non-sunk tile.
     * Otherwise, they follow normal movement rules.
     * @param tiles A map of all tiles on the board with their positions
     * @return A list of valid positions the Pilot can move to
     */
    @Override
    public List<Position> getMovePositions(Map<Position, Tile> tiles) {
        if (hasFlewThisTurn) {
            return super.getMovePositions(tiles);
        } else {
            // Select all non-sunk tiles
            return tiles.keySet().stream()
                    .filter(p -> !tiles.get(p).isSunk())
                    .toList();
        }
    }

    /**
     * Resets the Pilot's state at the start of a new turn.
     * This includes resetting the flying ability flag.
     */
    @Override
    public void resetState() {
        super.resetState();
        hasFlewThisTurn = false;
    }

    /**
     * Sets whether the Pilot has used their flying ability this turn.
     * @param b true if the Pilot has flown this turn, false otherwise
     */
    public void setHasFlewThisTurn(boolean b) {
        hasFlewThisTurn = b;
    }
}
