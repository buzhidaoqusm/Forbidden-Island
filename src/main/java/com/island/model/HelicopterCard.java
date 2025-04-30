package com.island.model;

public class HelicopterCard extends Card {
    public HelicopterCard(String belongingPlayer) {
        super(CardType.HELICOPTER, "Helicopter", belongingPlayer, null, null);
    }

    @Override
    public void useCard(Player player) {
        if (player.getCards().contains(this)) {
            GameStateManager.getInstance().handleHelicopterLift(player);
            player.removeCard(this.getName());
        }
    }
}