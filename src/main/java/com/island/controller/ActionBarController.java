package com.island.controller;

import com.island.models.Room;
import com.island.models.adventurers.*;
import com.island.models.island.*;
import com.island.models.treasure.TreasureType;
import com.island.models.card.*;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

import static com.island.util.ui.Dialog.showMessage;

/**
 * Controller class responsible for managing the action bar UI and game actions.
 * Handles player actions, validates moves, and updates the game state accordingly.
 * This class serves as the interface between the UI and the game logic for all player actions,
 * including movement, card usage, treasure capture, and turn management.
 */
public class ActionBarController {
    /** Reference to the main game controller */
    private GameController gameController;
    /** The current player whose actions are being managed */
    private Player currentPlayer;

    /**
     * Constructs a new ActionBarController.
     */
    public ActionBarController() {
    }

    /**
     * Checks if a player can play a special action card.
     * @param player Player to check
     * @return true if the player has any playable special cards
     */
    public boolean canPlaySpecialCard(Player player) {
        return gameController.getPlayerController().canPlaySpecialCard(player);
    }

    /**
     * Checks if a player can shore up any adjacent tiles.
     * @param player Player to check
     * @return true if there are any valid tiles to shore up
     */
    public boolean canShoreUpTile(Player player) {
        return gameController.getPlayerController().canShoreUpTile(player);
    }

    /**
     * Checks if a player can give a card to another player.
     * @param player Player attempting to give a card
     * @return true if the player can give a card
     */
    public boolean canGiveCard(Player player) {
        return gameController.getPlayerController().canGiveCard(player);
    }

    /**
     * Checks if a player can capture a treasure.
     * @param player Player attempting to capture treasure
     * @return true if the player can capture a treasure
     */
    public boolean canCaptureTreasure(Player player) {
        return gameController.getPlayerController().canCaptureTreasure(player);
    }

    /**
     * Checks if the current player has drawn treasure cards this turn.
     * @return true if treasure cards have been drawn
     */
    public boolean hasDrawnTreasureCards() {
        return gameController.getPlayerController().hasDrawnTreasureCards();
    }

    /**
     * Sends a message to draw treasure cards for a player.
     * @param count Number of cards to draw
     * @param player Player drawing the cards
     */
    public void sendDrawTreasureCardsMessage(int count, Player player) {
        gameController.getRoomController().sendDrawTreasureCardsMessage(count, player);
    }

    /**
     * Gets the number of flood cards drawn in the current turn.
     * @return Number of flood cards drawn
     */
    public int getDrawnFloodCards() {
        return gameController.getPlayerController().getDrawnFloodCards();
    }

    /**
     * Handles player movement action.
     * Validates the move and sends appropriate network messages.
     * Handles both normal movement and movement from sunk tiles.
     */
    public void handleMoveAction() {
        Tile chosenTile = gameController.getChosenTile();
        Tile playerTile = getIsland().getTile(currentPlayer.getPosition());
        if (playerTile.isSunk()) {
            List<Tile> validTilesOnSunk = gameController.getValidTilesOnSunk(currentPlayer);
            if (chosenTile != null && validTilesOnSunk.contains(chosenTile)) {
                gameController.getRoomController().sendMoveMessage(currentPlayer, chosenTile.getPosition());
            } else {
                gameController.showErrorToast("Invalid Tile!");
            }
        } else if (getRemainingActions() > 0) {
            List<Position> validPositions = currentPlayer.getMovePositions(getIsland().getTiles());
            if (chosenTile != null && validPositions.contains(chosenTile.getPosition())) {
                gameController.getRoomController().sendMoveMessage(currentPlayer, chosenTile.getPosition());
            } else {
                gameController.showErrorToast("Invalid Move!");
            }
        }
        notifyActionPerformed();
    }

    /**
     * Handles shore up action.
     * Validates the tile and sends appropriate network messages.
     */
    public void handleShoreUpAction() {
        if (getRemainingActions() > 0) {
            Tile chosenTile = gameController.getChosenTile();
            List<Position> validPositions = currentPlayer.getShorePositions(getIsland().getTiles());
            if (chosenTile != null && chosenTile.getState() == Tile.TileState.FLOODED && validPositions.contains(chosenTile.getPosition())) {
                gameController.getRoomController().sendShoreUpMessage(currentPlayer, chosenTile.getPosition());
            } else {
                gameController.showErrorToast("Invalid Tile!");
            }
        }
        notifyActionPerformed();
    }

    /**
     * Handles giving a card to another player.
     * Shows dialog for selecting player and card, validates the action,
     * and sends appropriate network messages.
     */
    public void handleGiveCardAction() {
        if (getRemainingActions() > 0) {
            Player currentPlayer = gameController.getCurrentPlayer();

            // Get current player's cards
            List<Card> playerCards = currentPlayer.getCards();
            if (playerCards.isEmpty()) {
                gameController.showWarningToast("You have no cards to give");
                return;
            }

            Room room = gameController.getRoom();

            // Get eligible players who can receive cards
            List<Player> eligiblePlayers = currentPlayer.getGiveCardPlayers(room.getPlayers());

            if (eligiblePlayers.isEmpty()) {
                gameController.showWarningToast("No eligible players to give cards");
                return;
            }

            // Create player selection dialog
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
                // Only treasure cards can be given, not special cards
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

            // Create dialog
            Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
            dialog.setTitle("Give Card");
            dialog.setHeaderText("Give a card to another player");
            dialog.getDialogPane().setContent(dialogContent);

            // Show dialog and handle result
            dialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    String selectedPlayerName = playerComboBox.getValue();
                    String selectedCardName = cardComboBox.getValue();

                    if (selectedPlayerName != null && selectedCardName != null) {
                        // Find selected player and card
                        Player selectedPlayer = room.getPlayerByUsername(selectedPlayerName);

                        Card selectedCard = null;
                        for (Card card : playerCards) {
                            if (card.getName().equals(selectedCardName)) {
                                selectedCard = card;
                                break;
                            }
                        }

                        if (selectedPlayer != null && selectedCard != null) {
                            // Send network message
                            gameController.getRoomController().sendGiveCardMessage(currentPlayer, selectedPlayer, selectedCard);
                        }
                    }
                }
            });
        }
        notifyActionPerformed();
    }

    /**
     * Handles Navigator's special ability to move other players.
     * Shows dialog for selecting player and number of moves,
     * then waits for tile selection.
     */
    public void handleMoveOtherPlayerAction() {
        if (getRemainingActions() > 0) {
            Player currentPlayer = gameController.getCurrentPlayer();
            Room room = gameController.getRoom();

            // Ensure current player is Navigator
            if (currentPlayer.getRole() != PlayerRole.NAVIGATOR) {
                gameController.showErrorToast("Only Navigator can move other players");
                return;
            }
            Navigator navigator = (Navigator) currentPlayer;

            // Get movable players
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

            // Create player selection dialog
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

            // Create dialog
            Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
            dialog.setTitle("Move Other Player");
            dialog.setHeaderText("Move another player up to 2 adjacent tiles");
            dialog.getDialogPane().setContent(dialogContent);

            // Show dialog and handle result
            dialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    String selectedPlayerName = playerComboBox.getValue();
                    Integer selectedMoves = movesComboBox.getValue();

                    if (selectedPlayerName != null && selectedMoves != null) {
                        // Find selected player
                        Player selectedPlayer = room.getPlayerByUsername(selectedPlayerName);

                        if (selectedPlayer != null) {
                            // Set selected player and move count
                            navigator.setNavigatorTarget(selectedPlayer, selectedMoves);

                            // Prompt user to select destination
                            showMessage("Select Destination",
                                    "Now click on a tile to move " + selectedPlayer.getName() +
                                            " there. You can move them up to " + selectedMoves + " adjacent tiles.");
                        }
                    }
                }
            });
        }
    }

    /**
     * Handles capturing a treasure.
     * Validates treasure location and card requirements,
     * then sends appropriate network messages.
     */
    public void handleCaptureTreasureAction() {
        if (getRemainingActions() > 0) {
            Player currentPlayer = gameController.getCurrentPlayer();

            // Get treasure type of current tile
            Position playerPosition = currentPlayer.getPosition();
            Tile currentTile = getIsland().getTile(playerPosition);

            if (currentTile == null || currentTile.getTreasureType() == null) {
                showMessage("No Treasure", "You are not on a treasure tile.");
                return;
            }

            TreasureType treasureType = currentTile.getTreasureType();

            // Check if player has enough treasure cards
            int treasureCardCount = 0;
            for (Card card : currentPlayer.getCards()) {
                if (card.getType() == CardType.TREASURE && card.getTreasureType() == treasureType) {
                    treasureCardCount++;
                }
            }

            if (treasureCardCount < 4) {
                showMessage("Not Enough Cards", "You need 4 matching treasure cards to capture this treasure. You have " + treasureCardCount + " " + treasureType.getDisplayName() + " cards.");
                return;
            }

            // Confirm treasure capture
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Capture Treasure");
            confirmDialog.setHeaderText("Capture " + treasureType.getDisplayName());
            confirmDialog.setContentText("Do you want to discard 4 " + treasureType.getDisplayName() + " cards to capture this treasure?");

            confirmDialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    // Execute treasure capture
                    gameController.getRoomController().sendCaptureTreasureMessage(currentPlayer, treasureType);
                }
            });
        }
        notifyActionPerformed();
    }

    /**
     * Handles end turn action.
     * Sends appropriate network message to end the current player's turn.
     */
    public void handleEndTurnAction() {
        gameController.getRoomController().sendEndTurnMessage(currentPlayer);
    }

    /**
     * Handles playing special cards.
     * Delegates to game controller for special card logic.
     */
    public void handlePlaySpecialAction() {
        gameController.handlePlaySpecialAction();
    }

    /**
     * Handles drawing flood cards.
     * Updates flood card count and sends network message.
     */
    public void handleDrawFloodAction() {
        gameController.getPlayerController().addDrawnFloodCards(1);
        gameController.getRoomController().sendDrawFloodMessage(1, currentPlayer.getName());
    }

    /**
     * Sets whether the current player has drawn treasure cards this turn.
     * @param hasDrawn true if treasure cards have been drawn
     */
    public void setHasDrawnTreasureCards(boolean hasDrawn) {
        gameController.getPlayerController().setHasDrawnTreasureCards(hasDrawn);
    }

    /**
     * Handles discarding a card.
     * Delegates to game controller for discard logic.
     */
    public void handleDiscardAction() {
        gameController.handleDiscardAction();
    }

    /**
     * Advances to the next player's turn.
     */
    public void nextTurn() {
        gameController.nextTurn();
    }

    /**
     * Checks if any player is on a sunk tile.
     * @return true if any player is on a sunk tile
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
     * Handles the case when a player is on a sunk tile.
     * @param currentProgramPlayer The player on a sunk tile
     */
    public void handlePlayerSunk(Player currentProgramPlayer) {
        gameController.handlePlayerSunk(currentProgramPlayer);
    }

    /**
     * Cleans up controller state.
     * Called when shutting down the game.
     */
    public void shutdown() {
        currentPlayer = null;
        gameController = null;
    }

    /**
     * Notifies observers that an action has been performed.
     * Updates the action bar UI.
     */
    private void notifyActionPerformed() {
        if (gameController != null) {
            gameController.updateActionBar();
        }
    }


    /**
     * Sets the game controller reference and initializes current player.
     * @param gameController The main game controller
     */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
        currentPlayer = gameController.getCurrentPlayer();
    }

    /**
     * Gets the current active player.
     * @return The current player
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Sets the current active player and updates the UI.
     * @param currentPlayer The new current player
     */
    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;

        // Notify observers when current player changes
        if (gameController != null) {
            gameController.updateActionBar();
        }
    }

    // Getters and utility methods
    public int getRemainingActions() { return gameController.getRemainingActions(); }
    public Room getRoom() { return gameController.getRoom(); }
    public GameController getGameController() { return gameController; }
    public Island getIsland() { return gameController.getIsland(); }

}
