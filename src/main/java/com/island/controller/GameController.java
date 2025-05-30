package com.island.controller;

import com.island.controller.factory.StandardCardFactory;
import com.island.models.Room;
import com.island.models.adventurers.*;
import com.island.models.island.*;
import com.island.models.game.GameState;
import com.island.models.card.*;
import com.island.network.Message;
import com.island.network.MessageHandler;
import com.island.network.RoomController;
import com.island.util.observer.GameSubjectImpl;
import com.island.util.ui.Dialog;
import com.island.views.game.GameView;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;

/**
 * The main controller class for the Forbidden Island game.
 * This class coordinates all other controllers and manages the overall game state.
 * It acts as a central hub for game logic and communication between different components.
 * Handles game initialization, turn management, player actions, and win/lose conditions.
 */
public class GameController {
    /** The main view of the game */
    private GameView gameView;
    /** Controller for managing room/network related operations */
    private RoomController roomController;
    /** The game room model containing player information */
    private Room room;
    /** The island model representing the game board */
    private Island island;
    /** Controller for managing island-related operations */
    private IslandController islandController;

    /** Controller for managing player-related operations */
    private PlayerController playerController;
    /** Controller for managing card operations */
    private CardController cardController;
    /** Controller for managing action bar UI and game actions */
    private ActionBarController actionBarController;
    /** The current player whose turn it is */
    private Player currentPlayer;

    /** List of players to be moved by helicopter card */
    private List<Player> helicopterPlayers;
    /** Currently active special card being played */
    private Card activeSpecialCard;

    /** Number of actions remaining for current player */
    private int remainingActions = 3;
    /** Flag indicating if game has started */
    private boolean gameStart = false;
    /** Flag indicating if game is over */
    private boolean gameOver = false;

    /** Implementation of the Observer pattern for game state changes */
    private GameSubjectImpl gameSubject;

    /**
     * Constructs a new GameController with the given RoomController.
     * Initializes all sub-controllers and establishes necessary connections between components.
     * @param roomController The controller managing room/network operations
     */
    public GameController(RoomController roomController) {
        this.roomController = roomController;
        roomController.setGameController(this);
        room = roomController.getRoom();

        gameSubject = new GameSubjectImpl();
        
        islandController = new IslandController();
        islandController.setGameController(this);
        island = islandController.getIsland();
        
        // Set island reference in RoomController
        roomController.setIsland(island);

        playerController = new PlayerController();
        playerController.setGameController(this);
        cardController = new CardController(new StandardCardFactory());
        cardController.setGameController(this);
        actionBarController = new ActionBarController();
        actionBarController.setGameController(this);
    }

    /**
     * Gets the game subject for observer pattern implementation.
     * @return The game subject instance
     */
    public GameSubjectImpl getGameSubject() {
        return gameSubject;
    }

    /**
     * Handles a player joining the game.
     * @param message The join request message
     * @throws Exception If join request fails
     */
    public void handlePlayerJoin(Message message) throws Exception {
        roomController.handleJoinRequest(message);
    }

    /**
     * Gets the room controller instance.
     * @return The room controller
     */
    public RoomController getRoomController() {
        return roomController;
    }

    /**
     * Cleans up resources and shuts down the game.
     */
    public void shutdown() {
        roomController.shutdown();
    }

    /**
     * Starts a new game with the given random seed.
     * Initializes the island, players, cards and game state.
     * @param seed Random seed for game initialization
     */
    public void startGame(long seed) {
        gameStart = true;
        currentPlayer = room.getPlayers().getFirst();

        // Initialize island
        islandController.initIsland(seed);

        // Initialize players
        playerController.initPlayers(seed);

        // Initialize cards
        cardController.initCards(seed);

        // Deal initial cards to players
        playerController.dealCards(cardController.getTreasureDeck());

        gameSubject.setGameState(GameState.RUNNING);
        gameView.initGame();
        gameView.setPrimaryStage();
    }

    /**
     * Starts a new turn for the given player.
     * Resets player state and action points.
     * @param player The player whose turn is starting
     */
    public void startTurn(Player player) {
        currentPlayer = player;
        currentPlayer.resetState();
        actionBarController.setCurrentPlayer(player);
        remainingActions = 3;
        playerController.resetPlayerState();
        gameSubject.setGameState(GameState.TURN_START);
        gameSubject.notifyActionBarChanged();
    }

    // Getters and setters with appropriate documentation
    public IslandController getIslandController() { return islandController; }
    public void setGameView(GameView gameView) { this.gameView = gameView; }
    public PlayerController getPlayerController() { return playerController; }
    public CardController getCardController() { return cardController; }
    public void setCurrentPlayer(Player currentPlayer) { this.currentPlayer = currentPlayer; }
    public Player getCurrentPlayer() { return currentPlayer; }
    public ActionBarController getActionBarController() { return actionBarController; }
    public int getRemainingActions() { return remainingActions; }
    public void decreaseRemainingActions() { remainingActions--; }
    public void setRemainingActions(int remainingActions) { this.remainingActions = remainingActions; }
    public Room getRoom() { return room; }
    public Island getIsland() { return islandController.getIsland(); }
    public Player getCurrentProgramPlayer() { return room.getCurrentProgramPlayer(); }

    /**
     * Handles water level rising event.
     * Increases water level and checks for game over condition.
     */
    public void handleWaterRise() {
        islandController.increaseWaterLevel();
        if (islandController.getWaterLevel() == 10 ) {
            gameOver = true;
            roomController.sendGameOverMessage("Water level has reached the maximum!");
        }
        cardController.handleWaterRise();
        updateWaterLevel();
    }

    /**
     * Gets the message handler for network communication.
     * @return The message handler instance
     */
    public MessageHandler getMessageHandler() {
        return roomController.getMessageHandler();
    }

    /**
     * Handles drawing treasure cards for a player.
     * @param count Number of cards to draw
     * @param player Player drawing the cards
     */
    public void handleDrawTreasureCard(int count, Player player) {
        cardController.drawTreasureCard(count, player);
    }

    /**
     * Gets the currently selected tile.
     * @return The chosen tile
     */
    public Tile getChosenTile() {
        return islandController.getChosenTile();
    }

    /**
     * Updates the game board view.
     */
    public void updateBoard() {
        gameSubject.notifyBoardChanged();
    }

    /**
     * Updates the action bar view.
     */
    public void updateActionBar() {
        gameSubject.notifyActionBarChanged();
    }

    /**
     * Transfers a card from one player to another.
     * @param fromPlayer Player giving the card
     * @param toPlayer Player receiving the card
     * @param card Name of the card being transferred
     */
    public void giveCard(Player fromPlayer, Player toPlayer, String card) {
        Card removedCard = fromPlayer.removeCard(card);
        toPlayer.addCard(removedCard);
        decreaseRemainingActions();
    }

    /**
     * Updates the players info view.
     */
    public void updatePlayersInfo() {
        gameSubject.notifyPlayerInfoChanged();
    }

    /**
     * Updates the card view.
     */
    public void updateCardView() {
        gameSubject.notifyCardChanged();
    }

    /**
     * Handles playing a special action card.
     * Shows appropriate dialog based on card type.
     */
    public void handlePlaySpecialAction() {
        Card chosenCard = playerController.getChosenCard();
        if (chosenCard == null) {
            showErrorToast("Not choose a card!");
            return;
        }
        if (chosenCard.getType() != CardType.TREASURE) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Use Special Card");
            alert.setHeaderText("Do you want to use " + chosenCard.getName() + "?");

            switch (chosenCard.getType()) {
                case HELICOPTER:
                    alert.setContentText("The helicopter can move one or more players from any tile to another non-sunk tile.");
                    break;
                case SANDBAGS:
                    alert.setContentText("Sandbags can shore up any flooded tile.");
                    break;
                default:
                    return;
            }

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    switch (chosenCard.getType()) {
                        case HELICOPTER:
                            handleHelicopterCard(chosenCard);
                            break;
                        case SANDBAGS:
                            handleSandbagsCard(chosenCard);
                            break;
                    }
                }
            });
        }
    }

    /**
     * Handles using a sandbags card.
     * @param chosenCard The sandbags card being used
     */
    private void handleSandbagsCard(Card chosenCard) {
        showToast("Please click on the tile you want to shore up");
        activeSpecialCard = chosenCard;
    }

    /**
     * Handles using a helicopter card.
     * Shows dialog for selecting players and handles win condition check.
     * @param chosenCard The helicopter card being used
     */
    private void handleHelicopterCard(Card chosenCard) {
        // If all treasures are collected and all players are on Fool's Landing, win the game
        if (islandController.getTreasures().length == 0) {
            for (Player player : room.getPlayers()) {
                if (!island.getTile(player.getPosition()).getName().equals("Blue")) {
                    break;
                }
            }
            gameOver = true;
            roomController.sendGameOverMessage("All treasures have been captured!");
        }

        VBox dialogContent = new VBox(10);
        dialogContent.setPadding(new Insets(20));

        // Select players to move
        Label selectPlayersLabel = new Label("Select players to move:");
        FlowPane playerCheckBoxes = new FlowPane(10, 10);
        Map<String, CheckBox> playerSelections = new HashMap<>();

        for (Player player : room.getPlayers()) {
            javafx.scene.control.CheckBox checkBox = new javafx.scene.control.CheckBox(player.getName());
            playerCheckBoxes.getChildren().add(checkBox);
            playerSelections.put(player.getName(), checkBox);
        }

        // Instructions for selecting target position
        Label instructionLabel = new Label("After selecting, click OK, then click on the target tile");

        dialogContent.getChildren().addAll(selectPlayersLabel, playerCheckBoxes, instructionLabel);

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Use Helicopter");
        dialog.setHeaderText("Select Players to Move");
        dialog.getDialogPane().setContent(dialogContent);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                List<Player> selectedPlayers = new ArrayList<>();
                for (Map.Entry<String, javafx.scene.control.CheckBox> entry : playerSelections.entrySet()) {
                    if (entry.getValue().isSelected()) {
                        for (Player player : room.getPlayers()) {
                            if (player.getName().equals(entry.getKey())) {
                                selectedPlayers.add(player);
                                break;
                            }
                        }
                    }
                }

                if (!selectedPlayers.isEmpty()) {
                    // Wait for user to click target tile
                    showToast("Please click on the tile you want to move to");
                    helicopterPlayers = selectedPlayers;
                    activeSpecialCard = chosenCard;
                }
            }
        });
    }

    /**
     * Handles using a special card at a specific position.
     * @param position The target position for the special card effect
     */
    public void handleUseSpecialCard(Position position) {
        // If using a special card
        if (activeSpecialCard != null) {
            switch (activeSpecialCard.getType()) {
                case HELICOPTER:
                    if (helicopterPlayers != null && !helicopterPlayers.isEmpty()) {
                        executeHelicopterMove(position);
                    }
                    break;
                case SANDBAGS:
                    executeSandbagsUse(position);
                    break;
            }
            return;
        }
    }

    /**
     * Executes the sandbags card effect on a tile.
     * @param position Position of the tile to shore up
     */
    private void executeSandbagsUse(Position position) {
        Tile tile = island.getTile(position);
        if (tile == null || !tile.isFlooded()) {
            showWarningToast("Can only shore up flooded tiles");
            return;
        }
        Player user = room.getCurrentProgramPlayer();

        // Add sandbags card to discard pile
        int cardIndex = user.getCards().indexOf(activeSpecialCard);

        // Use RoomController
        roomController.sendSandbagsMessage(user, position, cardIndex);

        // Reset state
        activeSpecialCard = null;
    }

    /**
     * Executes the helicopter move action.
     * @param position Destination position for the helicopter move
     */
    private void executeHelicopterMove(Position position) {
        Player user = room.getCurrentProgramPlayer();
        if (helicopterPlayers == null || helicopterPlayers.isEmpty()) return;

        // Check if target position is valid
        Tile targetTile = island.getTile(position);
        if (targetTile == null || targetTile.isSunk()) {
            showWarningToast("Cannot move to a sunk tile");
            return;
        }

        // Selected players must be on the same tile
        for (Player player : helicopterPlayers) {
            if (!player.getPosition().equals(user.getPosition())) {
                showErrorToast("All players must be on the same tile");
                return;
            }
        }

        int cardIndex = user.getCards().indexOf(activeSpecialCard);

        // Use RoomController
        roomController.sendHelicopterMoveMessage(helicopterPlayers, user, position, cardIndex);

        // Reset state
        helicopterPlayers = null;
        activeSpecialCard = null;
    }

    /**
     * Adds a card to the treasure discard pile.
     * @param card Card to add to discard pile
     */
    public void addTreasureDiscardPile(Card card) {
        cardController.addTreasureDiscardPile(card);
    }

    /**
     * Draws flood cards and checks for game-ending conditions.
     * @param count Number of flood cards to draw
     * @return List of positions where tiles were flooded
     */
    public List<Position> drawFloodCards(int count) {
        List<Position> floodedPositions = cardController.drawFloodCards(count);
        // Check if treasure tiles are sunk before treasure collection
        if (!islandController.checkTreasureTiles()) {
            gameOver = true;
            roomController.sendGameOverMessage("A treasure tile has sunk before the treasure is captured!");
        } else if (!islandController.checkFoolsLanding()) {
            gameOver = true;
            roomController.sendGameOverMessage("Fool's Landing has sunk!");
        }
        return floodedPositions;
    }

    /**
     * Handles discarding a card action.
     */
    public void handleDiscardAction() {
        Card chosenCard = playerController.getChosenCard();
        if (chosenCard != null) {
            int cardIndex = room.getCurrentProgramPlayer().getCards().indexOf(chosenCard);
            roomController.sendDiscardMessage(room.getCurrentProgramPlayer(), cardIndex);
            playerController.setChosenCard(null);
        }
    }

    /**
     * Advances to the next player's turn.
     * Handles flood card drawing at the end of a round.
     */
    public void nextTurn() {
        List<Player> players = room.getPlayers();
        int nextIndex = (players.indexOf(currentPlayer) + 1) % players.size();
        Player nextPlayer = players.get(nextIndex);
        roomController.sendStartTurnMessage(nextPlayer);

        // If completing a round (back to first player), draw flood cards based on water level
        if (nextIndex == 0) {
            int waterLevel = islandController.getWaterLevel();
            int cardsToDraw;

            // Determine number of flood cards based on water level
            if (waterLevel <= 2) cardsToDraw = 2;
            else if (waterLevel <= 5) cardsToDraw = 3;
            else if (waterLevel <= 7) cardsToDraw = 4;
            else cardsToDraw = 5;

            roomController.sendDrawFloodMessage(cardsToDraw, "system");
        }
    }

    /**
     * Handles the case when a player is on a sunk tile.
     * Shows valid move options and checks for game over condition.
     * @param currentProgramPlayer The player on a sunk tile
     */
    public void handlePlayerSunk(Player currentProgramPlayer) {
        List<Position> validPositions = currentProgramPlayer.getMovePositions(island.getTiles());
        if (validPositions.isEmpty()) {
            gameOver = true;
            roomController.sendGameOverMessage("One player has no valid moves to a non-sunk tile!");
        }
        List<Tile> validTiles = getValidTilesOnSunk(currentProgramPlayer);
        
        // Update board and show valid moves
        updateBoard();
        gameView.getIslandView().addBoarders(validTiles);
        showToast("Your tile has sunk. Please select a valid tile to move to");
    }

    /**
     * Gets valid tiles that a player can move to when their current tile is sunk.
     * @param player The player needing to move
     * @return List of valid tiles the player can move to
     */
    public List<Tile> getValidTilesOnSunk(Player player) {
        List<Position> validPositions = player.getMovePositions(island.getTiles());
        List<Tile> validTiles = new ArrayList<>();

        double minDistance = Double.MAX_VALUE;
        for (Position position : validPositions) {
            if (player instanceof Diver) {
                double distance = Math.sqrt(Math.pow(position.getX() - player.getPosition().getX(), 2) +
                        Math.pow(position.getY() - player.getPosition().getY(), 2));
                if (distance <= minDistance) {
                    if (distance < minDistance) validTiles = new ArrayList<>();
                    minDistance = distance;
                    validTiles.add(island.getTile(position));
                }
            } else {
                validTiles.add(island.getTile(position));
            }
        }
        return validTiles;
    }

    // Toast notification methods
    /**
     * Shows a toast notification to the user.
     * @param message Message to display
     */
    public void showToast(String message) {
        if (gameView != null) {
            Stage primaryStage = gameView.getPrimaryStage();
            if (primaryStage != null) {
                Dialog.showToast(primaryStage, message);
            }
        }
    }

    /**
     * Shows a success toast notification.
     * @param message Message to display
     */
    public void showSuccessToast(String message) {
        if (gameView != null) {
            Stage primaryStage = gameView.getPrimaryStage();
            if (primaryStage != null) {
                Dialog.showSuccessToast(primaryStage, message);
            }
        }
    }

    /**
     * Shows a warning toast notification.
     * @param message Message to display
     */
    public void showWarningToast(String message) {
        if (gameView != null) {
            Stage primaryStage = gameView.getPrimaryStage();
            if (primaryStage != null) {
                Dialog.showWarningToast(primaryStage, message);
            }
        }
    }

    /**
     * Shows an error toast notification.
     * @param message Message to display
     */
    public void showErrorToast(String message) {
        if (gameView != null) {
            Stage primaryStage = gameView.getPrimaryStage();
            if (primaryStage != null) {
                Dialog.showErrorToast(primaryStage, message);
            }
        }
    }

    // Additional getters and setters
    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
    public void setWaterLevel(int waterLevel) { islandController.setWaterLevel(waterLevel); }

    /**
     * Updates the water level display.
     */
    public void updateWaterLevel() {
        gameSubject.notifyWaterLevelChanged(islandController.getWaterLevel());
    }

    /**
     * Returns to the main menu.
     */
    public void returnToMainMenu() {
        gameView.returnToMainMenu();
    }

    /**
     * Resets all tile borders on the game board.
     */
    public void resetTileBorders() {
        gameView.getIslandView().clearAllBoarders();
    }

    /**
     * Checks if the game has started.
     * @return true if the game has started
     */
    public boolean isGameStart() {
        return gameStart;
    }
}
