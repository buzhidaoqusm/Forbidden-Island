package com.island.model;

import java.util.List;
import java.util.ArrayList;

public class HelicopterCard extends Card {
    private List<Player> selectedPlayers; // Store selected players
    private Position targetPosition;      // Target position

    public HelicopterCard(String belongingPlayer) {
        super(CardType.HELICOPTER, "Helicopter", belongingPlayer, null, null);
        this.selectedPlayers = new ArrayList<>();
        this.targetPosition = null;
    }

    @Override
    public void useCard(Player player) {
        if (!player.getCards().contains(this)) {
            return;
        }

        // Check if player is at Fool's Landing
        if (!isAtFoolsLanding(player)) {
            throw new IllegalStateException("Helicopter card can only be used at Fool's Landing");
        }

        // Check if any players are selected
        if (selectedPlayers.isEmpty()) {
            throw new IllegalStateException("Please select at least one player");
        }

        // Check if target position is valid
        if (targetPosition == null) {
            throw new IllegalStateException("Please select a valid target position");
        }

        // Check if target position is not sunk
        if (!isValidDestination(targetPosition)) {
            throw new IllegalStateException("Target position must be an unsunk tile");
        }

        // Execute helicopter movement
        for (Player selectedPlayer : selectedPlayers) {
            selectedPlayer.setPosition(targetPosition);
        }

        // Remove the used card
        player.removeCard(this.getName());

        // Reset state
        resetState();
    }

    // Add player to selection list
    public void addSelectedPlayer(Player player) {
        if (!selectedPlayers.contains(player)) {
            selectedPlayers.add(player);
        }
    }

    // Remove player from selection list
    public void removeSelectedPlayer(Player player) {
        selectedPlayers.remove(player);
    }

    // Set target position
    public void setTargetPosition(Position position) {
        this.targetPosition = position;
    }

    // Get current selected players list
    public List<Player> getSelectedPlayers() {
        return new ArrayList<>(selectedPlayers);
    }

    // Get target position
    public Position getTargetPosition() {
        return targetPosition;
    }

    // Reset card state
    public void resetState() {
        selectedPlayers.clear();
        targetPosition = null;
    }

    // Check if player is at Fool's Landing
    private boolean isAtFoolsLanding(Player player) {
        Position currentPos = player.getPosition();
        Island island = GameStateManager.getInstance().getIsland();
        Tile currentTile = island.getTile(currentPos);
        return currentTile != null && currentTile.getName().equals("Fool's Landing");
    }

    // Check if target position is valid (not sunk)
    private boolean isValidDestination(Position position) {
        Island island = GameStateManager.getInstance().getIsland();
        Tile targetTile = island.getTile(position);
        return targetTile != null && !targetTile.isSunk();
    }

    // Check if all selected players can be moved
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