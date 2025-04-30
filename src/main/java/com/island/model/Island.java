package com.island.model;
import java.util.HashMap;
import java.util.Map;

// 岛屿类
public class Island {
    private Map<Position, Tile> tiles;

    public Island() {
        this.tiles = new HashMap<>();
    }

    public Map<Position, Tile> getTiles() {
        return tiles;
    }

    // 根据位置获取瓷砖
    public Tile getTile(Position position) {
        return tiles.get(position);
    }

    // 根据名称查找瓷砖
    public Tile findTile(String name) {
        for (Tile tile : tiles.values()) {
            if (tile.getName().equals(name)) {
                return tile;
            }
        }
        return null;
    }

    // 根据位置查找瓷砖
    public Tile findTile(Position position) {
        return tiles.get(position);
    }

    // 使指定位置的瓷砖被淹没
    public void floodTile(Position position) {
        Tile tile = tiles.get(position);
        if (tile != null) {
            tile.flood();
        }
    }
}