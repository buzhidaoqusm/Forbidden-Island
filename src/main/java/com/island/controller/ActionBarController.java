package com.island.controller;

import com.island.model.*;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * The ActionBarController class manages the player action controls and interactions.
 *
 * This controller:
 * - Handles all player actions during their turn (move, shore up, give cards, etc.)
 * - Manages action-specific dialog boxes and user interface elements
 * - Tracks the remaining actions for the current player
 * - Coordinates with other controllers to execute player choices
 * - Validates action eligibility based on game rules and current state
 * - Implements role-specific action capabilities (like Navigator's special move)
 * - Manages the end-of-turn sequence when players are finished
 */
public class ActionBarController {
    /**
     * Reference to the main game controller
     */
    private GameController gameController;

    /**
     * The player whose turn it currently is
     */
    private Player currentPlayer;

    /**
     * Constructs a new ActionBarController with default values
     */
    public ActionBarController() {

    }

    /**
     * Establishes a bidirectional link with the game controller and
     * initializes the current player reference
     *
     * @param gameController The main controller for the game
     */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
        currentPlayer = gameController.getCurrentPlayer();
    }

    /**
     * Gets the current active player
     *
     * @return The player whose turn it currently is
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Updates the current active player
     *
     * @param currentPlayer The player to set as current
     */
    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    /**
     * Gets the number of actions remaining for the current player
     *
     * @return The count of remaining actions
     */
    public int getRemainingActions() {
        return gameController.getRemainingActions();
    }

    /**
     * Gets the current game room
     *
     * @return The Room object containing game state
     */
    public Room getRoom() {
        return gameController.getRoom();
    }

    /**
     * Gets the main game controller
     *
     * @return The GameController instance
     */
    public GameController getGameController() {
        return gameController;
    }

    /**
     * Gets the island model
     *
     * @return The Island model containing the game board
     */
    public Island getIsland() {
        return gameController.getIsland();
    }

    /**
     * Checks if the player has a special card they can play
     *
     * @param player The player to check
     * @return true if the player can play a special card, false otherwise
     */
    public boolean canPlaySpecialCard(Player player) {
        return gameController.getPlayerController().canPlaySpecialCard(player);
    }

    /**
     * Checks if the player can shore up any nearby flooded tiles
     *
     * @param player The player to check
     * @return true if the player can shore up a tile, false otherwise
     */
    public boolean canShoreUpTile(Player player) {
        return gameController.getPlayerController().canShoreUpTile(player);
    }

    /**
     * Checks if the player can give a card to another player
     *
     * @param player The player to check
     * @return true if the player can give a card, false otherwise
     */
    public boolean canGiveCard(Player player) {
        return gameController.getPlayerController().canGiveCard(player);
    }

    /**
     * Checks if the player can capture a treasure
     *
     * @param player The player to check
     * @return true if the player can capture a treasure, false otherwise
     */
    public boolean canCaptureTreasure(Player player) {
        return gameController.getPlayerController().canCaptureTreasure(player);
    }

    /**
     * Checks if the player has already drawn treasure cards this turn
     *
     * @return true if the player has drawn treasure cards, false otherwise
     */
    public boolean hasDrawnTreasureCards() {
        return gameController.getPlayerController().hasDrawnTreasureCards();
    }

    /**
     * Sends a message that the player is drawing treasure cards
     *
     * @param i The number of cards to draw
     * @param player The player drawing the cards
     */
    public void sendDrawTreasureCardsMessage(int i, Player player) {
        gameController.getRoomController().sendDrawTreasureCardsMessage(i, player);
    }

    /**
     * Gets the number of flood cards drawn in the current turn
     *
     * @return The count of flood cards drawn
     */
    public int getDrawnFloodCards() {
        return gameController.getPlayerController().getDrawnFloodCards();
    }

    /**
     * Handles the player movement action.
     * Validates the chosen tile for movement based on the player's position and state.
     * If the player is on a sunk tile, special movement rules apply.
     * Only allows movement to valid adjacent tiles that aren't sunk, following role-specific rules.
     */
    public void handleMoveAction() {
        // 检查当前玩家是否为空
        if (currentPlayer == null) {
            gameController.showErrorToast("No active player!");
            return;
        }

//        Tile chosenTile = gameController.getChosenTile();
//        if (chosenTile == null) {
//            gameController.showErrorToast("Please select a tile first!");
//            return;
//        }

//        Tile playerTile = getIsland().getTile(currentPlayer.getPosition());
//        if (playerTile == null) {
//            gameController.showErrorToast("Player position is invalid!");
//            return;
//        }11

//        if (playerTile.isSunk()) {
//            List<Tile> validTilesOnSunk = gameController.getValidTilesOnSunk(currentPlayer);
//            if (validTilesOnSunk.contains(chosenTile)) {
//                gameController.getRoomController().sendMoveMessage(currentPlayer, chosenTile.getPosition());
//            } else {
//                gameController.showErrorToast("Invalid Tile!");
//            }
//            return; // 添加return语句，防止执行下面的代码
//        }
//
//        if (getRemainingActions() > 0) {
//            List<Position> validPositions = currentPlayer.getMovePositions(getIsland().getGameMap());
//            if (validPositions.contains(chosenTile.getPosition())) {
//                gameController.getRoomController().sendMoveMessage(currentPlayer, chosenTile.getPosition());
//            } else {
//                gameController.showErrorToast("Invalid Move!");
//            }
//        } else {
//            gameController.showErrorToast("No actions remaining!");
//        }
        List<Position> validPositions = currentPlayer.getMovePositions(getIsland().getGameMap());
        gameController.getGameView().getIslandView().highlightTiles(validPositions, "hightlight-move");
    }

    /**
     * Handles the shore up action to stabilize a flooded tile.
     * Validates if the chosen tile is flooded and in range for the current player.
     * Only allows shoring up flooded tiles that are adjacent or the player's current tile.
     */
    public void handleShoreUpAction() {
        // 检查当前玩家是否为空
        if (currentPlayer == null) {
            gameController.showErrorToast("No active player!");
            return;
        }

        if (getRemainingActions() <= 0) {
            gameController.showErrorToast("No actions remaining!");
            return;
        }

        Tile chosenTile = gameController.getChosenTile();
        if (chosenTile == null) {
            gameController.showErrorToast("Please select a tile first!");
            return;
        }

        List<Position> validPositions = currentPlayer.getShorePositions(getIsland().getGameMap());
        if (chosenTile.getState() == TileState.FLOODED && validPositions.contains(chosenTile.getPosition())) {
            gameController.getRoomController().sendShoreUpMessage(currentPlayer, chosenTile.getPosition());
        } else {
            gameController.showErrorToast("Invalid Tile!");
        }
    }

    /**
     * Handles the action of giving a card to another player.
     * Shows a dialog allowing the player to select both a recipient and a card to give.
     * Validates that the current player has cards to give and that there are eligible recipients.
     * Only treasure cards can be given to other players.
     */
    public void handleGiveCardAction() {
        // 检查当前玩家是否为空
        if (currentPlayer == null) {
            gameController.showErrorToast("No active player!");
            return;
        }

        if (getRemainingActions() <= 0) {
            gameController.showErrorToast("No actions remaining!");
            return;
        }

        // getting the current player's cards
        List<Card> playerCards = currentPlayer.getCards();
        if (playerCards.isEmpty()) {
            gameController.showWarningToast("You have no cards to give");
            return;
        }

        Room room = gameController.getRoom();

        // getting the eligible players to give cards
        List<Player> eligiblePlayers = new ArrayList<>();
        // Messenger can give cards to any player
        if (currentPlayer.getRole() == PlayerRole.MESSENGER) {
            for (Player player : room.getPlayers()) {
                if (!player.equals(currentPlayer)) {
                    eligiblePlayers.add(player);
                }
            }
        } else {
            // Other roles can only give cards to players on the same tile
            Position currentPos = currentPlayer.getPosition();
            for (Player player : room.getPlayers()) {
                if (!player.equals(currentPlayer) && player.getPosition().equals(currentPos)) {
                    eligiblePlayers.add(player);
                }
            }
        }

        if (eligiblePlayers.isEmpty()) {
            gameController.showWarningToast("No eligible players to give cards");
            return;
        }

        // creating the dialog
        VBox dialogContent = new VBox(10);
        dialogContent.setPadding(new Insets(20));

        Label selectPlayerLabel = new Label("Select a player to give a card to:");
        ComboBox<String> playerComboBox = new ComboBox<>();
        for (Player player : eligiblePlayers) {
            playerComboBox.getItems().add(player.getName());
        }
        playerComboBox.getSelectionModel().selectFirst();

        Label selectCardLabel = new Label("Select a card to give:");
        ComboBox<String> cardComboBox = new ComboBox<>();
        for (Card card : playerCards) {
            // only the treasure cards can be given
            if (card.getType() == CardType.TREASURE) {
                cardComboBox.getItems().add(card.getName());
            }
        }

        if (cardComboBox.getItems().isEmpty()) {
            gameController.showWarningToast("You have no card to give");
            return;
        }

        cardComboBox.getSelectionModel().selectFirst();

        dialogContent.getChildren().addAll(
                selectPlayerLabel, playerComboBox,
                selectCardLabel, cardComboBox
        );

        // creating the dialog
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Give Card");
        dialog.setHeaderText("Give a card to another player");
        dialog.getDialogPane().setContent(dialogContent);

        // showing the dialog
        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String selectedPlayerName = playerComboBox.getValue();
                String selectedCardName = cardComboBox.getValue();

                if (selectedPlayerName != null && selectedCardName != null) {
                    // find the selected player and card
                    Player selectedPlayer = null;
                    for (Player player : room.getPlayers()) {
                        if (player.getName().equals(selectedPlayerName)) {
                            selectedPlayer = player;
                            break;
                        }
                    }

                    Card selectedCard = null;
                    for (Card card : playerCards) {
                        if (card.getName().equals(selectedCardName)) {
                            selectedCard = card;
                            break;
                        }
                    }

                    if (selectedPlayer != null && selectedCard != null) {
                        // Find the index of the card in the player's hand
                        int cardIndex = playerCards.indexOf(selectedCard);
                        gameController.getRoomController().sendGiveCardMessage(currentPlayer, selectedPlayer, cardIndex);
                    }
                }
            }
        });
    }

    /**
     * Handles the Navigator special ability to move another player.
     * Shows a dialog to select a player and number of moves (1 or 2).
     * Validates that the current player is a Navigator and there are other players to move.
     * Sets up the Navigator's target for subsequent tile selection.
     */
    public void handleMoveOtherPlayerAction() {
        // 检查当前玩家是否为空
        if (currentPlayer == null) {
            gameController.showErrorToast("No active player!");
            return;
        }

        if (getRemainingActions() <= 0) {
            gameController.showErrorToast("No actions remaining!");
            return;
        }
        Room room = gameController.getRoom();

        // check if the current player is a Navigator
        if (currentPlayer.getRole() != PlayerRole.NAVIGATOR) {
            gameController.showErrorToast("Only Navigator can move other players");
            return;
        }
        Navigator navigator = (Navigator) currentPlayer;

        // getting the eligible players to move
        List<Player> movablePlayers = new ArrayList<>();
        for (Player player : room.getPlayers()) {
            if (!player.getName().equals(navigator.getName())) {
                movablePlayers.add(player);
            }
        }

        if (movablePlayers.isEmpty()) {
            gameController.showWarningToast("No other players to move");
            return;
        }

        // creating the components for the dialog
        VBox dialogContent = new VBox(10);
        dialogContent.setPadding(new Insets(20));

        Label selectPlayerLabel = new Label("Select a player to move:");
        ComboBox<String> playerComboBox = new ComboBox<>();
        for (Player player : movablePlayers) {
            playerComboBox.getItems().add(player.getName());
        }
        playerComboBox.getSelectionModel().selectFirst();

        Label movesLabel = new Label("Number of moves (1 or 2):");
        ComboBox<Integer> movesComboBox = new ComboBox<>();
        movesComboBox.getItems().addAll(1, 2);
        movesComboBox.getSelectionModel().select(0);

        dialogContent.getChildren().addAll(
                selectPlayerLabel, playerComboBox,
                movesLabel, movesComboBox
        );

        // creating the dialog
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Move Other Player");
        dialog.setHeaderText("Move another player up to 2 adjacent tiles");
        dialog.getDialogPane().setContent(dialogContent);

        // showing the dialog
        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String selectedPlayerName = playerComboBox.getValue();
                Integer selectedMoves = movesComboBox.getValue();

                if (selectedPlayerName != null && selectedMoves != null) {
                    // find the selected player
                    Player selectedPlayer = null;
                    for (Player player : room.getPlayers()) {
                        if (player.getName().equals(selectedPlayerName)) {
                            selectedPlayer = player;
                            break;
                        }
                    }

                    if (selectedPlayer != null) {
                        // set the navigator target
                        navigator.setNavigatorTarget(selectedPlayer, selectedMoves);

                        // show the message to select the destination
                        gameController.showToast("Select Destination: Now click on a tile to move " +
                                selectedPlayer.getName() + " there. You can move them up to " +
                                selectedMoves + " adjacent tiles.");
                    }
                }
            }
        });
    }

    /**
     * Handles the action of capturing a treasure.
     * Validates that the player is on a treasure tile and has 4 matching treasure cards.
     * Shows a confirmation dialog before processing the capture.
     * Discards the required cards and registers the captured treasure.
     */
    public void handleCaptureTreasureAction() {
        // 检查当前玩家是否为空
        if (currentPlayer == null) {
            gameController.showErrorToast("No active player!");
            return;
        }

        if (getRemainingActions() <= 0) {
            gameController.showErrorToast("No actions remaining!");
            return;
        }

        // getting the current player's position and tile
        Position playerPosition = currentPlayer.getPosition();
        Tile currentTile = getIsland().getTile(playerPosition);

        if (currentTile == null || currentTile.getTreasureType() == null) {
            gameController.showWarningToast("You are not on a treasure tile.");
            return;
        }

        TreasureType treasureType = currentTile.getTreasureType();

        // checking if the player has enough matching treasure cards
        int treasureCardCount = 0;
        for (Card card : currentPlayer.getCards()) {
            if (card.getType() == CardType.TREASURE && card.getTreasureType() == treasureType) {
                treasureCardCount++;
            }
        }

        if (treasureCardCount < 4) {
            gameController.showWarningToast("You need 4 matching treasure cards to capture this treasure. You have " +
                    treasureCardCount + " " + treasureType.getDisplayName() + " cards.");
            return;
        }

        // confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Capture Treasure");
        confirmDialog.setHeaderText("Capture " + treasureType.getDisplayName());
        confirmDialog.setContentText("Do you want to discard 4 " + treasureType.getDisplayName() + " cards to capture this treasure?");

        confirmDialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                // Find the treasure card indices to discard
                List<Integer> cardIndices = new ArrayList<>();
                List<Card> playerCards = currentPlayer.getCards();
                int count = 0;
                for (int i = 0; i < playerCards.size() && count < 4; i++) {
                    Card card = playerCards.get(i);
                    if (card.getType() == CardType.TREASURE && card.getTreasureType() == treasureType) {
                        cardIndices.add(i);
                        count++;
                    }
                }

                gameController.getRoomController().sendCaptureTreasureMessage(currentPlayer, cardIndices);
            }
        });
    }

    /**
     * Sends a message to end the current player's turn
     */
    public void handleEndTurnAction() {
        // 检查当前玩家是否为空
        if (currentPlayer == null) {
            gameController.showErrorToast("No active player!");
            return;
        }
        gameController.getRoomController().sendEndTurnMessage(currentPlayer);
    }

    /**
     * Initiates the process of playing a special card
     */
    public void handlePlaySpecialAction() {
        // 检查当前玩家是否为空
        if (currentPlayer == null) {
            gameController.showErrorToast("No active player!");
            return;
        }
        gameController.handlePlaySpecialAction();
    }

    /**
     * Handles drawing of flood cards
     * Increments the count of flood cards drawn and sends a message to the room
     */
    public void handleDrawFloodAction() {
        // 检查当前玩家是否为空
        if (currentPlayer == null) {
            gameController.showErrorToast("No active player!");
            return;
        }
        gameController.getPlayerController().addDrawnFloodCards(1);
        gameController.getRoomController().sendDrawFloodMessage(1, currentPlayer.getName());
    }

    /**
     * Sets the flag indicating whether the player has drawn treasure cards this turn
     *
     * @param hasDrawn Whether the player has drawn treasure cards
     */
    public void setHasDrawnTreasureCards(boolean hasDrawn) {
        gameController.getPlayerController().setHasDrawnTreasureCards(hasDrawn);
    }

    /**
     * Initiates the card discard action when the player has too many cards
     */
    public void handleDiscardAction() {
        gameController.handleDiscardAction();
    }

    /**
     * Progresses the game to the next player's turn
     */
    public void nextTurn() {
        gameController.nextTurn();
    }

    /**
     * Checks if any player is currently on a sunk tile
     *
     * @return true if any player is on a sunk tile, false otherwise
     */
    public boolean isAnyPlayerSunk() {
        Room room = gameController.getRoom();
        for (Player player : room.getPlayers()) {
            if (player.getPosition() != null) {
                Tile tile = getIsland().getTile(player.getPosition());
                if (tile.isSunk()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Handles the situation where a player is on a sunk tile
     * Coordinates with the game controller to manage player movement or game over conditions
     *
     * @param currentProgramPlayer The player on a sunk tile
     */
    public void handlePlayerSunk(Player currentProgramPlayer) {
        gameController.handlePlayerSunk(currentProgramPlayer);
    }

    /**
     * Cleans up resources when the game is shutting down
     */
    public void shutdown() {
        currentPlayer = null;
        gameController = null;
    }
}
