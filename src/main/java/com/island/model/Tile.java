package com.island.model;

public class Tile {
    private String name;
    private Position position;
    private TileState state;
    private TreasureType treasureType;

    public Tile(String name, Position position, TreasureType treasureType) {
        this.name = name;
        this.position = position;
        this.treasureType = treasureType;
        this.state = TileState.NORMAL;
    }

    public void flood() {
        if (state == TileState.NORMAL) {
            state = TileState.FLOODED;
        } else if (state == TileState.FLOODED) {
            state = TileState.SUNK;
        }
    }

    public void shoreUp() {
        if (state == TileState.FLOODED) {
            state = TileState.NORMAL;
        }
    }

    public boolean isSunk() {
        return state == TileState.SUNK;
    }

    public boolean isFlooded() {
        return state == TileState.FLOODED;
    }

    public boolean isNormal() {
        return state == TileState.NORMAL;
    }

    public String getName() {
        return name;
    }

    public Position getPosition() {
        return position;
    }

    public TileState getState() {
        return state;
    }

    public TreasureType getTreasureType() {
        return treasureType;
    }
}