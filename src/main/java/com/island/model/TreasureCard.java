package com.island.model;

public class TreasureCard extends Card {
    private static final int TREASURE_CARDS_REQUIRED = 4; // Number of cards required to collect treasure

    public TreasureCard(TreasureType treasureType, String belongingPlayer) {
        super(CardType.TREASURE, treasureType.getDisplayName(), belongingPlayer, null, treasureType);
    }

    @Override
    public void useCard(Player player) {
        if (!player.getCards().contains(this)) {
            throw new IllegalStateException("Player does not have this treasure card");
        }

        // Check if player is on the correct treasure tile
        if (!isOnCorrectTreasureTile(player)) {
            throw new IllegalStateException("Must be on the corresponding treasure tile to collect treasure");
        }

        // Check if player has enough treasure cards
        if (!hasEnoughTreasureCards(player)) {
            throw new IllegalStateException("Need 4 cards of the same treasure type to collect treasure");
        }

        // Check if treasure has already been collected
        if (player.getCapturedTreasures().contains(this.getTreasureType())) {
            throw new IllegalStateException("This treasure has already been collected");
        }

        // Collect treasure
        player.addCaptureTreasure(this.getTreasureType());
        
        // Remove all cards used to collect treasure
        removeTreasureCards(player);
    }

    // Check if player is on the correct treasure tile
    private boolean isOnCorrectTreasureTile(Player player) {
        Position playerPos = player.getPosition();
        Island island = GameStateManager.getInstance().getIsland();
        Tile currentTile = island.getTile(playerPos);
        
        return currentTile != null && 
               currentTile.getTreasureType() == this.getTreasureType() &&
               !currentTile.isSunk();
    }

    // Check if player has enough treasure cards
    private boolean hasEnoughTreasureCards(Player player) {
        int count = 0;
        for (Card card : player.getCards()) {
            if (card instanceof TreasureCard && 
                ((TreasureCard) card).getTreasureType() == this.getTreasureType()) {
                count++;
            }
        }
        return count >= TREASURE_CARDS_REQUIRED;
    }

    // Remove cards used to collect treasure
    private void removeTreasureCards(Player player) {
        int cardsToRemove = TREASURE_CARDS_REQUIRED;
        for (int i = player.getCards().size() - 1; i >= 0 && cardsToRemove > 0; i--) {
            Card card = player.getCards().get(i);
            if (card instanceof TreasureCard && 
                ((TreasureCard) card).getTreasureType() == this.getTreasureType()) {
                player.removeCard(card.getName());
                cardsToRemove--;
            }
        }
    }
}
