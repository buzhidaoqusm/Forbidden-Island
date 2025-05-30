package com.island.controller;

import com.island.models.Room;
import com.island.models.adventurers.*;
import com.island.models.island.*;
import com.island.models.treasure.TreasureType;
import com.island.models.card.*;
import java.util.*;

/**
 * Controller class responsible for managing all card-related operations in the game.
 * This includes managing treasure and flood card decks, handling card drawing and discarding,
 * and managing special card effects.
 */
public class CardController {
    /** Reference to the main game controller */
    private GameController gameController;
    /** Deck of treasure cards */
    private final Deque<Card> treasureDeck;
    /** Deck of flood cards */
    private final Deque<Card> floodDeck;
    /** Discard pile for treasure cards */
    private final List<Card> treasureDiscardPile;
    /** Discard pile for flood cards */
    private final List<Card> floodDiscardPile;
    /** Random seed for shuffling cards */
    private long seed;
    /** Reference to the game board (island) */
    private Island island;
    /** Factory for creating and initializing cards */
    private CardFactory cardFactory;

    /**
     * Constructs a new CardController with the specified card factory.
     * Initializes all card collections.
     * @param cardFactory Factory for creating and initializing cards
     */
    public CardController(CardFactory cardFactory) {
        this.treasureDeck = new ArrayDeque<>();
        this.floodDeck = new ArrayDeque<>();
        this.treasureDiscardPile = new ArrayList<>();
        this.floodDiscardPile = new ArrayList<>();
        this.cardFactory = cardFactory;
    }

    /**
     * Sets the game controller reference and initializes island reference.
     * @param gameController The main game controller
     */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
        island = gameController.getIslandController().getIsland();
    }

    /**
     * Initializes all card decks using the provided random seed.
     * @param seed Random seed for card initialization and shuffling
     */
    public void initCards(long seed) {
        cardFactory.initCards(this, seed);
    }

    /**
     * Draws flood cards and applies their effects to the island.
     * If the flood deck is empty, reshuffles the discard pile.
     * @param count Number of flood cards to draw
     * @return List of positions where tiles were flooded
     */
    public List<Position> drawFloodCards(int count) {
        List<Position> floodedPositions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (floodDeck.isEmpty()) {
                // If deck is empty, reshuffle discard pile
                if (!floodDiscardPile.isEmpty()) {
                    floodDeck.addAll(floodDiscardPile);
                    floodDiscardPile.clear();
                    shuffleDecks();
                } else {
                    break; // If no cards to draw, return
                }
            }

            Card card = floodDeck.poll();
            if (card != null) {
                floodedPositions.add(card.getFloodPosition());
                island.floodTile(card.getFloodPosition());
                floodDiscardPile.add(card);
            }
        }

        // Notify observers after drawing flood cards
        if (gameController != null) {
            gameController.updateCardView();
            gameController.updateBoard();
        }

        return floodedPositions;
    }

    /**
     * Draws treasure cards for a player.
     * Handles water rise cards and reshuffles discard pile if needed.
     * @param count Number of cards to draw
     * @param player Player drawing the cards
     */
    public void drawTreasureCard(int count, Player player) {
        for (int i = 0; i < count; i++) {
            if (treasureDeck.isEmpty()) {
                // If deck is empty, reshuffle discard pile
                if (!treasureDiscardPile.isEmpty()) {
                    treasureDeck.addAll(treasureDiscardPile);
                    treasureDiscardPile.clear();
                    shuffleDecks();
                } else {
                    return; // If no cards to draw, return
                }
            }

            Card card = treasureDeck.poll();
            if (card != null) {
                if (card.getType() == CardType.WATER_RISE) {
                    gameController.handleWaterRise();
                } else {
                    player.addCard(card);
                }
            }
        }

        // Notify observers after drawing treasure cards
        if (gameController != null) {
            gameController.updateCardView();
            gameController.updatePlayersInfo();
        }
    }

    /**
     * Shuffles both treasure and flood decks using the current random seed.
     */
    public void shuffleDecks() {
        Random random = new Random(seed);
        List<Card> tempTreasure = new ArrayList<>(treasureDeck);
        List<Card> tempFlood = new ArrayList<>(floodDeck);
        Collections.shuffle(tempTreasure, random);
        Collections.shuffle(tempFlood, random);
        treasureDeck.clear();
        floodDeck.clear();
        treasureDeck.addAll(tempTreasure);
        floodDeck.addAll(tempFlood);
    }

    // Getters and setters
    public Deque<Card> getTreasureDeck() { return treasureDeck; }
    public Deque<Card> getFloodDeck() { return floodDeck; }
    public List<Card> getFloodDiscardPile() { return floodDiscardPile; }
    public List<Card> getTreasureDiscardPile() { return treasureDiscardPile; }

    /**
     * Adds a card to the treasure discard pile.
     * @param card Card to add to the discard pile
     */
    public void addTreasureDiscardPile(Card card) {
        treasureDiscardPile.add(card);
    }

    /**
     * Handles the water rise event.
     * Reshuffles flood discard pile and adds it back to the top of the flood deck.
     */
    public void handleWaterRise() {
        // Reshuffle flood discard pile and put back on top of deck
        if (!floodDiscardPile.isEmpty()) {
            Collections.shuffle(floodDiscardPile, new Random(seed));
            // Put cards from discard pile back on top of deck
            for (Card card : floodDiscardPile) {
                floodDeck.addFirst(card);
            }
            floodDiscardPile.clear();
        }
        // Add water rise card to discard pile
        treasureDiscardPile.add(Card.createSpecialCard(CardType.WATER_RISE));
    }

    /**
     * Cleans up card resources.
     * Called when shutting down the game.
     */
    public void shutdown() {
        // Clean up card resources
        if (treasureDeck != null) {
            treasureDeck.clear();
        }
        if (floodDeck != null) {
            floodDeck.clear();
        }
        if (treasureDiscardPile != null) {
            treasureDiscardPile.clear();
        }
        if (floodDiscardPile != null) {
            floodDiscardPile.clear();
        }
    }

    // Additional getters and setters
    public void setSeed(long seed) { this.seed = seed; }
    public Island getIsland() { return island; }
    public GameController getGameController() { return gameController; }
}

