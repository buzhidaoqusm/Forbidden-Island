package com.island.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 标准玩家类，实现了基本的玩家功能
 */
public class StandardPlayer extends Player {
    
    public StandardPlayer(String name) {
        super(name, null);
    }

    public StandardPlayer(String name, PlayerRole role) {
        super(name, role);
    }

    @Override
    public List<Position> getMovePositions(Map<Position, Tile> tiles) {
        List<Position> validPositions = new ArrayList<>();
        Position currentPos = getPosition();
        
        if (currentPos == null) return validPositions;
        
        // 基本移动：上下左右
        Position[] directions = {
            new Position(currentPos.getX() + 1, currentPos.getY()),
            new Position(currentPos.getX() - 1, currentPos.getY()),
            new Position(currentPos.getX(), currentPos.getY() + 1),
            new Position(currentPos.getX(), currentPos.getY() - 1)
        };
        
        // 检查每个方向
        for (Position pos : directions) {
            Tile tile = tiles.get(pos);
            if (tile != null && !tile.isSunk()) {
                validPositions.add(pos);
            }
        }
        
        // 根据角色添加特殊移动
        if (getRole() == PlayerRole.EXPLORER) {
            // 探险家可以斜向移动
            Position[] diagonals = {
                new Position(currentPos.getX() + 1, currentPos.getY() + 1),
                new Position(currentPos.getX() + 1, currentPos.getY() - 1),
                new Position(currentPos.getX() - 1, currentPos.getY() + 1),
                new Position(currentPos.getX() - 1, currentPos.getY() - 1)
            };
            for (Position pos : diagonals) {
                Tile tile = tiles.get(pos);
                if (tile != null && !tile.isSunk()) {
                    validPositions.add(pos);
                }
            }
        } else if (getRole() == PlayerRole.PILOT) {
            // 飞行员可以飞到任何位置
            tiles.forEach((pos, tile) -> {
                if (!tile.isSunk() && !pos.equals(currentPos)) {
                    validPositions.add(pos);
                }
            });
        } else if (getRole() == PlayerRole.DIVER) {
            // 潜水员可以穿过已沉没的格子
            addDiverPositions(currentPos, tiles, validPositions, new ArrayList<>());
        }
        
        return validPositions;
    }

    private void addDiverPositions(Position current, Map<Position, Tile> tiles, 
                                 List<Position> validPositions, List<Position> visited) {
        visited.add(current);
        
        // 检查相邻位置
        Position[] directions = {
            new Position(current.getX() + 1, current.getY()),
            new Position(current.getX() - 1, current.getY()),
            new Position(current.getX(), current.getY() + 1),
            new Position(current.getX(), current.getY() - 1)
        };
        
        for (Position pos : directions) {
            if (!visited.contains(pos)) {
                Tile tile = tiles.get(pos);
                if (tile != null) {
                    if (!tile.isSunk()) {
                        // 找到一个可用的格子
                        validPositions.add(pos);
                    } else {
                        // 继续穿过沉没的格子
                        addDiverPositions(pos, tiles, validPositions, visited);
                    }
                }
            }
        }
    }

    @Override
    public List<Position> getShorePositions(Map<Position, Tile> tiles) {
        List<Position> validPositions = new ArrayList<>();
        Position currentPos = getPosition();
        
        if (currentPos == null) return validPositions;
        
        // 基本加固：上下左右相邻的格子
        Position[] directions = {
            currentPos,  // 当前位置
            new Position(currentPos.getX() + 1, currentPos.getY()),
            new Position(currentPos.getX() - 1, currentPos.getY()),
            new Position(currentPos.getX(), currentPos.getY() + 1),
            new Position(currentPos.getX(), currentPos.getY() - 1)
        };
        
        // 检查每个方向
        for (Position pos : directions) {
            Tile tile = tiles.get(pos);
            if (tile != null && tile.isFlooded() && !tile.isSunk()) {
                validPositions.add(pos);
            }
        }
        
        // 工程师可以一次加固两个相邻的格子
        if (getRole() == PlayerRole.ENGINEER) {
            // 工程师的额外加固位置与基本加固位置相同
            validPositions.addAll(validPositions);
        }
        
        return validPositions;
    }

    @Override
    public void setRole(PlayerRole playerRole) {

    }
} 