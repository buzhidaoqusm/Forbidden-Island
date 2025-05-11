package com.island.controller;

import com.island.model.*;

import java.util.*;

import static com.island.util.Constant.tilesNames;

/**
 * The IslandController class is responsible for managing the island's state and interactions.
 * It handles the initialization of the island, tile management, water level control, and treasure collection.
 * This class also interacts with the GameController to update the game state and notify observers.
 * */
public class IslandController {
    // Island object
    private Island island;

    // Controller of game
    private GameController gameController;

    // Room object
    private Room room;

    // Water level of the island
    private int waterLevel;

    // Chosen tile for actions
    private Tile chosenTile;

    // Array of treasure names
    private String[] treasures = new String[] { "Earth", "Wind", "Fire", "Ocean" };

    public IslandController(Island island) {
        this.island = island;
        chosenTile = null;
        waterLevel = 1;
    }

    /**
     * Initializes the island with a shuffled set of tiles based on the provided seed.
     * The tiles are placed in a specific pattern and assigned treasure types accordingly.
     * @param seed The seed for randomization.
     */
    public void initIsland(long seed) {
        List<String> tilesList = new ArrayList<>(Arrays.asList(tilesNames));
        Collections.shuffle(tilesList, new Random(seed)); // Shuffle the tiles

        // Initialize tiles on the island
        int i = 2, j = 0;
        for (String tileName : tilesList) {
            if (tileName.contains("Earth")) {
                addTile(tileName, new Position(i, j), TreasureType.EARTH_STONE);
            } else if (tileName.contains("Fire")) {
                addTile(tileName, new Position(i, j), TreasureType.FIRE_CRYSTAL);
            } else if (tileName.contains("Ocean")) {
                addTile(tileName, new Position(i, j), TreasureType.OCEAN_CHALICE);
            } else if (tileName.contains("Wind")) {
                addTile(tileName, new Position(i, j), TreasureType.WIND_STATUE);
            } else {
                addTile(tileName, new Position(i, j), null);
            }

            // Make the tiles follow a specific pattern
            i++;
            if (j < 2 && i == 4 + j) { // The first 3 rows
                i = 5 - i;
                j++;
            } else if (j == 2 && i == 6) { // The middle row, ie. row 4
                i = 0;
                j++;
            } else if(j > 2 && i == 9 - j){ // The last 2 rows
                i = 7 - i;
                j++;
            }
        }

        // Update the game state after initialization
        if (gameController != null) {
            gameController.updateBoard();
            gameController.updateWaterLevel();
        }
    }

    private void addTile(String name, Position position, TreasureType type) {
        island.getTiles().put(position, new Tile(name, position, type));
    }

    public Island getIsland() {
        return island;
    }

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
        this.room = gameController.getRoomController().getRoom();
    }

    public Room getRoom() {
        return room;
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    /**
     * Increases the water level of the island and notifies the game controller to update the view.
     */
    public void increaseWaterLevel() {
        waterLevel++;
        if (gameController != null) {
            gameController.updateWaterLevel();
        }
    }

    /**
     * Handle tile click events.
     * This method checks if the clicked tile is valid for the current player and performs the necessary actions.
     * @param tile The clicked tile.
     * */
    public void handleTileClick(Tile tile) {
        if (chosenTile != null && chosenTile.equals(tile)) {
            chosenTile = null;
            gameController.resetTileBorders();
            return;
        }
        // set the chosen tile
        chosenTile = tile;

        // Check if the navigator uses the special ability
        if (gameController.getCurrentPlayer() instanceof Navigator navigator && navigator.getNavigatorTarget() != null) {
            Player navigatorTarget = navigator.getNavigatorTarget();
            Position fromPosition = navigatorTarget.getPosition();
            Position toPosition = tile.getPosition();

            // Check if the move is valid (adjacent and not sunk)
            int dx = Math.abs(toPosition.getX() - fromPosition.getX());
            int dy = Math.abs(toPosition.getY() - fromPosition.getY());
            boolean isAdjacent = false;
            if (navigator.getNavigatorMoves() == 2) {
                isAdjacent = (dx + dy == 2);
            } else if (navigator.getNavigatorMoves() == 1) {
                isAdjacent = (dx + dy == 1);
            }

            if (!isAdjacent || island.getTile(toPosition).isSunk()) {
                gameController.showWarningToast("Invalid move! You can only move to one or two adjacent tiles that is not sunk.");
                return;
            }

            gameController.getRoomController().sendMoveByNavigatorMessage(gameController.getCurrentPlayer(), navigatorTarget, tile);
            return;
        }

        // Handle special card usage
        gameController.handleUseSpecialCard(tile.getPosition());
    }

    public Tile getChosenTile() {
        return chosenTile;
    }

    public GameController getGameController() {
        return gameController;
    }

    public String[] getTreasures() {
        return treasures;
    }

    public void removeTreasure(String treasureName) {

    }

    public boolean checkTreasureTiles() {

        return true;
    }

    public boolean checkFoolsLanding() {

        return true;
    }

    public void setWaterLevel(int waterLevel) {
        this.waterLevel = waterLevel;
    }

    public void shoreUpTile(Player player, Position position) {

    }

    public void captureTreasure(Player player, TreasureType treasureType) {

    }

    public void floodTile(Position position) {

    }

    public void shoreUp(Position position) {

    }
}
