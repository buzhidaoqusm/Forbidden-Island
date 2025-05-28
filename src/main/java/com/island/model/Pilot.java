package com.island.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Pilot extends Player {
    private boolean hasFlownThisTurn;

    public Pilot(String name) {
        super(name, PlayerRole.PILOT);
        this.hasFlownThisTurn = false;
    }

    @Override
    public List<Position> getMovePositions(Map<Position, Tile> tiles) {
        List<Position> positions = new ArrayList<>();
        if (!hasFlownThisTurn) {
            // 飞行员可以飞到任何未沉没的板块
            for (Map.Entry<Position, Tile> entry : tiles.entrySet()) {
                if (!entry.getValue().isSunk()) {
                    positions.add(entry.getKey());
                }
            }
        } else {
            // 如果已经使用过飞行能力，则只能常规移动
            Position currentPos = getPosition();
            int[] dx = {-1, 0, 1, 0};
            int[] dy = {0, 1, 0, -1};
            
            for (int i = 0; i < 4; i++) {
                Position newPos = new Position(currentPos.getX() + dx[i], currentPos.getY() + dy[i]);
                if (tiles.containsKey(newPos) && !tiles.get(newPos).isSunk()) {
                    positions.add(newPos);
                }
            }
        }
        return positions;
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

    @Override
    public void setRole(PlayerRole playerRole) {

    }

    public void resetFlightStatus() {
        this.hasFlownThisTurn = false;
    }

    public boolean hasFlownThisTurn() {
        return hasFlownThisTurn;
    }

    public void setHasFlownThisTurn(boolean hasFlownThisTurn) {
        this.hasFlownThisTurn = hasFlownThisTurn;
    }
}