package com.island.controller;

import com.island.model.*;
import com.island.network.Message;
import com.island.network.MessageHandler;
import com.island.network.RoomController;
import com.island.util.observer.GameSubjectImpl;
import com.island.util.ui.Dialog;
import com.island.view.GameView;
import javafx.stage.Stage;

import java.util.*;

/**
 * The GameController class serves as the central coordinator for the Forbidden Island game.
 * It manages the overall game state, gameplay flow, and coordinates interactions between different components.
 * 
 * This controller:
 * - Maintains references to all other controllers (IslandController, PlayerController, CardController, ActionBarController)
 * - Handles game initialization, turn management, and game over conditions
 * - Coordinates player actions and their effects on the game state
 * - Manages the network communication through the RoomController
 * - Tracks game parameters like remaining actions, water level, and active cards
 * - Provides methods for other controllers to access game state and trigger game events
 */
public class GameController {
    // Main controller of game views
    private GameView gameView;

    // Controller of room
    private RoomController roomController;

    // Current game room
    private Room room;

    // Island object
    private Island island = new Island();

    // Controller of island
    private IslandController islandController;

    // Controller of players
    private PlayerController playerController;

    // Controller of cards
    private CardController cardController;

    // Controller of action bar
    private ActionBarController actionBarController;

    // Current player in turn
    private Player currentPlayer;

    // Players that will be moved by helicopter
    private List<Player> helicopterPlayers;

    // Special cards currently in use
    private Card activeSpecialCard;

    // Reaming actions of current player
    private int remainingActions = 3;

    // Game start flag
    private boolean gameStart = false;

    // Game over flag
    private boolean gameOver = false;

    // Players selected for helicopter lift
    private List<Player> selectedPlayers;
    // Target position for helicopter lift
    private Position targetPosition;

    // Observer pattern implementation
    private GameSubjectImpl gameSubject = new GameSubjectImpl();

    /**
     * Constructs a GameController with the given RoomController.
     * Initializes all other controllers and establishes bidirectional references.
     * 
     * @param roomController The controller managing network communication and room state
     */
    public GameController(RoomController roomController) {
        this.roomController = roomController;
        roomController.setGameController(this);
        room = roomController.getRoom();

        islandController = new IslandController(island);
        islandController.setGameController(this);

        playerController = new PlayerController();
        playerController.setGameController(this);
        cardController = new CardController();
        cardController.setGameController(this);
        actionBarController = new ActionBarController();
        actionBarController.setGameController(this);
    }

    /**
     * Initializes the game with the given random seed.
     * Sets up the island, players, and cards, and deals initial cards to players.
     * 
     * @param seed Random seed for game initialization to ensure deterministic behavior
     */
    public void startGame(long seed) {
        gameStart = true;
        currentPlayer = room.getPlayers().getFirst();

        islandController.initIsland(seed);
        playerController.initPlayers(seed);
        cardController.initCards(seed);

        // Deal cards for players
        playerController.dealCards(cardController.getTreasureDeck());

        gameSubject.setGameState(GameState.RUNNING);
        gameView.initGame();
        gameView.setPrimaryStage();
    }

    /**
     * Prepares a player to start their turn.
     * Resets player state, updates the action bar, and sets the remaining actions to 3.
     * 
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

    /**
     * Handles player join requests by delegating to the RoomController.
     * 
     * @param message The join request message
     * @throws Exception If the join process fails
     */
    public void handlePlayerJoin(Message message) throws Exception {
        roomController.handleJoinRequest(message);
    }

    /**
     * Returns the RoomController associated with this GameController.
     * 
     * @return The RoomController instance
     */
    public RoomController getRoomController() {
        return roomController;
    }

    /**
     * Cleans up resources when the game is shutting down.
     */
    public void shutdown() {

    }

    /**
     * Handles the water rise event when a water rise card is drawn.
     * Increases the water level and checks for game over conditions.
     */
    public void handleWaterRise() {
        islandController.increaseWaterLevel();
        if (islandController.getWaterLevel() == 10 ) {
            gameOver = true;
            roomController.sendGameOverMessage("Water level has reached the maximum!");
        }
        cardController.handleWaterRise();
    }

    /**
     * Handles drawing treasure cards for a player.
     * Delegates to the CardController to manage the card drawing process.
     * 
     * @param count The number of cards to draw
     * @param player The player who is drawing the cards
     */
    public List handleDrawTreasureCard(int count, Player player) {
        cardController.drawTreasureCard(count, player);
        return List.of();
    }

    /**
     * Transfers a card from one player to another.
     * Removes the card from the giving player and adds it to the receiving player.
     * 
     * @param fromPlayer The player giving the card
     * @param toPlayer The player receiving the card
     * @param card The name of the card to be given
     */
    public void giveCard(Player fromPlayer, Player toPlayer, String card) {
        Card removedCard = fromPlayer.removeCard(card);
        toPlayer.addCard(removedCard);
        decreaseRemainingActions();
    }

    /**
     * Adds a card to the treasure discard pile.
     * 
     * @param card The card to add to the discard pile
     */
    public void addTreasureDiscardPile(Card card) {
        cardController.addTreasureDiscardPile(card);
    }

    /**
     * Draws a specified number of flood cards and checks for game over conditions.
     * 
     * @param count The number of flood cards to draw
     * @return A list of positions that were flooded
     */
    public List<Position> drawFloodCards(int count) {
        List<Position> floodedPositions = cardController.drawFloodCards(count);
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
     * Handles the discard action when a player needs to discard a card.
     * Identifies the chosen card and sends a discard message to the room.
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
     * Advances the game to the next player's turn.
     * If the next player is the first player, also triggers flood card drawing.
     */
    public void nextTurn() {
        List<Player> players = room.getPlayers();
        int nextIndex = (players.indexOf(currentPlayer) + 1) % players.size();
        Player nextPlayer = players.get(nextIndex);
        roomController.sendStartTurnMessage(nextPlayer);

        // If the next player is the first player, draw flood cards
        if (nextIndex == 0) {
            int waterLevel = islandController.getWaterLevel();
            int cardsToDraw;

            // determine the number of flood cards to draw based on the water level
            if (waterLevel <= 2) cardsToDraw = 2;
            else if (waterLevel <= 5) cardsToDraw = 3;
            else if (waterLevel <= 7) cardsToDraw = 4;
            else cardsToDraw = 5;

            roomController.sendDrawFloodMessage(cardsToDraw, "system");
        }
    }

    /**
     * Handles the case when a player is on a sunk tile.
     * Checks if the player has valid moves, and if not, ends the game.
     * 
     * @param currentProgramPlayer The player who is on a sunk tile
     */
    public void handlePlayerSunk(Player currentProgramPlayer) {
        List<Position> validPositions = currentProgramPlayer.getMovePositions(island.getGameMap());
        if (validPositions.isEmpty()) {
            gameOver = true;
            roomController.sendGameOverMessage("One player has no valid moves to a non-sunk tile!");
        }
        List<Tile> validTiles = getValidTilesOnSunk(currentProgramPlayer);

    }

    /**
     * Determines valid tiles a player can move to when they are on a sunk tile.
     * For the Diver role, considers the minimum distance to valid tiles.
     * 
     * @param player The player who is on a sunk tile
     * @return A list of valid tiles the player can move to
     */
    public List<Tile> getValidTilesOnSunk(Player player) {
        List<Position> validPositions = player.getMovePositions(island.getGameMap());
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

    /**
     * Returns the IslandController associated with this GameController.
     * 
     * @return The IslandController instance
     */
    public IslandController getIslandController() {
        return islandController;
    }

    /**
     * Sets the GameView for this GameController.
     * 
     * @param gameView The GameView to be set
     */
    public void setGameView(GameView gameView) {
        this.gameView = gameView;
    }

    public PlayerController getPlayerController() {
        return playerController;
    }

    public CardController getCardController() {
        return cardController;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public ActionBarController getActionBarController() {
        return actionBarController;
    }

    public int getRemainingActions() {
        return remainingActions;
    }

    public void decreaseRemainingActions() {
        remainingActions--;
    }

    public void setRemainingActions(int remainingActions) {
        this.remainingActions = remainingActions;
    }

    public Room getRoom() {
        return room;
    }

    public Island getIsland() {
        return islandController.getIsland();
    }

    public Player getCurrentProgramPlayer() {
        return room.getCurrentProgramPlayer();
    }

    public MessageHandler getMessageHandler() {
        return roomController.getMessageHandler();
    }


    public Tile getChosenTile() {
        return islandController.getChosenTile();
    }



    public void handlePlaySpecialAction() {
        if (currentPlayer == null || !playerController.canPlaySpecialCard(currentPlayer)) {
            showWarningToast("Cannot use special card");
            return;
        }

        Card chosenCard = playerController.getChosenCard();
        if (chosenCard == null) {
            showWarningToast("Please select a special card");
            return;
        }

        if (chosenCard.getType() == CardType.HELICOPTER) {
            handleHelicopterCard(chosenCard);
        } else if (chosenCard.getType() == CardType.SANDBAGS) {
            handleSandbagsCard(chosenCard);
        }
    }

    private void handleSandbagsCard(Card chosenCard) {
        if (chosenCard.getType() != CardType.SANDBAGS) {
            showErrorToast("Invalid sandbag card");
            return;
        }

        // Get valid positions from IslandController
        List<Position> validPositions = islandController.getValidShoreUpPositions(currentPlayer);
        if (validPositions.isEmpty()) {
            showWarningToast("No flooded tiles available");
            return;
        }

        // Check action points through PlayerController
        if (!playerController.canPerformAction(currentPlayer)) {
            showWarningToast("Not enough action points");
            return;
        }

        activeSpecialCard = chosenCard;
        islandController.setValidPositions(validPositions);
        showToast("Please select a flooded tile to shore up");
    }

    private void handleHelicopterCard(Card chosenCard) {
        if (chosenCard.getType() != CardType.HELICOPTER) {
            showErrorToast("Invalid helicopter card");
            return;
        }

        // Check Fool's Landing through IslandController
        if (!islandController.isAtFoolsLanding(currentPlayer.getPosition())) {
            showWarningToast("Helicopter card can only be used at Fool's Landing");
            return;
        }

        activeSpecialCard = chosenCard;
        selectedPlayers = new ArrayList<>();
        selectedPlayers.add(currentPlayer);

        // Get valid positions from IslandController
        List<Position> validPositions = islandController.getValidHelicopterDestinations();
        islandController.setValidPositions(validPositions);
        showToast("Please select a destination");
    }

    public void handleUseSpecialCard(Position position) {
        if (activeSpecialCard == null) {
            showErrorToast("No active special card");
            return;
        }

        if (activeSpecialCard.getType() == CardType.HELICOPTER) {
            executeHelicopterMove(position);
        } else if (activeSpecialCard.getType() == CardType.SANDBAGS) {
            executeSandbagsUse(position);
        }

        activeSpecialCard = null;
        islandController.clearValidPositions();
    }

    public void executeSandbagsUse(Position position) {
        try {
            // Set the target position for the sandbag card
            activeSpecialCard.setTargetPosition(position);
            
            // Use the sandbag card
            activeSpecialCard.useCard(currentPlayer);
            
            showSuccessToast("Successfully shored up tile");
            
            // Check game state through IslandController
            if (!islandController.checkTreasureTiles()) {
                gameOver = true;
                roomController.sendGameOverMessage("A treasure tile has sunk before its treasure was collected!");
            } else if (!islandController.checkFoolsLanding()) {
                gameOver = true;
                roomController.sendGameOverMessage("Fool's Landing has sunk!");
            }
        } catch (IllegalStateException e) {
            showErrorToast(e.getMessage());
        }
    }

    public void executeHelicopterMove(Position position) {
        try {
            // Set the target position for the helicopter card
            activeSpecialCard.setTargetPosition(position);
            
            // Add selected players to the helicopter card
            for (Player player : selectedPlayers) {
                activeSpecialCard.addSelectedPlayer(player);
            }
            
            // Use the helicopter card
            activeSpecialCard.useCard(currentPlayer);
            
            showSuccessToast("Successfully used helicopter lift");

            // Check win condition through IslandController
            if (islandController.checkHelicopterWinCondition()) {
                gameOver = true;
                roomController.sendGameOverMessage("All players have successfully escaped!");
            }
        } catch (IllegalStateException e) {
            showErrorToast(e.getMessage());
        }
    }

    /**
     * Displays a toast message to the user.
     *
     * @param message The message to be displayed
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
     * Displays a success toast message to the user.
     *
     * @param message The success message to be displayed
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
     * Displays a warning toast message to the user.
     *
     * @param message The warning message to be displayed
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
     * Displays an error toast message to the user.
     *
     * @param message The error message to be displayed
     */
    public void showErrorToast(String message) {
        if (gameView != null) {
            Stage primaryStage = gameView.getPrimaryStage();
            if (primaryStage != null) {
                Dialog.showErrorToast(primaryStage, message);
            }
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public void setWaterLevel(int waterLevel) {
        islandController.setWaterLevel(waterLevel);
    }

    public void returnToMainMenu() {
        gameView.returnToMainMenu();
    }

    public void resetTileBorders() {
    }

    public boolean isGameStart() {
        return gameStart;
    }

    public GameSubjectImpl getGameSubject() {
        return gameSubject;
    }

    public Object updateBoard() {
        gameSubject.notifyBoardChanged();
        return null;
    }

    public void updateActionBar() {
        gameSubject.notifyActionBarChanged();
    }

    public void updatePlayersInfo() {
        gameSubject.notifyPlayerInfoChanged();
    }

    public void updateCardView() {
        gameSubject.notifyCardChanged();
    }

    public void updateWaterLevel() {
        gameSubject.notifyWaterLevelChanged(islandController.getWaterLevel());
    }
}

