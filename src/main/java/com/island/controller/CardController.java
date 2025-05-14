package com.island.controller;

import com.island.model.*;

import java.util.*;

/**
 * The CardController class manages all card-related functionality in the Forbidden Island game.
 * 
 * This controller:
 * - Initializes and manages the treasure and flood card decks
 * - Handles card drawing, discarding, and shuffling operations
 * - Processes special events triggered by certain cards (like water rise)
 * - Maintains the discard piles for both card types
 * - Coordinates card distribution to players
 * - Ensures proper card recycling when decks are depleted
 * - Works with other controllers to update game state based on card effects
 */
public class CardController {
    /**
     * Reference to the main game controller
     */
    private GameController gameController;
    
    /**
     * The deck of treasure cards (including treasure and special action cards)
     */
    private final Deque<Card> treasureDeck;
    
    /**
     * The deck of flood cards
     */
    private final Deque<Card> floodDeck;
    
    /**
     * Discard pile for used treasure cards
     */
    private final List<Card> treasureDiscardPile;
    
    /**
     * Discard pile for used flood cards
     */
    private final List<Card> floodDiscardPile;
    
    /**
     * Random seed used for shuffling cards deterministically
     */
    private long seed;
    
    /**
     * Reference to the island model to access tile information
     */
    private Island island;


    /**
     * Constructs a new CardController with empty card collections
     */
    public CardController() {
        this.treasureDeck = new ArrayDeque<>();
        this.floodDeck = new ArrayDeque<>();
        this.treasureDiscardPile = new ArrayList<>();
        this.floodDiscardPile = new ArrayList<>();
    }

    /**
     * Establishes a bidirectional link with the game controller and
     * obtains a reference to the island model
     * 
     * @param gameController The main controller for the game
     */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
        island = gameController.getIslandController().getIsland();
    }

    /**
     * Initializes both card decks with the appropriate cards based on the game configuration.
     * Creates treasure cards (5 of each treasure type), special action cards (helicopter, sandbags),
     * water rise cards, and flood cards for every tile on the island.
     * Shuffles both decks and draws initial flood cards to start the game.
     * 
     * @param seed The random seed for deterministic card shuffling
     */
    public void initCards(long seed) {
        this.seed = seed;
        // Initialize the treasure deck
        for (TreasureType type : TreasureType.values()) {
            if (type != TreasureType.NONE) {
                for (int i = 0; i < 4; i++) {
                    treasureDeck.add(Card.createTreasureCard(type, ""));
                }
            }
        }

        // Add special cards
        treasureDeck.add(Card.createSpecialCard(CardType.HELICOPTER));
        treasureDeck.add(Card.createSpecialCard(CardType.HELICOPTER));
        treasureDeck.add(Card.createSpecialCard(CardType.HELICOPTER));
        treasureDeck.add(Card.createSpecialCard(CardType.SANDBAGS));
        treasureDeck.add(Card.createSpecialCard(CardType.SANDBAGS));
        treasureDeck.add(Card.createSpecialCard(CardType.WATER_RISE));
        treasureDeck.add(Card.createSpecialCard(CardType.WATER_RISE));
        treasureDeck.add(Card.createSpecialCard(CardType.WATER_RISE));

        // Initialize the flood deck
        for (Map.Entry<Position, Tile> entry : island.getGameMap().entrySet()) {
            floodDeck.add(Card.createFloodCard(entry.getValue().getName(), entry.getKey(), ""));
        }

        // shuffle decks
        shuffleDecks();

        // draw 6 flood cards
        drawFloodCards(6);

        // update card view
        if (gameController != null) {
            gameController.updateCardView();
        }
    }

    /**
     * Shuffles both the treasure and flood card decks using the provided random seed.
     * Uses a temporary list to perform the shuffle operation, then transfers the cards
     * back to the respective deck queues. This ensures consistent shuffling behavior
     * when using the same seed.
     */
    private void shuffleDecks() {
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

    /**
     * Draws a specified number of flood cards and applies their effects to the island.
     * Each drawn card floods or sinks the corresponding tile on the island board.
     * If the flood deck is empty, reshuffles the discard pile back into the deck.
     * Updates the game board after applying flood effects.
     * 
     * @param count The number of flood cards to draw
     * @return A list of positions on the island that were affected by flooding
     */
    public List<Position> drawFloodCards(int count) {
        List<Position> floodedPositions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (floodDeck.isEmpty()) {
                // If the deck is empty, reshuffle the discard pile
                if (!floodDiscardPile.isEmpty()) {
                    floodDeck.addAll(floodDiscardPile);
                    floodDiscardPile.clear();
                    shuffleDecks();
                } else {
                    break; // If there are no cards left, return
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
     * Draws a specified number of treasure cards for a player.
     * Handles special cards like Water Rise by triggering their effects immediately.
     * If the treasure deck is empty, reshuffles the discard pile back into the deck.
     * Updates the player's hand and game state after card drawing.
     * 
     * @param count The number of treasure cards to draw
     * @param player The player who will receive the drawn cards
     */
    public void drawTreasureCard(int count, Player player) {
        for (int i = 0; i < count; i++) {
            if (treasureDeck.isEmpty()) {
                // if the deck is empty, reshuffle the discard pile
                if (!treasureDiscardPile.isEmpty()) {
                    treasureDeck.addAll(treasureDiscardPile);
                    treasureDiscardPile.clear();
                    shuffleDecks();
                } else {
                    return; // if there are no cards left, return
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

        // notify observers after drawing treasure cards
        if (gameController != null) {
            gameController.updateCardView();
            gameController.updatePlayersInfo();
        }
    }

    /**
     * Handles the water rise event triggered by drawing a water rise card.
     * Shuffles the flood discard pile and adds it back to the flood deck,
     * then adds the water rise card to the treasure discard pile.
     * This increases the frequency of previously flooded tiles appearing again.
     */
    public void handleWaterRise() {
        // shuffle the flood discard pile
        if (!floodDiscardPile.isEmpty()) {
            Collections.shuffle(floodDiscardPile, new Random(seed));
            // add the shuffled cards back to the flood deck
            for (Card card : floodDiscardPile) {
                floodDeck.addFirst(card);
            }
            floodDiscardPile.clear();
        }
        // add a water rise card to the treasure discard pile
        treasureDiscardPile.add(Card.createSpecialCard(CardType.WATER_RISE));
    }

    /**
     * Gets the treasure card deck
     * 
     * @return The deck of treasure cards
     */
    public Deque<Card> getTreasureDeck() {
        return treasureDeck;
    }

    /**
     * Gets the flood card deck
     * 
     * @return The deck of flood cards
     */
    public Deque<Card> getFloodDeck() {
        return floodDeck;
    }

    /**
     * Gets the flood discard pile
     * 
     * @return The list of discarded flood cards
     */
    public List<Card> getFloodDiscardPile() {
        return floodDiscardPile;
    }

    /**
     * Gets the treasure discard pile
     * 
     * @return The list of discarded treasure cards
     */
    public List<Card> getTreasureDiscardPile() {
        return treasureDiscardPile;
    }

    /**
     * Adds a card to the treasure discard pile
     * 
     * @param card The card to be added to the discard pile
     */
    public void addTreasureDiscardPile(Card card) {
        treasureDiscardPile.add(card);
    }

    /**
     * Cleans up resources when the game is shutting down
     */
    public void shutdown() {
    }
}
