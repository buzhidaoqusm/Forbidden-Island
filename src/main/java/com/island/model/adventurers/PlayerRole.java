package com.forbiddenisland.models.adventurers;

/**
 * Represents the different roles that players can take in the game.
 * Each role has a display name and an associated color for identification.
 */
public enum PlayerRole {
    EXPLORER("Explorer"),
    PILOT("Pilot"),
    NAVIGATOR("Navigator"),
    DIVER("Diver"),
    ENGINEER("Engineer"),
    MESSENGER("Messenger");

    private final String displayName;

    /**
     * Creates a new player role with the specified display name.
     * @param displayName The name to display in the user interface
     */
    PlayerRole(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the display name of the role.
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the color associated with a player role.
     * @param role The role to get the color for
     * @return The color name as a string, or null if the role is not recognized
     */
    public static String getColor(PlayerRole role) {
        switch (role) {
            case EXPLORER:
                return "Green";
            case PILOT:
                return "Blue";
            case NAVIGATOR:
                return "Yellow";
            case DIVER:
                return "Black";
            case ENGINEER:
                return "Red";
            case MESSENGER:
                return "White";
        }
        return null;
    }
} 