package com.island.model;

/**
 * 水位上升卡类，用于提高水位
 */
public class WaterRiseCard extends Card {
    public WaterRiseCard(String belongingPlayer) {
        super(CardType.WATER_RISE, "Water Rise", belongingPlayer, null, null);
    }

    @Override
    public void useCard(Player player) {
        if (!player.getCards().contains(this)) {
            throw new IllegalStateException("玩家没有这张水位上升卡");
        }

        // 检查玩家是否在愚者降临点
        if (!isAtFoolsLanding(player)) {
            throw new IllegalStateException("水位上升卡只能在愚者降临点使用");
        }

        // 检查水位是否已经达到最大值
        if (isMaxWaterLevel()) {
            throw new IllegalStateException("水位已经达到最大值");
        }

        // 处理水位上升
        GameStateManager.getInstance().handleWaterRise();
        
        // 移除使用过的卡牌
        player.removeCard(this.getName());
    }

    /**
     * 检查玩家是否在愚者降临点
     */
    private boolean isAtFoolsLanding(Player player) {
        Position playerPos = player.getPosition();
        Island island = GameStateManager.getInstance().getIsland();
        Tile currentTile = island.getTile(playerPos);
        
        return currentTile != null && 
               currentTile.getName().equals("Fool's Landing") &&
               !currentTile.isSunk();
    }

    /**
     * 检查水位是否达到最大值
     */
    private boolean isMaxWaterLevel() {
        return GameStateManager.getInstance().getWaterLevel() >= 10;
    }

    /**
     * 获取当前水位
     */
    public int getCurrentWaterLevel() {
        return GameStateManager.getInstance().getWaterLevel();
    }

    /**
     * 获取最大水位
     */
    public int getMaxWaterLevel() {
        return 10;
    }
}