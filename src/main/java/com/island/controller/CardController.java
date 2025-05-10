package com.island.controller;

import com.island.model.*;

import java.util.*;

/**
 * CardController is responsible for managing the card system in the game, including the creation, distribution, shuffling, and discarding of treasure cards and flood cards.
 * This class coordinates the interaction between the game controller and the cards, and handles the logic of card extraction, special card processing, and water level rise.
 * */
public class CardController {
    private GameController gameController;
    // Card management related fields
    private final Deque<Card> treasureDeck;
    private final Deque<Card> floodDeck;
    private final List<Card> treasureDiscardPile;
    private final List<Card> floodDiscardPile;
    private long seed;
    private Island island;


    public CardController() {
        this.treasureDeck = new ArrayDeque<>();
        this.floodDeck = new ArrayDeque<>();
        this.treasureDiscardPile = new ArrayList<>();
        this.floodDiscardPile = new ArrayList<>();
    }

    /**
     * Set up the game controller and initialize the island object.
     * @param gameController the GameController
     * */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
        island = gameController.getIslandController().getIsland();
    }

    /**
     * Initialize the deck, including the creation of treasure cards and flood cards, shuffling, and drawing the initial flood card.
     * @param seed The seed for randomization.
     * */
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
        for (Map.Entry<Position, Tile> entry : island.getTiles().entrySet()) {
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
     * Shuffle the Treasure and Flood card decks.
     * */
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
     * Draws a specified number of flood cards and floods the corresponding island positions.
     * If the flood deck is empty, it reshuffles the discard pile and adds it back to the deck.
     * @param count The number of flood cards to draw
     * @return A list of island positions that got flooded
     * */
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
     * If the treasure deck is empty, it reshuffles the discard pile and adds it back to the deck.
     * @param count The number of treasure cards to draw
     * @param player The player who is drawing the cards
     * */
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
     * Shuffles the flood discard pile and adds a water rise card to the treasure discard pile.
     * */
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

    public Deque<Card> getTreasureDeck() {
        return treasureDeck;
    }

    public Deque<Card> getFloodDeck() {
        return floodDeck;
    }

    public List<Card> getFloodDiscardPile() {
        return floodDiscardPile;
    }

    public List<Card> getTreasureDiscardPile() {
        return treasureDiscardPile;
    }

    public void addTreasureDiscardPile(Card card) {
        treasureDiscardPile.add(card);
    }



    public void shutdown() {
    }
}
