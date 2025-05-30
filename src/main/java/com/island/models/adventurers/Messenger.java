package com.island.models.adventurers;

import java.util.List;

/**
 * The Messenger class represents a player with the Messenger role in the Forbidden Island game.
 * Messengers have the unique ability to give cards to any player on the board, regardless of distance.
 */
public class Messenger extends Player{
    /**
     * Creates a new Messenger player with the specified username.
     * @param username The name of the player
     */
    public Messenger(String username) {
        super(username);
        setRole(PlayerRole.MESSENGER);
    }

    /**
     * Gets the list of players that this Messenger can give cards to.
     * Unlike other roles, Messengers can give cards to any player on the board.
     * @param players The list of all players in the game
     * @return A list of players that can receive cards from this Messenger
     */
    @Override
    public List<Player> getGiveCardPlayers(List<Player> players) {
        // Cannot give cards to oneself
        return  players.stream().filter(player -> !player.equals(this)).toList();
    }
}
