package com.island.model;

import java.util.*;

// 岛屿类
public class Island {
    private Map<Position, Tile> gameMap;
    private int waterLevel;
    private Position foolsLandingPosition;
    private static final int MAX_WATER_LEVEL = 10;

    public Island() {
        this.gameMap = new HashMap<>();
        this.waterLevel = 1;
        initializeIsland();
    }

    /**
     * Initialize the island with tiles in their starting positions
     */
    private void initializeIsland() {
          //initialize the island with tiles in their starting positions
    }

    /**
     * Create a new tile at the specified position
     * @param position The position of the tile
     * @param name The name of the tile
     * @param treasureType The type of treasure on the tile
     */
    private void createTile(Position position, String name, TreasureType treasureType) {
        Tile tile = new Tile(name, position, treasureType);
        gameMap.put(position, tile);
        if (name.equals("Fool's Landing")) {
            foolsLandingPosition = position;
        }
    }

    /**
     * Get a tile at the specified position
     * @param position The position to check
     * @return The tile at the position, or null if no tile exists
     */
    public Tile getTile(Position position) {
        return gameMap.get(position);
    }

    /**
     * Get all tiles in the game map
     * @return An unmodifiable map of all tiles
     */
    public Map<Position, Tile> getGameMap() {
        return Collections.unmodifiableMap(gameMap);
    }

    /**
     * Find a tile by its treasure type
     * @param treasureType The type of treasure to find
     * @return The position of the first tile with the specified treasure type, or null if not found
     */
    public Position findTile(TreasureType treasureType) {
        for (Map.Entry<Position, Tile> entry : gameMap.entrySet()) {
            if (entry.getValue().getTreasureType() == treasureType) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Get all flooded tiles
     * @return List of positions of flooded tiles
     */
    public List<Position> getFloodedTiles() {
        List<Position> floodedTiles = new ArrayList<>();
        for (Map.Entry<Position, Tile> entry : gameMap.entrySet()) {
            if (entry.getValue().isFlooded()) {
                floodedTiles.add(entry.getKey());
            }
        }
        return floodedTiles;
    }

    /**
     * Get all sunk tiles
     * @return List of positions of sunk tiles
     */
    public List<Position> getSunkTiles() {
        List<Position> sunkTiles = new ArrayList<>();
        for (Map.Entry<Position, Tile> entry : gameMap.entrySet()) {
            if (entry.getValue().isSunk()) {
                sunkTiles.add(entry.getKey());
            }
        }
        return sunkTiles;
    }

    /**
     * Get all adjacent tiles to a position
     * @param position The position to check
     * @return List of positions of adjacent tiles
     */
    public List<Position> getAdjacentPositions(Position position) {
        List<Position> adjacentPositions = new ArrayList<>();
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, 1, 0, -1};

        for (int i = 0; i < 4; i++) {
            Position newPos = new Position(position.getX() + dx[i], position.getY() + dy[i]);
            if (gameMap.containsKey(newPos)) {
                adjacentPositions.add(newPos);
            }
        }
        return adjacentPositions;
    }

    /**
     * Check if two positions are adjacent
     * @param pos1 First position
     * @param pos2 Second position
     * @return true if the positions are adjacent, false otherwise
     */
    public boolean isAdjacent(Position pos1, Position pos2) {
        int dx = Math.abs(pos1.getX() - pos2.getX());
        int dy = Math.abs(pos1.getY() - pos2.getY());
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    /**
     * Get all valid shore up positions for a player
     * @param player The player attempting to shore up
     * @return List of valid positions for shoring up
     */
    public List<Position> getValidShoreUpPositions(Player player) {
        List<Position> validPositions = new ArrayList<>();
        Position playerPos = player.getPosition();

        // If player is an Engineer, they can shore up any flooded tile
        if (player instanceof Engineer) {
            for (Map.Entry<Position, Tile> entry : gameMap.entrySet()) {
                if (entry.getValue().isFlooded() && !entry.getValue().isSunk()) {
                    validPositions.add(entry.getKey());
                }
            }
        } else {
            // Other players can only shore up adjacent flooded tiles
            for (Position pos : getAdjacentPositions(playerPos)) {
                Tile tile = gameMap.get(pos);
                if (tile != null && tile.isFlooded() && !tile.isSunk()) {
                    validPositions.add(pos);
                }
            }
        }

        return validPositions;
    }

    /**
     * Get the current water level
     * @return The current water level
     */
    public int getWaterLevel() {
        return waterLevel;
    }

    /**
     * Set the water level
     * @param level The new water level
     * @throws IllegalArgumentException if the level is invalid
     */
    public void setWaterLevel(int level) {
        if (level < 1 || level > MAX_WATER_LEVEL) {
            throw new IllegalArgumentException("Water level must be between 1 and " + MAX_WATER_LEVEL);
        }
        this.waterLevel = level;
    }

    /**
     * Get the position of Fool's Landing
     * @return The position of Fool's Landing
     */
    public Position getFoolsLandingPosition() {
        return foolsLandingPosition;
    }

    /**
     * Check if a position is Fool's Landing
     * @param position The position to check
     * @return true if the position is Fool's Landing, false otherwise
     */
    public boolean isFoolsLanding(Position position) {
        return position.equals(foolsLandingPosition);
    }

    /**
     * Flood a tile at the specified position
     * @param position The position of the tile to flood
     * @return true if the tile was successfully flooded, false otherwise
     */
    public boolean floodTile(Position position) {
        Tile tile = gameMap.get(position);
        if (tile == null) {
            return false;
        }

        // If the tile is already sunk, it cannot be flooded
        if (tile.isSunk()) {
            return false;
        }

        // If the tile is already flooded, it becomes sunk
        if (tile.isFlooded()) {
            tile.flood();
            return true;
        }

        // Otherwise, flood the tile
        tile.flood();
        return true;
    }

    /**
     * Check if a tile can be flooded
     * @param position The position to check
     * @return true if the tile can be flooded, false otherwise
     */
    public boolean canFloodTile(Position position) {
        Tile tile = gameMap.get(position);
        return tile != null && !tile.isSunk();
    }

    /**
     * Get all tiles that can be flooded
     * @return List of positions of tiles that can be flooded
     */
    public List<Position> getFloodableTiles() {
        List<Position> floodableTiles = new ArrayList<>();
        for (Map.Entry<Position, Tile> entry : gameMap.entrySet()) {
            if (canFloodTile(entry.getKey())) {
                floodableTiles.add(entry.getKey());
            }
        }
        return floodableTiles;
    }

    /**
     * Check if a player is on a sunk tile
     * @param player The player to check
     * @return true if the player is on a sunk tile, false otherwise
     */
    public boolean isPlayerOnSunkTile(Player player) {
        Position playerPos = player.getPosition();
        Tile tile = gameMap.get(playerPos);
        return tile != null && tile.isSunk();
    }

    /**
     * Get all valid positions a player can swim to from a sunk tile
     * @param player The player attempting to swim
     * @return List of valid positions the player can swim to
     */
    public List<Position> getValidSwimPositions(Player player) {
        List<Position> validPositions = new ArrayList<>();
        Position playerPos = player.getPosition();

        // Check all adjacent positions
        for (Position pos : getAdjacentPositions(playerPos)) {
            Tile tile = gameMap.get(pos);
            if (tile != null && !tile.isSunk()) {
                validPositions.add(pos);
            }
        }

        return validPositions;
    }

    /**
     * Check if a player can swim to safety
     * @param player The player to check
     * @return true if the player can swim to safety, false otherwise
     */
    public boolean canPlayerSwimToSafety(Player player) {
        return !getValidSwimPositions(player).isEmpty();
    }

    /**
     * Get all players on a specific tile
     * @param position The position to check
     * @param players List of all players in the game
     * @return List of players on the specified tile
     */
    public List<Player> getPlayersOnTile(Position position, List<Player> players) {
        List<Player> playersOnTile = new ArrayList<>();
        for (Player player : players) {
            if (player.getPosition().equals(position)) {
                playersOnTile.add(player);
            }
        }
        return playersOnTile;
    }

    /**
     * Check if a tile is safe for players
     * @param position The position to check
     * @return true if the tile is safe (not sunk), false otherwise
     */
    public boolean isTileSafe(Position position) {
        Tile tile = gameMap.get(position);
        return tile != null && !tile.isSunk();
    }

    /**
     * Get the number of sunk tiles
     * @return The number of sunk tiles
     */
    public int getSunkTileCount() {
        return (int) gameMap.values().stream()
                .filter(Tile::isSunk)
                .count();
    }

    /**
     * Get the number of flooded tiles
     * @return The number of flooded tiles
     */
    public int getFloodedTileCount() {
        return (int) gameMap.values().stream()
                .filter(Tile::isFlooded)
                .count();
    }

    /**
     * Check if the island is in a critical state
     * @return true if the island is in a critical state, false otherwise
     */
    public boolean isInCriticalState() {
        int sunkCount = getSunkTileCount();
        int totalTiles = gameMap.size();
        return (double) sunkCount / totalTiles >= 0.5; // More than 50% of tiles are sunk
    }

    /**
     * Get all tiles of a specific treasure type
     * @param treasureType The type of treasure to find
     * @return List of positions of tiles with the specified treasure type
     */
    public List<Position> getTilesByTreasureType(TreasureType treasureType) {
        List<Position> tiles = new ArrayList<>();
        for (Map.Entry<Position, Tile> entry : gameMap.entrySet()) {
            if (entry.getValue().getTreasureType() == treasureType) {
                tiles.add(entry.getKey());
            }
        }
        return tiles;
    }

    /**
     * Check if a treasure type is still available
     * @param treasureType The type of treasure to check
     * @return true if at least one tile with the treasure type is not sunk, false otherwise
     */
    public boolean isTreasureTypeAvailable(TreasureType treasureType) {
        return getTilesByTreasureType(treasureType).stream()
                .anyMatch(pos -> !gameMap.get(pos).isSunk());
    }
}