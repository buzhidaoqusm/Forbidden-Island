package com.island.model;

public class WaterRiseCard extends Card {
    public WaterRiseCard(String belongingPlayer) {
        super(CardType.WATER_RISE, "Water Rise", belongingPlayer, null, null);
    }

    @Override
    public void useCard(Player player) {
        if (!player.getCards().contains(this)) {
            throw new IllegalStateException("Player does not have this Water Rise card");
        }

        // Check if player is at Fool's Landing
        if (!isAtFoolsLanding(player)) {
            throw new IllegalStateException("Water Rise card can only be used at Fool's Landing");
        }

        // Check if water level is already at maximum
        if (isMaxWaterLevel()) {
            throw new IllegalStateException("Water level is already at maximum");
        }

        // Handle water rise
        GameStateManager.getInstance().handleWaterRise();
        
        // Remove the used card
        player.removeCard(this.getName());
    }

    // Check if player is at Fool's Landing
    private boolean isAtFoolsLanding(Player player) {
        Position playerPos = player.getPosition();
        Island island = GameStateManager.getInstance().getIsland();
        Tile currentTile = island.getTile(playerPos);
        
        return currentTile != null && 
               currentTile.getName().equals("Fool's Landing") &&
               !currentTile.isSunk();
    }

    // Check if water level is at maximum
    private boolean isMaxWaterLevel() {
        return GameStateManager.getInstance().getWaterLevel() >= 10;
    }

    // Get current water level
    public int getCurrentWaterLevel() {
        return GameStateManager.getInstance().getWaterLevel();
    }

    // Get maximum water level
    public int getMaxWaterLevel() {
        return 10;
    }
}