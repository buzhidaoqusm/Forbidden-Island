package com.island.models.adventurers;

/**
 * The Navigator class represents a player with the Navigator role in the Forbidden Island game.
 * Navigators have the unique ability to move other players up to two tiles per turn.
 */
public class Navigator extends Player{
    /** The player that the Navigator is currently moving */
    private Player navigatorTarget;
    /** The number of moves remaining for the Navigator's target */
    private int navigatorMoves;

    /**
     * Creates a new Navigator player with the specified name.
     * @param name The name of the player
     */
    public Navigator(String name) {
        super(name);
        setRole(PlayerRole.NAVIGATOR);
    }

    /**
     * Sets the target player and number of moves for the Navigator's special ability.
     * @param player The player to be moved by the Navigator
     * @param moves The number of moves available for the target player
     */
    public void setNavigatorTarget(Player player, int moves) {
        this.navigatorTarget = player;
        this.navigatorMoves = moves;
    }

    /**
     * Gets the player that the Navigator is currently moving.
     * @return The target player, or null if no player is being moved
     */
    public Player getNavigatorTarget() {
        return navigatorTarget;
    }

    /**
     * Gets the number of moves remaining for the Navigator's target.
     * @return The number of moves available
     */
    public int getNavigatorMoves() {
        return navigatorMoves;
    }

    /**
     * Resets the Navigator's target and remaining moves.
     * This is called when the Navigator's turn ends or when the target player has used all their moves.
     */
    public void resetTargetAndMoves() {
        navigatorTarget = null;
        navigatorMoves = 0;
    }

    /**
     * Resets the Navigator's state at the start of a new turn.
     * This includes resetting the target player and remaining moves.
     */
    @Override
    public void resetState() {
        super.resetState();
        resetTargetAndMoves();
    }
}
