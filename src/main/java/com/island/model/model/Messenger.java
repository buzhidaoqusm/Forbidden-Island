package com.island.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Messenger extends Player {
    public Messenger(String name) {
        super(name, PlayerRole.MESSENGER);
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

    public void giveCard(Player targetPlayer, Card card) {
        if (this.getCards().contains(card)) {
            this.removeCard(card);
            targetPlayer.addCard(card);
        }
    }
}