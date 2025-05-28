package com.island.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Explorer extends Player {
    public Explorer(String name) {
        super(name, PlayerRole.EXPLORER);
    }

    @Override
    public List<Position> getMovePositions(Map<Position, Tile> tiles) {
        List<Position> positions = new ArrayList<>();
        Position currentPos = getPosition();

        // 探险家可以斜向移动，检查八个方向
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < 8; i++) {
            Position newPos = new Position(currentPos.getX() + dx[i], currentPos.getY() + dy[i]);
            if (tiles.containsKey(newPos) && !tiles.get(newPos).isSunk()) {
                positions.add(newPos);
            }
        }
        return positions;
    }

    @Override
    public List<Position> getShorePositions(Map<Position, Tile> tiles) {
        List<Position> positions = new ArrayList<>();
        Position currentPos = getPosition();

        // 探险家可以斜向加固
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < 8; i++) {
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
}
