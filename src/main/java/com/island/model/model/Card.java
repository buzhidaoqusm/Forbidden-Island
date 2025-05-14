package com.island.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Represents a card in the game.
 * This class handles all types of cards including treasure cards, flood cards, and special cards.
 */
public class Card {
    private final CardType type;
    private final String name;
    private String belongingPlayer;
    private final Position floodPosition;
    private final TreasureType treasureType;
    private List<Player> selectedPlayers; // For helicopter card
    private Position targetPosition;      // For helicopter card

    /**
     * Constructor for creating a new card.
     * @param type The type of the card (TREASURE, FLOOD, WATER_RISE, etc.)
     * @param name The name of the card
     * @param belongingPlayer The player who owns this card
     * @param floodPosition The position this card affects (for flood cards)
     * @param treasureType The type of treasure (for treasure cards)
     */
    public Card(CardType type, String name, String belongingPlayer, Position floodPosition, TreasureType treasureType) {
        this.type = type;
        this.name = name;
        this.belongingPlayer = belongingPlayer;
        this.floodPosition = floodPosition;
        this.treasureType = treasureType;
        this.selectedPlayers = new ArrayList<>();
        this.targetPosition = null;
    }

    /**
     * Uses the card based on its type.
     * @param player The player using the card
     */
    public void useCard(Player player) {
        if (!player.getCards().contains(this)) {
            throw new IllegalStateException("Player does not have this card");
        }

        switch (type) {
            case HELICOPTER:
                useHelicopterCard(player);
                break;
            case SANDBAGS:
                useSandbagCard(player);
                break;
            case FLOOD:
                useFloodCard(player);
                break;
            case WATER_RISE:
                useWaterRiseCard(player);
                break;
            case TREASURE:
                useTreasureCard(player);
                break;
            default:
                throw new IllegalStateException("Invalid card type");
        }
    }

    private void useHelicopterCard(Player player) {
        // Check if player is at Fool's Landing
        if (!isAtFoolsLanding(player)) {
            throw new IllegalStateException("Helicopter card can only be used at Fool's Landing");
        }

        // Check if at least one player is selected
        if (selectedPlayers.isEmpty()) {
            throw new IllegalStateException("Please select at least one player");
        }

        // Check if target position is valid
        if (targetPosition == null) {
            throw new IllegalStateException("Please select a valid target position");
        }

        // Check if target position is not sunk
        if (!isValidDestination(targetPosition)) {
            throw new IllegalStateException("Target position must be a non-sunk tile");
        }

        // Move all selected players to the target position
        for (Player selectedPlayer : selectedPlayers) {
            selectedPlayer.setPosition(targetPosition);
        }

        // Remove the used card
        player.removeCard(this.getName());

        // Reset state after use
        resetState();
    }

    private void useSandbagCard(Player player) {
        // Get island and target tile
        Island island = GameStateManager.getInstance().getIsland();
        Tile tile = island.getTile(player.getPosition());

        // Validate target tile
        if (tile == null) {
            throw new IllegalArgumentException("Target tile does not exist");
        }

        // Check if tile is sunk
        if (tile.isSunk()) {
            throw new IllegalStateException("Target tile is sunk and cannot be shored up");
        }

        // Check if tile is flooded
        if (!tile.isFlooded()) {
            throw new IllegalStateException("Target tile is not flooded");
        }

        // Check if player has enough action points
        if (!player.canPerformAction()) {
            throw new IllegalStateException("Player does not have enough action points");
        }

        // Check if player is in valid position (unless engineer)
        if (!isValidShoreUpPosition(player, player.getPosition())) {
            throw new IllegalStateException("Player must be adjacent to the target tile");
        }

        // Execute shore up
        tile.shoreUp();
        
        // Use an action point
        player.useAction();
        
        // Remove card
        player.removeCard(this.getName());

        // Notify game state manager
        GameStateManager.getInstance().handleShoreUp(player, player.getPosition());
    }

    private void useFloodCard(Player player) {
        Island island = GameStateManager.getInstance().getIsland();
        if (floodPosition != null) {
            island.floodTile(floodPosition);
        }
    }

    private void useWaterRiseCard(Player player) {
        Island island = GameStateManager.getInstance().getIsland();
        int currentLevel = island.getWaterLevel();
        if (currentLevel < 10) {
            island.setWaterLevel(currentLevel + 1);
        }
    }

    private void useTreasureCard(Player player) {
        // Treasure cards are used for capturing treasures
        // This is handled by the Player class when checking for treasure capture
    }

    private boolean isAtFoolsLanding(Player player) {
        Position currentPos = player.getPosition();
        Island island = GameStateManager.getInstance().getIsland();
        Tile currentTile = island.getTile(currentPos);
        return currentTile != null && currentTile.getName().equals("Fool's Landing");
    }

    private boolean isValidDestination(Position position) {
        Island island = GameStateManager.getInstance().getIsland();
        Tile targetTile = island.getTile(position);
        return targetTile != null && !targetTile.isSunk();
    }

    private boolean isValidShoreUpPosition(Player player, Position targetPosition) {
        // Engineers can shore up any flooded tile
        if (player instanceof Engineer) {
            return true;
        }

        // Other players must be adjacent to the target tile
        Position playerPos = player.getPosition();
        return isAdjacent(playerPos, targetPosition);
    }

    private boolean isAdjacent(Position pos1, Position pos2) {
        int dx = Math.abs(pos1.getX() - pos2.getX());
        int dy = Math.abs(pos1.getY() - pos2.getY());
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    public void addSelectedPlayer(Player player) {
        if (!selectedPlayers.contains(player)) {
            selectedPlayers.add(player);
        }
    }

    public void removeSelectedPlayer(Player player) {
        selectedPlayers.remove(player);
    }

    public void setTargetPosition(Position position) {
        this.targetPosition = position;
    }

    public List<Player> getSelectedPlayers() {
        return new ArrayList<>(selectedPlayers);
    }

    public Position getTargetPosition() {
        return targetPosition;
    }

    public void resetState() {
        selectedPlayers.clear();
        targetPosition = null;
    }

    // Getters
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