package com.island.model;

/**
 * Represents the different types of treasures in the game.
 */
public enum TreasureType {
    EARTH_STONE("Earth Stone"),
    FIRE_CRYSTAL("Fire Crystal"),
    OCEAN_CHALICE("Ocean Chalice"),
    WIND_STATUE("Wind Statue"),
    NONE("None");

    private final String displayName;

    TreasureType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TreasureType fromString(String text) {
        for (TreasureType type : TreasureType.values()) {
            if (type.name().equalsIgnoreCase(text)) {
                return type;
            }
        }
        return NONE;
    }
}