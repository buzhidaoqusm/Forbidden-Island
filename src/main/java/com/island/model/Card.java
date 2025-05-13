package com.island.model;

/**
 * Abstract base class for all cards in the game.
 * This class defines the common properties and behaviors of cards,
 * including treasure cards, flood cards, and special cards.
 */
public abstract class Card {
    private final CardType type;
    private final String name;
    private String belongingPlayer;
    private final Position floodPosition;
    private final TreasureType treasureType;

    /**
     * Constructor for creating a new card.
     * @param type The type of the card (TREASURE, FLOOD, WATER_RISE, etc.)
     * @param name The name of the card
     * @param belongingPlayer The player who owns this card
     * @param floodPosition The position this card affects (for flood cards)
     * @param treasureType The type of treasure (for treasure cards)
     */
    protected Card(CardType type, String name, String belongingPlayer, Position floodPosition, TreasureType treasureType) {
        this.type = type;
        this.name = name;
        this.belongingPlayer = belongingPlayer;
        this.floodPosition = floodPosition;
        this.treasureType = treasureType;
    }

    /**
     * Creates a new treasure card.
     * @param treasureType The type of treasure
     * @param belongingPlayer The player who will own this card
     * @return A new treasure card instance
     */
    public static Card createTreasureCard(TreasureType treasureType, String belongingPlayer) {
        return new TreasureCard(treasureType, belongingPlayer);
    }

    /**
     * Creates a new flood card.
     * @param tileName The name of the tile to flood
     * @param position The position of the tile
     * @param belongingPlayer The player who will own this card
     * @return A new flood card instance
     */
    public static Card createFloodCard(String tileName, Position position, String belongingPlayer) {
        return new FloodCard(tileName, position, belongingPlayer);
    }

    /**
     * Creates a new special card without an owner.
     * @param type The type of special card
     * @return A new special card instance
     */
    public static Card createSpecialCard(CardType type) {
        return createSpecialCard(type, "");
    }

    /**
     * Creates a new special card with an owner.
     * @param type The type of special card
     * @param belongingPlayer The player who will own this card
     * @return A new special card instance
     */
    public static Card createSpecialCard(CardType type, String belongingPlayer) {
        switch (type) {
            case HELICOPTER:
                return new HelicopterCard(belongingPlayer);
            case SANDBAGS:
                return new SandbagCard(belongingPlayer);
            case WATER_RISE:
                return new WaterRiseCard(belongingPlayer);
            default:
                throw new IllegalArgumentException("Invalid special card type: " + type);
        }
    }

    /**
     * Abstract method for using the card.
     * Each card type must implement its own usage logic.
     * @param player The player using the card
     */
    public abstract void useCard(Player player);

    /**
     * Gets the type of the card.
     * @return The card type
     */
    public CardType getType() {
        return type;
    }

    /**
     * Gets the name of the card.
     * @return The card name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the player who owns this card.
     * @return The owner's name
     */
    public String getBelongingPlayer() {
        return belongingPlayer;
    }

    /**
     * Sets the player who owns this card.
     * @param belongingPlayer The new owner's name
     */
    public void setBelongingPlayer(String belongingPlayer) {
        this.belongingPlayer = belongingPlayer;
    }

    /**
     * Gets the position this card affects (for flood cards).
     * @return The flood position
     */
    public Position getFloodPosition() {
        return floodPosition;
    }

    /**
     * Gets the type of treasure (for treasure cards).
     * @return The treasure type
     */
    public TreasureType getTreasureType() {
        return treasureType;
    }

    @Override
    public String toString() {
        return "Card{" +
                "type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}