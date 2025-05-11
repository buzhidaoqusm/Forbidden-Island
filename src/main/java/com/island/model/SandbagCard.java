package com.island.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SandbagCard extends Card {
    public SandbagCard(String belongingPlayer) {
        super(CardType.SANDBAGS, "Sandbag", belongingPlayer, null, null);
    }

    /**
     * Use sandbag card to shore up a specified tile
     * @param player The player using the card
     * @param targetPosition The position of the target tile
     * @throws IllegalStateException if the player doesn't have the card or the tile cannot be shored up
     * @throws IllegalArgumentException if the target position is invalid
     */
    public void useCard(Player player, Position targetPosition) {
        // Check if player has the card
        if (!player.getCards().contains(this)) {
            throw new IllegalStateException("Player does not have this sandbag card");
        }

        // Get the island and target tile
        Island island = GameStateManager.getInstance().getIsland();
        Tile tile = island.getTile(targetPosition);

        // Validate target tile
        if (tile == null) {
            throw new IllegalArgumentException("Target tile does not exist");
        }

        // Check if tile is sunk
        if (tile.isSunk()) {
            throw new IllegalStateException("Target tile is sunk, cannot shore up");
        }

        // Check if tile is flooded
        if (!tile.isFlooded()) {
            throw new IllegalStateException("Target tile is not flooded, no need to shore up");
        }

        // Check if player has enough actions
        if (!player.canPerformAction()) {
            throw new IllegalStateException("Player does not have enough actions to shore up");
        }

        // Check if player is adjacent to the target tile (unless they are an Engineer)
        if (!isValidShoreUpPosition(player, targetPosition)) {
            throw new IllegalStateException("Player must be adjacent to the target tile to shore up");
        }

        // Execute shore up
        tile.shoreUp();
        
        // Use one action
        player.useAction();
        
        // Remove the card
        player.removeCard(this.getName());

        // Notify game state manager
        GameStateManager.getInstance().handleShoreUp(player, targetPosition);
    }

    /**
     * Check if the player can shore up the target tile
     * @param player The player attempting to shore up
     * @param targetPosition The position of the target tile
     * @return true if the player can shore up the tile, false otherwise
     */
    private boolean isValidShoreUpPosition(Player player, Position targetPosition) {
        // Engineers can shore up any flooded tile
        if (player instanceof Engineer) {
            return true;
        }

        // Other players must be adjacent to the target tile
        Position playerPos = player.getPosition();
        return isAdjacent(playerPos, targetPosition);
    }

    /**
     * Check if two positions are adjacent
     * @param pos1 First position
     * @param pos2 Second position
     * @return true if the positions are adjacent, false otherwise
     */
    private boolean isAdjacent(Position pos1, Position pos2) {
        int dx = Math.abs(pos1.getX() - pos2.getX());
        int dy = Math.abs(pos1.getY() - pos2.getY());
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    /**
     * Get all valid positions where the player can use the sandbag card
     * @param player The player using the card
     * @return List of valid positions
     */
    public List<Position> getValidShoreUpPositions(Player player) {
        List<Position> validPositions = new ArrayList<>();
        Island island = GameStateManager.getInstance().getIsland();
        Position playerPos = player.getPosition();

        // If player is an Engineer, they can shore up any flooded tile
        if (player instanceof Engineer) {
            for (Map.Entry<Position, Tile> entry : island.getGameMap().entrySet()) {
                if (entry.getValue().isFlooded() && !entry.getValue().isSunk()) {
                    validPositions.add(entry.getKey());
                }
            }
        } else {
            // Other players can only shore up adjacent flooded tiles
            for (Map.Entry<Position, Tile> entry : island.getGameMap().entrySet()) {
                if (entry.getValue().isFlooded() && 
                    !entry.getValue().isSunk() && 
                    isAdjacent(playerPos, entry.getKey())) {
                    validPositions.add(entry.getKey());
                }
            }
        }

        return validPositions;
    }

    // Keep the original no-parameter useCard for compatibility
    @Override
    public void useCard(Player player) {
        throw new UnsupportedOperationException("Please use useCard(Player, Position) method and specify target tile");
    }
}