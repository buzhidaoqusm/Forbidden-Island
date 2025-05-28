package com.island.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Navigator extends Player {
    private Player navigatorTarget;
    private int navigatorMoves;

    public Navigator(String name) {
        super(name, PlayerRole.NAVIGATOR);
        this.navigatorTarget = null;
        this.navigatorMoves = 0;
    }

    @Override
    public List<Position> getMovePositions(Map<Position, Tile> tiles) {
        List<Position> positions = new ArrayList<>();
        Position currentPos = getPosition();
        
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, 1, 0, -1};
        
        for (int i = 0; i < 4; i++) {
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

    public void setNavigatorTarget(Player player, int moves) {
        this.navigatorTarget = player;
        this.navigatorMoves = moves;
    }

    public void resetTargetAndMoves() {
        this.navigatorTarget = null;
        this.navigatorMoves = 0;
    }

    public Player getNavigatorTarget() {
        return navigatorTarget;
    }

    public int getNavigatorMoves() {
        return navigatorMoves;
    }
}