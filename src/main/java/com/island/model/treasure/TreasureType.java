package com.forbiddenisland.models.treasure;

/**
 * Represents the different types of treasures in the game.
 * Each treasure type has a display name used for user interface purposes.
 */
public enum TreasureType {
    EARTH_STONE("Earth"),
    FIRE_CRYSTAL("Fire"),
    OCEAN_CHALICE("Ocean"),
    WIND_STATUE("Wind"),
    NONE("None");  // Used for tiles without treasures

    private final String displayName;

    /**
     * Creates a new treasure type with the specified display name.
     * @param displayName The name to display in the user interface
     */
    TreasureType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the display name of the treasure type.
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Converts a string to a TreasureType enum value.
     * The comparison is case-insensitive.
     * @param text The string to convert
     * @return The matching TreasureType, or null if no match is found
     */
    public static TreasureType fromString(String text) {
        for (TreasureType b : TreasureType.values()) {
            if (b.displayName.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}