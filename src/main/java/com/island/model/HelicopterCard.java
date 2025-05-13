package com.island.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a Helicopter card in the game.
 * Helicopter cards allow players to move one or more players to any non-sunk tile, typically used for escape or victory.
 */
public class HelicopterCard extends Card {
    private List<Player> selectedPlayers; // Players selected to move
    private Position targetPosition;      // Target position for helicopter move

    /**
     * Creates a new Helicopter card.
     * @param belongingPlayer The player who owns this card
     */
    public HelicopterCard(String belongingPlayer) {
        super(CardType.HELICOPTER, "Helicopter", belongingPlayer, null, null);
        this.selectedPlayers = new ArrayList<>();
        this.targetPosition = null;
    }

    /**
     * Uses the Helicopter card to move selected players to the target position.
     * Checks for ownership, valid selection, and destination.
     * @param player The player using the card
     */
    @Override
    public void useCard(Player player) {
        if (!player.getCards().contains(this)) {
            throw new IllegalStateException("玩家没有这张直升机卡");
        }

        // Check if player is at Fool's Landing
        if (!isAtFoolsLanding(player)) {
            throw new IllegalStateException("直升机卡只能在愚者降临点使用");
        }

        // Check if at least one player is selected
        if (selectedPlayers.isEmpty()) {
            throw new IllegalStateException("请至少选择一个玩家");
        }

        // Check if target position is valid
        if (targetPosition == null) {
            throw new IllegalStateException("请选择一个有效的目标位置");
        }

        // Check if target position is not sunk
        if (!isValidDestination(targetPosition)) {
            throw new IllegalStateException("目标位置必须是未沉没的板块");
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

    /**
     * Adds a player to the list of selected players for helicopter move.
     * @param player The player to add
     */
    public void addSelectedPlayer(Player player) {
        if (!selectedPlayers.contains(player)) {
            selectedPlayers.add(player);
        }
    }

    /**
     * Removes a player from the list of selected players.
     * @param player The player to remove
     */
    public void removeSelectedPlayer(Player player) {
        selectedPlayers.remove(player);
    }

    /**
     * Sets the target position for the helicopter move.
     * @param position The target position
     */
    public void setTargetPosition(Position position) {
        this.targetPosition = position;
    }

    /**
     * Gets the list of currently selected players.
     * @return List of selected players
     */
    public List<Player> getSelectedPlayers() {
        return new ArrayList<>(selectedPlayers);
    }

    /**
     * Gets the target position for the helicopter move.
     * @return The target position
     */
    public Position getTargetPosition() {
        return targetPosition;
    }

    /**
     * Resets the state of the card (clears selected players and target position).
     */
    public void resetState() {
        selectedPlayers.clear();
        targetPosition = null;
    }

    /**
     * Checks if the player is at Fool's Landing.
     * @param player The player to check
     * @return true if the player is at Fool's Landing
     */
    private boolean isAtFoolsLanding(Player player) {
        Position currentPos = player.getPosition();
        Island island = GameStateManager.getInstance().getIsland();
        Tile currentTile = island.getTile(currentPos);
        return currentTile != null && currentTile.getName().equals("Fool's Landing");
    }

    /**
     * Checks if the target position is a valid (not sunk) tile.
     * @param position The position to check
     * @return true if the tile is not sunk
     */
    private boolean isValidDestination(Position position) {
        Island island = GameStateManager.getInstance().getIsland();
        Tile targetTile = island.getTile(position);
        return targetTile != null && !targetTile.isSunk();
    }

    /**
     * Checks if all selected players can move to the target position.
     * @return true if all selected players can move
     */
    public boolean canMoveAllSelectedPlayers() {
        if (selectedPlayers.isEmpty() || targetPosition == null) {
            return false;
        }

        // Check if target position is valid
        if (!isValidDestination(targetPosition)) {
            return false;
        }

        // Check if all selected players are at Fool's Landing
        for (Player player : selectedPlayers) {
            if (!isAtFoolsLanding(player)) {
                return false;
            }
        }

        return true;
    }
}