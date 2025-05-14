package com.island.model;

import com.island.controller.GameController;
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
    private boolean isUsed;              // Track if card has been used
    private GameController gameController; // Reference to game controller

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
        this.isUsed = false;
    }

    /**
     * Sets the game controller for this card
     * @param gameController The game controller instance
     */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    /**
     * Uses the card based on its type.
     * @param player The player using the card
     * @throws IllegalStateException if the card cannot be used
     */
    public void useCard(Player player) {
        if (isUsed) {
            throw new IllegalStateException("Card has already been used");
        }

        if (!player.getCards().contains(this)) {
            throw new IllegalStateException("Player does not have this card");
        }

        if (!canUseCard(player)) {
            throw new IllegalStateException("Card cannot be used in current state");
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

        isUsed = true;
    }

    /**
     * Checks if the card can be used by the player
     * @param player The player attempting to use the card
     * @return true if the card can be used, false otherwise
     */
    private boolean canUseCard(Player player) {
        if (gameController == null) {
            return false;
        }

        switch (type) {
            case HELICOPTER:
                return isAtFoolsLanding(player) && !selectedPlayers.isEmpty() && targetPosition != null;
            case SANDBAGS:
                return gameController.getRemainingActions() > 0 && isValidShoreUpPosition(player, targetPosition);
            case FLOOD:
                return floodPosition != null && gameController.getIsland().canFloodTile(floodPosition);
            case WATER_RISE:
                return gameController.getIsland().getWaterLevel() < 10;
            case TREASURE:
                return canCaptureTreasure(player);
            default:
                return false;
        }
    }

    private void useHelicopterCard(Player player) {
        Island island = gameController.getIsland();
        
        // Validate all selected players
        for (Player selectedPlayer : selectedPlayers) {
            if (!island.isTileSafe(targetPosition)) {
                throw new IllegalStateException("Target position is not safe");
            }
        }

        // Move all selected players to the target position
        for (Player selectedPlayer : selectedPlayers) {
            selectedPlayer.setPosition(targetPosition);
        }

        // Remove the used card
        player.removeCard(this.getName());
        gameController.decreaseRemainingActions();

        // Check win condition
        if (island.checkHelicopterWinCondition()) {
            gameController.setGameOver(true);
            gameController.getRoomController().sendGameOverMessage("All players have successfully escaped!");
        }

        resetState();
    }

    private void useSandbagCard(Player player) {
        Island island = gameController.getIsland();
        Tile tile = island.getTile(targetPosition);

        if (tile == null || tile.isSunk() || !tile.isFlooded()) {
            throw new IllegalStateException("Invalid target tile for sandbag card");
        }

        // Shore up the tile
        tile.shoreUp();
        
        // Remove card and decrease actions
        player.removeCard(this.getName());
        gameController.decreaseRemainingActions();

        // Check game state
        if (!island.checkTreasureTiles()) {
            gameController.setGameOver(true);
            gameController.getRoomController().sendGameOverMessage("A treasure tile has sunk before its treasure was collected!");
        }
    }

    private void useFloodCard(Player player) {
        Island island = gameController.getIsland();
        if (floodPosition != null) {
            boolean flooded = island.floodTile(floodPosition);
            if (flooded) {
                // Check if any players are on the flooded tile
                List<Player> playersOnTile = island.getPlayersOnTile(floodPosition, gameController.getRoom().getPlayers());
                for (Player affectedPlayer : playersOnTile) {
                    gameController.handlePlayerSunk(affectedPlayer);
                }
            }
        }
    }

    private void useWaterRiseCard(Player player) {
        Island island = gameController.getIsland();
        int currentLevel = island.getWaterLevel();
        if (currentLevel < 10) {
            island.setWaterLevel(currentLevel + 1);
            gameController.handleWaterRise();
        }
    }

    private void useTreasureCard(Player player) {
        Island island = gameController.getIsland();
        Position playerPos = player.getPosition();
        Tile currentTile = island.getTile(playerPos);

        if (currentTile != null && currentTile.getTreasureType() == treasureType) {
            // Check if player has enough matching treasure cards
            int matchingCards = (int) player.getCards().stream()
                .filter(card -> card.getTreasureType() == treasureType)
                .count();

            if (matchingCards >= 4) {
                // Capture treasure
                player.captureTreasure(treasureType);
                // Remove used cards
                player.getCards().removeIf(card -> card.getTreasureType() == treasureType);
            }
        }
    }

    private boolean canCaptureTreasure(Player player) {
        Island island = gameController.getIsland();
        Position playerPos = player.getPosition();
        Tile currentTile = island.getTile(playerPos);

        if (currentTile == null || currentTile.getTreasureType() != treasureType) {
            return false;
        }

        int matchingCards = (int) player.getCards().stream()
            .filter(card -> card.getTreasureType() == treasureType)
            .count();

        return matchingCards >= 4;
    }

    private boolean isAtFoolsLanding(Player player) {
        if (gameController == null) return false;
        Island island = gameController.getIsland();
        Position currentPos = player.getPosition();
        return island.isFoolsLanding(currentPos);
    }

    private boolean isValidDestination(Position position) {
        if (gameController == null) return false;
        Island island = gameController.getIsland();
        return island.isTileSafe(position);
    }

    private boolean isValidShoreUpPosition(Player player, Position targetPosition) {
        if (gameController == null) return false;
        Island island = gameController.getIsland();
        
        // Engineers can shore up any flooded tile
        if (player instanceof Engineer) {
            return true;
        }

        // Other players must be adjacent to the target tile
        Position playerPos = player.getPosition();
        return island.isAdjacent(playerPos, targetPosition);
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

    /**
     * Resets the card's state
     */
    public void resetState() {
        selectedPlayers.clear();
        targetPosition = null;
        isUsed = false;
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
                ", isUsed=" + isUsed +
                '}';
    }
}