package com.forbiddenisland.models.card;

import com.forbiddenisland.models.island.Position;
import com.forbiddenisland.models.treasure.TreasureType;

/**
 * Represents a card in the Forbidden Island game.
 * Cards can be of different types: treasure cards, flood cards, or special cards.
 */
public class Card {
    private final CardType type;
    private final String name;
    private String belongingPlayer;
    private Position floodPosition;  // Used for flood cards
    private TreasureType treasureType;  // Used for treasure cards

    /**
     * Creates a treasure card.
     * @param treasureType The type of treasure
     * @param belongingPlayer The player who owns the card
     * @return A new treasure card
     */
    public static Card createTreasureCard(TreasureType treasureType, String belongingPlayer) {
        return new Card(CardType.TREASURE,
                treasureType.getDisplayName(),
                belongingPlayer,
                null,
                treasureType);
    }

    /**
     * Creates a flood card.
     * @param tileName The name of the tile to flood
     * @param position The position of the tile
     * @param belongingPlayer The player who owns the card
     * @return A new flood card
     */
    public static Card createFloodCard(String tileName, Position position, String belongingPlayer) {
        return new Card(CardType.FLOOD,
                tileName,
                belongingPlayer,
                position,
                null);
    }

    /**
     * Creates a special card (Water Rise, Helicopter, or Sandbags).
     * @param type The type of special card
     * @return A new special card
     * @throws IllegalArgumentException if the card type is unknown
     */
    public static Card createSpecialCard(CardType type) {
        return switch (type) {
            case WATER_RISE -> new Card(type, "WaterRise", null, null, null);
            case HELICOPTER -> new Card(type, "Helicopter", null, null, null);
            case SANDBAGS -> new Card(type, "Sandbags", null, null, null);
            default -> throw new IllegalArgumentException("Unknown card type");
        };
    }

    /**
     * Private constructor for creating a card.
     * @param type The type of card
     * @param name The name of the card
     * @param belongingPlayer The player who owns the card
     * @param floodPosition The position for flood cards
     * @param treasureType The type of treasure for treasure cards
     */
    private Card(CardType type, String name, String belongingPlayer, Position floodPosition, TreasureType treasureType) {
        this.type = type;
        this.name = name;
        this.belongingPlayer = belongingPlayer;
        this.floodPosition = floodPosition;
        this.treasureType = treasureType;
    }

    /**
     * Gets the type of the card.
     * @return The card type
     */
    public CardType getType() { return type; }

    /**
     * Gets the name of the card.
     * @return The card name
     */
    public String getName() { return name; }

    /**
     * Gets the flood position for flood cards.
     * @return The flood position, or null if not a flood card
     */
    public Position getFloodPosition() { return floodPosition; }

    /**
     * Gets the treasure type for treasure cards.
     * @return The treasure type, or null if not a treasure card
     */
    public TreasureType getTreasureType() { return treasureType; }

    /**
     * Gets the player who owns the card.
     * @return The name of the owning player
     */
    public String getBelongingPlayer() {
        return belongingPlayer;
    }

    /**
     * Sets the player who owns the card.
     * @param belongingPlayer The name of the new owning player
     */
    public void setBelongingPlayer(String belongingPlayer) {
        this.belongingPlayer = belongingPlayer;
    }
}

