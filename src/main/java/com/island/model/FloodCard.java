package com.island.model;
import com.island.controller.GameController;
public class FloodCard extends Card {
    public FloodCard(String tileName, Position position, String belongingPlayer) {
        super(CardType.FLOOD, tileName, belongingPlayer, position, null);
    }

    @Override
    public void useCard(Player player) {
        if (player.getCards().contains(this)) {
            GameController gameController = player.getGameController();
            Island island = gameController.getIsland();
            Position floodPos = this.getFloodPosition();
            
            // 淹没对应位置的板块
            island.floodTile(floodPos);
            
            // 将卡牌加入弃牌堆
            gameController.getCardController().getFloodDiscardPile().add(this);
            player.removeCard(this);
            
            // 检查玩家是否在被淹没的板块上
            if (player.getPosition().equals(floodPos)) {
                gameController.handlePlayerSunk(player);
            }
            
            // 更新游戏界面
            gameController.getIslandController().updateBoard();
        }
    }
}
