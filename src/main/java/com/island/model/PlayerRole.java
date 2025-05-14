package com.island.model;

public enum PlayerRole {
    EXPLORER,
    PILOT,
    NAVIGATOR,
    DIVER,
    ENGINEER,
    MESSENGER;

    public String getDisplayName() {
        switch (this) {
            case EXPLORER: return "Explorer";
            case PILOT: return "Pilot";
            case NAVIGATOR: return "Navigator";
            case DIVER: return "Diver";
            case ENGINEER: return "Engineer";
            case MESSENGER: return "Messenger";
            default: return "";
        }
    }

    public static String getColor(PlayerRole role) {
        return switch (role) {
            case EXPLORER -> "Green";
            case PILOT -> "Blue";
            case NAVIGATOR -> "Yellow";
            case DIVER -> "Black";
            case ENGINEER -> "Red";
            case MESSENGER -> "White";
        };
    }
}
