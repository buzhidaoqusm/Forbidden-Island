package com.island.model;

public class WaterRiseCard extends Card {
    public WaterRiseCard(String belongingPlayer) {
        super(CardType.WATER_RISE, "Water Rise", belongingPlayer, null, null);
    }

    @Override
    public void useCard(Player player) {
        if (player.getCards().contains(this)) {
            GameStateManager.getInstance().handleWaterRise();
            player.removeCard(this.getName());
        }
    }
}