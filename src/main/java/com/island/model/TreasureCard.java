package com.island.model;

public class TreasureCard extends Card {
    public TreasureCard(TreasureType treasureType, String belongingPlayer) {
        super(CardType.TREASURE, treasureType.getDisplayName(), belongingPlayer, null, treasureType);
    }

    @Override
    public void useCard(Player player) {
        if (player.getCards().contains(this)) {
            player.addCaptureTreasure(this.getTreasureType());
            player.removeCard(this.getName());
        }
    }
}
