package com.island.model;

public abstract class Card {
    private CardType type;
    private String name;
    private String belongingPlayer;
    private Position floodPosition;
    private TreasureType treasureType;

    protected Card(CardType type, String name, String belongingPlayer, Position floodPosition, TreasureType treasureType) {
        this.type = type;
        this.name = name;
        this.belongingPlayer = belongingPlayer;
        this.floodPosition = floodPosition;
        this.treasureType = treasureType;
    }

    public static Card createTreasureCard(TreasureType treasureType, String belongingPlayer) {
        return new TreasureCard(treasureType, belongingPlayer);
    }

    public static Card createFloodCard(String tileName, Position position, String belongingPlayer) {
        return new FloodCard(tileName, position, belongingPlayer);
    }

    public static Card createSpecialCard(CardType type) {
        switch (type) {
            case HELICOPTER:
                return new HelicopterCard(null);
            case SANDBAGS:
                return new SandbagCard(null);
            case WATER_RISE:
                return new WaterRiseCard(null);
            default:
                throw new IllegalArgumentException("Invalid special card type: " + type);
        }
    }

    public abstract void useCard(Player player);

    // Getters and setters
    public CardType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getBelongingPlayer() {
        return belongingPlayer;
    }

    public void setBelongingPlayer(String belongingPlayer) {
        this.belongingPlayer = belongingPlayer;
    }

    public Position getFloodPosition() {
        return floodPosition;
    }

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