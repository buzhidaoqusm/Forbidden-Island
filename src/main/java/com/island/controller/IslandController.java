package com.island.controller;

import com.island.models.Room;
import com.island.models.adventurers.*;
import com.island.models.island.*;
import com.island.models.treasure.TreasureType;
import com.island.models.card.*;

import java.util.*;

import static com.island.util.Constant.tilesNames;

/**
 * Controller class responsible for managing the island game board.
 * Handles island initialization, tile management, water level, and treasure-related operations.
 * This class manages the core game board mechanics including tile flooding, treasure capture,
 * and player movement validation.
 */
public class IslandController {
    /** The island model representing the game board */
    private Island island;
    /** Reference to the main game controller */
    private GameController gameController;
    /** Reference to the game room */
    private Room room;
    /** Current water level of the island */
    private int waterLevel;
    /** Currently selected tile on the board */
    private Tile chosenTile;
    /** Array of treasure names that haven't been captured yet */
    private String[] treasures = new String[] { "Earth", "Wind", "Fire", "Ocean" };

    /**
     * Constructs a new IslandController.
     * Initializes the island and sets default values.
     */
    public IslandController() {
        this.island = new Island();
        chosenTile = null;
        waterLevel = 1;
    }

    /**
     * Initializes the island board with tiles in random positions.
     * Places treasure tiles and regular tiles according to the game rules.
     * Uses the provided seed for reproducible randomization.
     * @param seed Random seed for tile placement
     */
    public void initIsland(long seed) {
        List<String> tilesList = new ArrayList<>(Arrays.asList(tilesNames));

        Collections.shuffle(tilesList, new Random(seed));
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
            i++;
            if (j < 2 && i == 4 + j) {
                i = 5 - i;
                j++;
            } else if (j == 2 && i == 6) {
                i = 0;
                j++;
            } else if(j > 2 && i == 9 - j){
                i = 7 - i;
                j++;
            }
        }

        // Notify observers after initialization
        if (gameController != null) {
            gameController.updateBoard();
            gameController.updateWaterLevel();
        }
    }

    /**
     * Adds a new tile to the island at the specified position.
     * @param name Name of the tile
     * @param position Position on the board
     * @param type Type of treasure associated with the tile (if any)
     */
    private void addTile(String name, Position position, TreasureType type) {
        island.getTiles().put(position, new Tile(name, position, type));
    }

    // Getters and setters
    public Island getIsland() { return island; }

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
        this.room = gameController.getRoomController().getRoom();
    }

    public Room getRoom() { return room; }
    public int getWaterLevel() { return waterLevel; }

    /**
     * Increases the water level by one step.
     * Updates the game state and notifies observers.
     */
    public void increaseWaterLevel() {
        waterLevel++;
        if (gameController != null) {
            gameController.updateWaterLevel();
        }
    }

    /**
     * Handles tile click events.
     * Manages tile selection, navigator movement, and special card actions.
     * @param tile The clicked tile
     */
    public void handleTileClick(Tile tile) {
        if (chosenTile != null && chosenTile.equals(tile)) {
            chosenTile = null;
            gameController.resetTileBorders();
            return;
        }
        // Set the selected tile
        chosenTile = tile;

        if (gameController.getCurrentPlayer() instanceof Navigator navigator && navigator.getNavigatorTarget() != null) {
            Player navigatorTarget = navigator.getNavigatorTarget();
            Position fromPosition = navigatorTarget.getPosition();
            Position toPosition = tile.getPosition();

            // Check if move is valid (adjacent and not sunk)
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

            // Send move message through RoomController
            gameController.getRoomController().sendMoveByNavigatorMessage(gameController.getCurrentPlayer(), navigatorTarget, tile);
            return;
        }

        // Handle special card usage
        gameController.handleUseSpecialCard(tile.getPosition());
    }

    public Tile getChosenTile() { return chosenTile; }
    public GameController getGameController() { return gameController; }
    public String[] getTreasures() { return treasures; }

    /**
     * Removes a treasure from the available treasures list once it's captured.
     * @param treasureName Name of the treasure to remove
     */
    public void removeTreasure(String treasureName) {
        for (int i = 0; i < treasures.length; i++) {
            if (treasures[i].equals(treasureName)) {
                treasures[i] = null;
                break;
            }
        }
    }

    /**
     * Checks if all treasure tiles are still accessible.
     * A treasure becomes inaccessible if both tiles containing it are sunk.
     * @return false if any treasure is no longer obtainable (both tiles sunk)
     */
    public boolean checkTreasureTiles() {
        for (String treasureName : treasures) {
            int count = 2;
            for (Tile tile : island.getTiles().values()) {
                if (tile.getTreasureType() != null && tile.getTreasureType().getDisplayName().equals(treasureName) && tile.isSunk()) {
                    count--;
                }
            }
            if (count == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if Fool's Landing (helicopter pad) is still accessible.
     * This is critical as players need it to escape the island.
     * @return false if Fool's Landing is sunk
     */
    public boolean checkFoolsLanding() {
        for (Tile tile : island.getTiles().values()) {
            if (tile.getName().equals("Blue") && tile.isSunk()) {
                return false;
            }
        }
        return true;
    }

    public void setWaterLevel(int waterLevel) {
        this.waterLevel = waterLevel;
    }

    /**
     * Shores up (stabilizes) a flooded tile.
     * Handles special cases for Engineer ability.
     * @param player Player performing the shore up action
     * @param position Position of the tile to shore up
     */
    public void shoreUpTile(Player player, Position position) {
        Tile tile = island.getTile(position);
        tile.shoreUp();
        chosenTile = null;
        gameController.resetTileBorders();
        if (player instanceof Engineer e) {
            if (e.isFirstShoreUp() && gameController.getPlayerController().canShoreUpTile(e)) {
                e.setFirstShoreUp(false);
                gameController.showToast("You can shore up one more tile.");
            } else {
                gameController.decreaseRemainingActions();
                e.setFirstShoreUp(true);
            }
        } else {
            gameController.decreaseRemainingActions();
        }

        // Notify observers of state change
        if (gameController != null) {
            gameController.updateBoard();
        }
    }

    /**
     * Captures a treasure from a treasure tile.
     * Discards the required treasure cards and marks the treasure as captured.
     * @param player Player capturing the treasure
     * @param treasureType Type of treasure being captured
     */
    public void captureTreasure(Player player, TreasureType treasureType) {
        List<Card> cards = new ArrayList<>(player.getCards());
        for (Card card : cards) {
            if (card.getType() == CardType.TREASURE && card.getTreasureType() == treasureType) {
                gameController.getCurrentPlayer().removeCard(card.getName());
                card.setBelongingPlayer("");
                gameController.addTreasureDiscardPile(card);
            }
        }
        player.addCapturedTreasure(treasureType);
        removeTreasure(treasureType.getDisplayName());
        gameController.decreaseRemainingActions();
    }
}
