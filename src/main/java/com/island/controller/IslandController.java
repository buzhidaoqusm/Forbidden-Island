package com.island.controller;

import com.island.model.*;

import java.util.*;

import static com.island.util.Constant.tilesNames;

/**
 * The IslandController class manages the island game board and its state in the Forbidden Island game.
 * 
 * This controller:
 * - Creates and initializes the island grid with randomized tile placement
 * - Tracks and updates the water level throughout the game
 * - Manages tile states (normal, flooded, sunk)
 * - Handles player interactions with tiles (moving, shoring up)
 * - Tracks treasure placement and collection
 * - Manages special interactions like Navigator movement
 * - Ensures game board state consistency
 */
public class IslandController {
    /**
     * The Island model containing the grid of tiles
     */
    private Island island;

    /**
     * Reference to the main game controller
     */
    private GameController gameController;

    /**
     * Reference to the current game room
     */
    private Room room;

    /**
     * Current water level of the island (1-10)
     * Higher levels cause more flood cards to be drawn each round
     */
    private int waterLevel;

    /**
     * The currently selected tile for actions
     */
    private Tile chosenTile;

    /**
     * Array of treasure names for tracking captured treasures
     */
    private String[] treasures = new String[] { "Earth", "Wind", "Fire", "Ocean" };

    private List<Position> validPositions;

    /**
     * Constructs an IslandController with the given Island model
     * Initializes the water level to 1 (lowest level)
     * 
     * @param island The Island model to control
     */
    public IslandController(Island island) {
        this.island = island;
        chosenTile = null;
        waterLevel = 1;
        this.validPositions = new ArrayList<>();
    }

    /**
     * Initializes the island with a shuffled set of tiles based on the provided seed.
     * Tiles are placed in a specific pattern and assigned treasure types accordingly.
     * This creates the game board layout with proper treasure tile distribution.
     * 
     * @param seed The seed for randomization to ensure deterministic behavior
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

    /**
     * Creates a new tile and adds it to the island's tile collection
     * 
     * @param name The name of the tile
     * @param position The position of the tile on the grid
     * @param type The treasure type associated with the tile, or null for regular tiles
     */
    private void addTile(String name, Position position, TreasureType type) {
        island.getGameMap().put(position, new Tile(name, position, type));
    }

    /**
     * Increases the water level of the island.
     * Higher water levels cause more flood cards to be drawn each round.
     * Updates the UI to reflect the new water level.
     */
    public void increaseWaterLevel() {
        int currentLevel = island.getWaterLevel();
        if (currentLevel < 10) {
            island.setWaterLevel(currentLevel + 1);
        }
        if (gameController != null) {
            gameController.updateWaterLevel();
        }
    }

    /**
     * Handles player interactions with tiles on the island.
     * Processes tile selection, special abilities (like Navigator), and special card usage.
     * Manages the highlighting of valid tiles for actions.
     * 
     * @param tile The tile that was clicked
     */
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

    /**
     * Marks a treasure as captured by removing it from the available treasures list.
     * 
     * @param treasureName The name of the treasure that was captured
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
     * Checks if all treasure tiles for uncaptured treasures are still available.
     * If both tiles for a treasure are sunk before the treasure is captured, the game is lost.
     * 
     * @return true if all treasure tiles are still valid, false if any uncaptured treasure's tiles are both sunk
     */
    public boolean checkTreasureTiles() {
        for (String treasureName : treasures) {
            int count = 2;
            for (Tile tile : island.getGameMap().values()) {
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
     * Checks if the Fool's Landing tile is still available.
     * If Fool's Landing sinks, the game is lost as players can't escape the island.
     * 
     * @return true if Fool's Landing is still available, false if it has sunk
     */
    public boolean checkFoolsLanding() {
        return true;
    }

    /**
     * Sets the water level to a specific value.
     * Used for testing or specific game events.
     * 
     * @param waterLevel The new water level value (1-10)
     */
    public void setWaterLevel(int waterLevel) {
        this.waterLevel = waterLevel;
    }

    /**
     * Executes the shore up action on a tile to restore it from flooded to normal state.
     * Handles special role abilities like the Engineer's double shore up ability.
     * Updates the game board to reflect the change.
     * 
     * @param player The player performing the shore up action
     * @param position The position of the tile to shore up
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

        if (gameController != null) {
            gameController.updateBoard();
        }
    }

    /**
     * Handles the capture of a treasure by a player.
     * Removes the 4 matching treasure cards from the player's hand,
     * adds the treasure to the player's captured treasures, and
     * marks the treasure as captured.
     * 
     * @param player The player capturing the treasure
     * @param treasureType The type of treasure being captured
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
        player.addCaptureTreasure(treasureType);
        removeTreasure(treasureType.getDisplayName());
        gameController.decreaseRemainingActions();
    }

    /**
     * Gets the currently selected tile for actions
     * 
     * @return The currently selected Tile object
     */
    public Tile getChosenTile() {
        return chosenTile;
    }

    /**
     * Gets the game controller associated with this controller
     * 
     * @return The GameController instance
     */
    public GameController getGameController() {
        return gameController;
    }

    /**
     * Gets the array of treasure names for tracking captured treasures
     * 
     * @return Array of treasure names, with null values for captured treasures
     */
    public String[] getTreasures() {
        return treasures;
    }

    /**
     * Gets the island model containing the game board
     * 
     * @return The Island model instance
     */
    public Island getIsland() {
        return island;
    }

    /**
     * Sets the game controller for this island controller
     * Establishes the bidirectional relationship between controllers
     * 
     * @param gameController The GameController to associate with this controller
     */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    /**
     * Gets the game room associated with this controller
     * 
     * @return The Room object containing players and game state
     */
    public Room getRoom() {
        return room;
    }

    /**
     * Gets the current water level of the island
     * 
     * @return The water level value (1-10)
     */
    public int getWaterLevel() {
        return island.getWaterLevel();
    }

    public void setValidPositions(List<Position> positions) {
        this.validPositions = new ArrayList<>(positions);
    }

    public void clearValidPositions() {
        this.validPositions.clear();
    }

    public List<Position> getValidPositions() {
        return new ArrayList<>(validPositions);
    }

    // New methods for special card handling

    public List<Position> getValidShoreUpPositions(Player player) {
        return island.getValidShoreUpPositions(player);
    }

    public boolean isAtFoolsLanding(Position position) {
        return island.isFoolsLanding(position);
    }

    public List<Position> getValidHelicopterDestinations() {
        List<Position> validPositions = new ArrayList<>();
        for (Map.Entry<Position, Tile> entry : island.getGameMap().entrySet()) {
            if (!entry.getValue().isSunk()) {
                validPositions.add(entry.getKey());
            }
        }
        return validPositions;
    }

    public boolean isValidShoreUpPosition(Player player, Position position) {
        Tile targetTile = island.getTile(position);
        if (targetTile == null || !targetTile.isFlooded() || targetTile.isSunk()) {
            return false;
        }

        if (player instanceof Engineer) {
            return true;
        }

        return island.isAdjacent(player.getPosition(), position);
    }

    public void shoreUpTile(Position position) {
        Tile tile = island.getTile(position);
        if (tile != null && tile.isFlooded() && !tile.isSunk()) {
            tile.shoreUp();
        }
    }

    public boolean isValidHelicopterDestination(Position position) {
        Tile targetTile = island.getTile(position);
        return targetTile != null && !targetTile.isSunk();
    }

    public boolean checkHelicopterWinCondition() {
        List<Player> players = gameController.getRoom().getPlayers();
        if (players.isEmpty()) {
            return false;
        }

        Position firstPlayerPos = players.get(0).getPosition();
        if (firstPlayerPos == null) {
            return false;
        }

        Tile currentTile = island.getTile(firstPlayerPos);
        if (currentTile == null || currentTile.isSunk()) {
            return false;
        }

        for (int i = 1; i < players.size(); i++) {
            if (!players.get(i).getPosition().equals(firstPlayerPos)) {
                return false;
            }
        }

        return true;
    }
}
