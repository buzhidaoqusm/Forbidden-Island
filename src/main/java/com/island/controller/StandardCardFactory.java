package com.island.controller;

import java.util.*;

public class StandardCardFactory implements CardFactory {
    @Override
    public void initCards(CardController cardController, long seed) {
        cardController.setSeed(seed);
        // Initialize the treasure deck
        for (TreasureType type : TreasureType.values()) {
            if (type != TreasureType.NONE) {
                for (int i = 0; i < 4; i++) {
                    cardController.getTreasureDeck().add(Card.createTreasureCard(type, ""));
                }
            }
        }
        // Add special cards
        cardController.getTreasureDeck().add(Card.createSpecialCard(CardType.HELICOPTER));
        cardController.getTreasureDeck().add(Card.createSpecialCard(CardType.HELICOPTER));
        cardController.getTreasureDeck().add(Card.createSpecialCard(CardType.HELICOPTER));
        cardController.getTreasureDeck().add(Card.createSpecialCard(CardType.SANDBAGS));
        cardController.getTreasureDeck().add(Card.createSpecialCard(CardType.SANDBAGS));
        cardController.getTreasureDeck().add(Card.createSpecialCard(CardType.WATER_RISE));
        cardController.getTreasureDeck().add(Card.createSpecialCard(CardType.WATER_RISE));
        cardController.getTreasureDeck().add(Card.createSpecialCard(CardType.WATER_RISE));

        // Initialize the flood deck
        for (Map.Entry<Position, Tile> entry : cardController.getIsland().getGameMap().entrySet()) {
            cardController.getFloodDeck().add(Card.createFloodCard(entry.getValue().getName(), entry.getKey(), ""));
        }

        cardController.shuffleDecks();
        cardController.drawFloodCards(6);

        if (cardController.getGameController() != null) {
            cardController.getGameController().updateCardView();
        }
    }
}
