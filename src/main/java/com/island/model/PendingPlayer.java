package com.island.model;

import java.util.List;
import java.util.Map;

public class PendingPlayer extends Player{
    public PendingPlayer(String name) {
        super(name, PlayerRole.EXPLORER);
    }

    @Override
    public List<Position> getMovePositions(Map<Position, Tile> tiles) {
        return null;
    }

    @Override
    public List<Position> getShorePositions(Map<Position, Tile> tiles) {
        return null;
    }

    @Override
    public void setRole(PlayerRole playerRole) {

    }
}
