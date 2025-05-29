package com.forbiddenisland.models.adventurers;

import com.forbiddenisland.models.*;
import com.forbiddenisland.models.card.Card;
import com.forbiddenisland.models.island.Position;
import com.forbiddenisland.models.island.Tile;
import com.forbiddenisland.models.treasure.TreasureType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a player in the Forbidden Island game.
 * Each player has a name, position on the island, cards, role, and can capture treasures.
 */
public class Player {
    private String name;
    private Position position;
    private List<Card> cards;
    private PlayerRole role;
    private boolean hasDrawnTreasureCards;
    private int drawnFloodCards;
    private List<TreasureType> capturedTreasures;

    /**
     * Creates a new player with the specified name.
     * @param name The name of the player
     */
    public Player(String name) {
        this.cards = new ArrayList<>();
        this.name = name;
        this.hasDrawnTreasureCards = false;
        this.drawnFloodCards = 0;
        this.capturedTreasures = new ArrayList<>();
    }

    /**
     * Gets the player's name.
     * @return The player's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the player's name.
     * @param name The new name for the player
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the player's current position on the island.
     * @return The player's position
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Sets the player's position on the island.
     * @param position The new position for the player
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * Gets the player's cards.
     * @return List of cards held by the player
     */
    public List<Card> getCards() {
        return cards;
    }

    /**
     * Sets the player's cards.
     * @param cards The new list of cards for the player
     */
    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    /**
     * Checks if the player has drawn treasure cards this turn.
     * @return true if the player has drawn treasure cards, false otherwise
     */
    public boolean isHasDrawnTreasureCards() {
        return hasDrawnTreasureCards;
    }

    /**
     * Sets whether the player has drawn treasure cards this turn.
     * @param hasDrawnTreasureCards The new state
     */
    public void setHasDrawnTreasureCards(boolean hasDrawnTreasureCards) {
        this.hasDrawnTreasureCards = hasDrawnTreasureCards;
    }

    /**
     * Gets the number of flood cards drawn by the player.
     * @return The number of flood cards drawn
     */
    public int getDrawnFloodCards() {
        return drawnFloodCards;
    }

    /**
     * Adds to the count of flood cards drawn by the player.
     * @param count The number of flood cards to add
     */
    public void addDrawnFloodCards(int count) {
        this.drawnFloodCards += count;
    }

    /**
     * Adds a captured treasure to the player's collection.
     * @param treasureType The type of treasure captured
     */
    public void addCapturedTreasure(TreasureType treasureType) {
        capturedTreasures.add(treasureType);
    }

    /**
     * Gets the player's role.
     * @return The player's role
     */
    public PlayerRole getRole() {
        return role;
    }

    /**
     * Sets the player's role.
     * @param role The new role for the player
     */
    public void setRole(PlayerRole role) {
        this.role = role;
    }

    /**
     * Adds a card to the player's hand.
     * @param card The card to add
     */
    public void addCard(Card card) {
        cards.add(card);
        card.setBelongingPlayer(name);
    }

    /**
     * Gets the valid positions the player can move to.
     * @param tiles The map of all tiles on the island
     * @return List of valid positions the player can move to
     */
    public List<Position> getMovePositions(Map<Position, Tile> tiles) {
        List<Position> validPositions = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (Math.abs(dx) + Math.abs(dy) > 1 || dx == 0 && dy == 0) continue;
                Position newPosition = new Position(position.getX() + dx, position.getY() + dy);
                if (tiles.containsKey(newPosition) && tiles.get(newPosition).getState() != Tile.TileState.SUNK) {
                    validPositions.add(newPosition);
                }
            }
        }
        return validPositions;
    }

    /**
     * Gets the positions of flooded tiles that the player can shore up.
     * @param tiles The map of all tiles on the island
     * @return List of positions of flooded tiles that can be shored up
     */
    public List<Position> getShorePositions(Map<Position, Tile> tiles) {
        List<Position> validPositions = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (Math.abs(dx) + Math.abs(dy) > 1) continue;
                Position newPosition = new Position(position.getX() + dx, position.getY() + dy);
                if (tiles.containsKey(newPosition) && tiles.get(newPosition).getState() == Tile.TileState.FLOODED) {
                    validPositions.add(newPosition);
                }
            }
        }
        return validPositions;
    }

    /**
     * Resets the player's state for a new turn.
     */
    public void resetState() {
        this.hasDrawnTreasureCards = false;
        this.drawnFloodCards = 0;
    }

    /**
     * Gets the list of players that this player can give cards to.
     * @param players The list of all players in the game
     * @return List of players that can receive cards from this player
     */
    public List<Player> getGiveCardPlayers(List<Player> players) {
        List<Player> eligiblePlayers = new ArrayList<>();
        for (Player player : players) {
            if (!player.equals(this) && player.getPosition().equals(position)) {
                eligiblePlayers.add(player);
            }
        }
        return  eligiblePlayers;
    }

    /**
     * Removes a card from the player's hand by name.
     * @param cardName The name of the card to remove
     * @return The removed card, or null if not found
     */
    public Card removeCard(String cardName) {
        for (Card card : cards) {
            if (card.getName().equals(cardName)) {
                cards.remove(card);
                card.setBelongingPlayer("");
                return card;
            }
        }
        return null;
    }

    /**
     * Removes a card from the player's hand by index.
     * @param cardIndex The index of the card to remove
     * @return The removed card
     */
    public Card removeCard(int cardIndex) {
        cards.get(cardIndex).setBelongingPlayer("");
        return cards.remove(cardIndex);
    }

    /**
     * Gets the list of treasures captured by the player.
     * @return List of captured treasures
     */
    public List<TreasureType> getCapturedTreasures() {
        return capturedTreasures;
    }
}
