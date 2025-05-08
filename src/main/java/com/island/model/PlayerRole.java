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
        switch (role) {
            case EXPLORER: return "#00FF00";  // Green
            case PILOT: return "#0000FF";     // Blue
            case NAVIGATOR: return "#FFFF00";  // Yellow
            case DIVER: return "#000000";     // Black
            case ENGINEER: return "#FF0000";   // Red
            case MESSENGER: return "#808080";  // Gray
            default: return "#FFFFFF";        // White
        }
    }
}
