package com.island.model;

public class SandbagCard extends Card {
    public SandbagCard(String belongingPlayer) {
        super(CardType.SANDBAGS, "Sandbag", belongingPlayer, null, null);
    }

    @Override
    public void useCard(Player player) {
        if (player.getCards().contains(this)) {
            GameStateManager.getInstance().handleSandbag(player);
            player.removeCard(this.getName());
        }
    }
}