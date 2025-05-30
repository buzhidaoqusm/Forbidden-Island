package com.island.controller;

import com.island.models.Room;
import com.island.models.adventurers.*;
import com.island.models.island.*;
import com.island.models.treasure.TreasureType;
import com.island.models.card.*;
import java.util.*;

import com.island.models.*;

/**
 * Controller class responsible for managing player-related operations.
 * Handles player initialization, role assignment, card management, action validation,
 * and turn state management. This class coordinates all player-specific game mechanics
 * and interactions with other game components.
 */
public class PlayerController {
    /** Reference to the main game controller */
    private GameController gameController;
    /** Reference to the game room */
    private Room room;
    /** Currently selected card by the player */
    private Card chosenCard;

    /**
     * Constructs a new PlayerController.
     */
    public PlayerController() {
    }

    /**
     * Sets the game controller reference and initializes room reference.
     * @param gameController The main game controller
     */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
        room = gameController.getRoomController().getRoom();
    }

    /**
     * Initializes players with random roles and starting positions.
     * Assigns special abilities based on roles and places players on their starting tiles.
     * Uses the provided seed for reproducible randomization.
     * @param seed Random seed for role assignment
     */
    public void initPlayers(long seed) {
        Island island = gameController.getIslandController().getIsland();
        ArrayList<PlayerRole> roles = new ArrayList<>(Arrays.asList(PlayerRole.values()));
        Collections.shuffle(roles, new Random(seed));
        int playerCount = room.getPlayers().size();
        List<Player> players = room.getPlayers();
        for (int i = 0; i < playerCount; i++) {
            Player player = players.get(i);
            switch (roles.get(i)) {
                case DIVER -> player = new Diver(player.getName());
                case ENGINEER -> player = new Engineer(player.getName());
                case EXPLORER -> player = new Explorer(player.getName());
                case MESSENGER -> player = new Messenger(player.getName());
                case NAVIGATOR -> player = new Navigator(player.getName());
                case PILOT -> player = new Pilot(player.getName());
            }
            // Update the host player
            if (room.isHost(player.getName())) {
                room.setHostPlayer(player);
            }
            // Update the current player
            if (gameController.getCurrentPlayer().getName().equals(player.getName())) {
                gameController.setCurrentPlayer(player);
            }
            // Set initial position according to the player role
            player.setPosition(island.findTile(PlayerRole.getColor(player.getRole())));
            room.addPlayer(player);
        }
        // Remove the characters of the first playerCount players from the room
        room.getPlayers().subList(0, playerCount).clear();

        // Update player info after initialization
        if (gameController != null) {
            gameController.updatePlayersInfo();
        }
    }

    /**
     * Deals initial treasure cards to all players.
     * Ensures each player gets exactly 2 non-Waters-Rise cards.
     * Waters-Rise cards are put back into the deck if drawn during initial deal.
     * @param treasureDeck The deck to deal cards from
     */
    public void dealCards(Deque<Card> treasureDeck) {
        for (Player player : room.getPlayers()) {
            while (player.getCards().size() < 2) {
                Card card = treasureDeck.poll();
                if (card != null && card.getType() != CardType.WATER_RISE) {
                    player.addCard(card);
                } else {
                    treasureDeck.add(card);
                }
            }
        }
    }

    // Getters and setters
    public Room getRoom() { return room; }
    public void setChosenCard(Card chosenCard) { this.chosenCard = chosenCard; }
    public Card getChosenCard() { return chosenCard; }

    /**
     * Checks if a player can play a special action card.
     * Valid special cards are Helicopter and Sandbags.
     * @param player Player to check
     * @return true if the player has any special action cards
     */
    public boolean canPlaySpecialCard(Player player) {
        if (player == null) return false;
        for (Card card : player.getCards()) {
            if (card.getType() == CardType.HELICOPTER || card.getType() == CardType.SANDBAGS) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a player can shore up any adjacent tiles.
     * @param player Player to check
     * @return true if there are any valid tiles to shore up
     */
    public boolean canShoreUpTile(Player player) {
        List<Position> validPositions = player.getShorePositions(gameController.getIsland().getTiles());
        return !validPositions.isEmpty();
    }

    /**
     * Checks if a player can give a card to another player.
     * Players must be on the same tile unless the player is a Messenger.
     * @param player Player attempting to give a card
     * @return true if the player can give a card to another player
     */
    public boolean canGiveCard(Player player) {
        if (player.getCards().isEmpty()) return false;
        if (player.getRole() == PlayerRole.MESSENGER) return true;

        // Other roles must be on the same tile
        Position playerPos = player.getPosition();
        if (playerPos == null) return false;
        for (Player tempPlayer : room.getPlayers()) {
            if (tempPlayer.equals(player)) continue;
            if (tempPlayer.getPosition().equals(playerPos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a player can capture a treasure on their current tile.
     * Player must be on a treasure tile and have 4 matching treasure cards.
     * @param player Player attempting to capture treasure
     * @return true if the player can capture a treasure
     */
    public boolean canCaptureTreasure(Player player) {
        Position playerPos = player.getPosition();
        Island island = gameController.getIsland();
        if (playerPos == null) return false;
        Tile currentTile = island.getTile(playerPos);
        if (currentTile == null) return false;

        // Check if player is on a treasure tile and has enough matching cards
        if (currentTile.getTreasureType() != null) {
            int treasureCardCount = 0;
            for (Card card : player.getCards()) {
                if (card.getType() == CardType.TREASURE && card.getTreasureType() == currentTile.getTreasureType()) {
                    treasureCardCount++;
                }
            }
            return treasureCardCount >= 4;
        }
        return false;
    }

    /**
     * Checks if the current player has drawn treasure cards this turn.
     * @return true if treasure cards have been drawn
     */
    public boolean hasDrawnTreasureCards() {
        return room.getCurrentProgramPlayer().isHasDrawnTreasureCards();
    }

    /**
     * Gets the number of flood cards drawn in the current turn.
     * @return Number of flood cards drawn
     */
    public int getDrawnFloodCards() {
        return room.getCurrentProgramPlayer().getDrawnFloodCards();
    }

    /**
     * Sets whether the current player has drawn treasure cards this turn.
     * @param hasDrawnTreasureCards true if treasure cards have been drawn
     */
    public void setHasDrawnTreasureCards(boolean hasDrawnTreasureCards) {
        room.getCurrentProgramPlayer().setHasDrawnTreasureCards(hasDrawnTreasureCards);
    }

    /**
     * Adds to the count of flood cards drawn this turn.
     * @param count Number of flood cards to add to the count
     */
    public void addDrawnFloodCards(int count) {
        room.getCurrentProgramPlayer().addDrawnFloodCards(count);
    }

    /**
     * Resets the state of the current player.
     * Called at the start of each turn to clear previous turn's state.
     */
    public void resetPlayerState() {
        if (room.getCurrentProgramPlayer() != null) {
            room.getCurrentProgramPlayer().resetState();
        }
    }

    /**
     * Cleans up player resources and resets state.
     * Called when shutting down the game.
     */
    public void shutdown() {
        chosenCard = null;
        resetPlayerState();
    }
}

