package com.island.models.adventurers;

/**
 * The Engineer class represents a player with the Engineer role in the Forbidden Island game.
 * Engineers have the unique ability to shore up two tiles for one action, but only once per turn.
 */
public class Engineer extends Player {
    /** Flag indicating whether the Engineer can still use their special ability this turn */
    private boolean firstShoreUp = true;

    /**
     * Creates a new Engineer player with the specified username.
     * @param username The name of the player
     */
    public Engineer(String username) {
        super(username);
        setRole(PlayerRole.ENGINEER);
    }

    /**
     * Checks if the Engineer can still use their special ability this turn.
     * @return true if the Engineer can still shore up two tiles at once, false otherwise
     */
    public boolean isFirstShoreUp() {
        return firstShoreUp;
    }

    /**
     * Sets whether the Engineer can use their special ability this turn.
     * @param b true if the Engineer can shore up two tiles at once, false otherwise
     */
    public void setFirstShoreUp(boolean b) {
        firstShoreUp = b;
    }

    /**
     * Resets the Engineer's state at the start of a new turn.
     * This includes resetting the special ability flag.
     */
    @Override
    public void resetState() {
        super.resetState();
        firstShoreUp = true;
    }
}
