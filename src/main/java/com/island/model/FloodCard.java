package com.island.model;
import com.island.controller.GameController;

/**
 * Represents a Flood card in the game.
 * Flood cards are used to flood specific tiles on the island.
 */
public class FloodCard extends Card {
    /**
     * Creates a new Flood card.
     * @param tileName The name of the tile to flood
     * @param position The position of the tile
     * @param belongingPlayer The player who owns this card (usually empty)
     */
    public FloodCard(String tileName, Position position, String belongingPlayer) {
        super(CardType.FLOOD, tileName, belongingPlayer, position, null);
    }

    /**
     * Uses the Flood card to flood the corresponding tile.
     * If the player is on the flooded tile, triggers the sunk logic.
     * @param player The player using the card
     */
    @Override
    public void useCard(Player player) {
        if (player.getCards().contains(this)) {
            GameController gameController = player.getGameController();
            Island island = gameController.getIsland();
            Position floodPos = this.getFloodPosition();
            
            // Flood the corresponding tile
            island.floodTile(floodPos);
            
            // Add the card to the flood discard pile
            gameController.getCardController().getFloodDiscardPile().add(this);
            player.removeCard(this.getName());
            
            // Check if the player is on the flooded tile
            if (player.getPosition().equals(floodPos)) {
                gameController.handlePlayerSunk(player);
            }
            
            // Update the game state/view
            gameController.getCardController().drawFloodCards(1);
        }
    }
}
