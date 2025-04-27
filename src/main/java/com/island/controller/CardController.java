package com.island.controller;

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

    public List<Position> drawFloodCards(int count) {

    }

    public void drawTreasureCard(int count, Player player) {

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

    public void handleWaterRise() {
    }

    public void shutdown() {
    }
}
