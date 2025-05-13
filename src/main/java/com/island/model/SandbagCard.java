package com.island.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a Sandbag card in the game.
 * Sandbag cards allow players to shore up (repair) a flooded tile, even if they are not on it.
 * Engineers can use this card on any flooded tile, while other roles must be adjacent.
 */
public class SandbagCard extends Card {
    /**
     * Creates a new Sandbag card.
     * @param belongingPlayer The player who owns this card
     */
    public SandbagCard(String belongingPlayer) {
        super(CardType.SANDBAGS, "Sandbag", belongingPlayer, null, null);
    }

    /**
     * Uses the Sandbag card to shore up a flooded tile.
     * Checks for ownership, tile state, and player action points.
     * @param player The player using the card
     */
    @Override
    public void useCard(Player player) {
        // 检查玩家是否拥有这张卡
        if (!player.getCards().contains(this)) {
            throw new IllegalStateException("玩家没有这张沙袋卡");
        }

        // 获取岛屿和目标板块
        Island island = GameStateManager.getInstance().getIsland();
        Tile tile = island.getTile(player.getPosition());

        // 验证目标板块
        if (tile == null) {
            throw new IllegalArgumentException("目标板块不存在");
        }

        // 检查板块是否已沉没
        if (tile.isSunk()) {
            throw new IllegalStateException("目标板块已沉没，无法加固");
        }

        // 检查板块是否被淹没
        if (!tile.isFlooded()) {
            throw new IllegalStateException("目标板块未被淹没，无需加固");
        }

        // 检查玩家是否有足够的行动点数
        if (!player.canPerformAction()) {
            throw new IllegalStateException("玩家没有足够的行动点数来加固板块");
        }

        // 检查玩家是否在目标板块相邻位置（除非是工程师）
        if (!isValidShoreUpPosition(player, player.getPosition())) {
            throw new IllegalStateException("玩家必须在目标板块相邻位置才能加固");
        }

        // 执行加固
        tile.shoreUp();
        
        // 使用一个行动点数
        player.useAction();
        
        // 移除卡牌
        player.removeCard(this.getName());

        // 通知游戏状态管理器
        GameStateManager.getInstance().handleShoreUp(player, player.getPosition());
    }

    /**
     * Checks if the player can shore up the target tile.
     * Engineers can shore up any flooded tile, others must be adjacent.
     * @param player The player attempting to shore up
     * @param targetPosition The position of the target tile
     * @return true if the player can shore up the tile, false otherwise
     */
    private boolean isValidShoreUpPosition(Player player, Position targetPosition) {
        // 工程师可以加固任何被淹没的板块
        if (player instanceof Engineer) {
            return true;
        }

        // 其他玩家必须在目标板块相邻位置
        Position playerPos = player.getPosition();
        return isAdjacent(playerPos, targetPosition);
    }

    /**
     * Checks if two positions are adjacent on the board.
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
     * Gets all valid positions where the player can use the sandbag card.
     * Engineers can shore up any flooded tile, others only adjacent ones.
     * @param player The player using the card
     * @return List of valid positions
     */
    public List<Position> getValidShoreUpPositions(Player player) {
        List<Position> validPositions = new ArrayList<>();
        Island island = GameStateManager.getInstance().getIsland();
        Position playerPos = player.getPosition();

        // 如果玩家是工程师，可以加固任何被淹没的板块
        if (player instanceof Engineer) {
            for (Map.Entry<Position, Tile> entry : island.getGameMap().entrySet()) {
                if (entry.getValue().isFlooded() && !entry.getValue().isSunk()) {
                    validPositions.add(entry.getKey());
                }
            }
        } else {
            // 其他玩家只能加固相邻的被淹没板块
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
}