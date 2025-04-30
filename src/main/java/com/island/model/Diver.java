package com.island.model;

import java.util.*;

public class Diver extends Player {
    public Diver(String name) {
        super(name, PlayerRole.DIVER);
    }

    @Override
    public List<Position> getMovePositions(Map<Position, Tile> tiles) {
        Set<Position> positions = new HashSet<>();
        Position currentPos = getPosition();
        
        // 获取所有可达的位置（包括穿过被淹没的板块）
        findReachablePositions(currentPos, tiles, positions, new HashSet<>());
        
        return new ArrayList<>(positions);
    }

    private void findReachablePositions(Position pos, Map<Position, Tile> tiles, 
                                      Set<Position> reachable, Set<Position> visited) {
        visited.add(pos);
        
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, 1, 0, -1};
        
        for (int i = 0; i < 4; i++) {
            Position newPos = new Position(pos.getX() + dx[i], pos.getY() + dy[i]);
            if (!visited.contains(newPos) && tiles.containsKey(newPos)) {
                Tile tile = tiles.get(newPos);
                if (!tile.isSunk()) {
                    reachable.add(newPos);
                }
                if (tile.isFlooded()) {
                    findReachablePositions(newPos, tiles, reachable, visited);
                }
            }
        }
    }

    @Override
    public List<Position> getShorePositions(Map<Position, Tile> tiles) {
        List<Position> positions = new ArrayList<>();
        Position currentPos = getPosition();
        
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, 1, 0, -1};
        
        for (int i = 0; i < 4; i++) {
            Position newPos = new Position(currentPos.getX() + dx[i], currentPos.getY() + dy[i]);
            if (tiles.containsKey(newPos) && tiles.get(newPos).isFlooded()) {
                positions.add(newPos);
            }
        }
        return positions;
    }
}