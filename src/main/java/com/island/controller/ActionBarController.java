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

public class ActionBarController {
    private GameController gameController;
    private Player currentPlayer;

    public ActionBarController() {

    }

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
        currentPlayer = gameController.getCurrentPlayer();
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }


    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public int getRemainingActions() {
        return gameController.getRemainingActions();
    }

    public Room getRoom() {
        return gameController.getRoom();
    }

    public GameController getGameController() {
        return gameController;
    }

    public Island getIsland() {
        return gameController.getIsland();
    }

    public boolean canPlaySpecialCard(Player player) {
        return gameController.getPlayerController().canPlaySpecialCard(player);
    }

    public boolean canShoreUpTile(Player player) {
        return gameController.getPlayerController().canShoreUpTile(player);
    }

    public boolean canGiveCard(Player player) {
        return gameController.getPlayerController().canGiveCard(player);
    }

    public boolean canCaptureTreasure(Player player) {
        return gameController.getPlayerController().canCaptureTreasure(player);
    }

    public boolean hasDrawnTreasureCards() {
        return gameController.getPlayerController().hasDrawnTreasureCards();
    }

    public void sendDrawTreasureCardsMessage(int i, Player player) {
        gameController.getRoomController().sendDrawTreasureCardsMessage(i, player);
    }

    public int getDrawnFloodCards() {
        return gameController.getPlayerController().getDrawnFloodCards();
    }

    /**
     * Handles the action of moving a player.
     * This method checks if the player is on a sunk tile and if the chosen tile is valid for movement.
     * If valid, it sends a move message to the room controller.
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
        }
        if (getRemainingActions() > 0) {
            List<Position> validPositions = currentPlayer.getMovePositions(getIsland().getTiles());
            if (chosenTile != null && validPositions.contains(chosenTile.getPosition())) {
                gameController.getRoomController().sendMoveMessage(currentPlayer, chosenTile.getPosition());
            } else {
                gameController.showErrorToast("Invalid Move!");
            }
        }
    }

    /**
     * Handles the action of shoring up a tile.
     * This method checks if the chosen tile is valid for shoring up and sends a shore up message to the room controller.
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
    }

    /**
     * Handles the action of giving a card to another player.
     * This method checks if the current player has cards to give and if there are eligible players to receive the card.
     * If valid, it shows a dialog for the user to select a player and a card to give.
     */
    public void handleGiveCardAction() {
        if (getRemainingActions() > 0) {
            Player currentPlayer = gameController.getCurrentPlayer();

            // getting the current player's cards
            List<Card> playerCards = currentPlayer.getCards();
            if (playerCards.isEmpty()) {
                gameController.showWarningToast("You have no cards to give");
                return;
            }

            Room room = gameController.getRoom();

            // getting the eligible players to give cards
            List<Player> eligiblePlayers = currentPlayer.getGiveCardPlayers(room.getPlayers());

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
                        Player selectedPlayer = room.getPlayerByUsername(selectedPlayerName);

                        Card selectedCard = null;
                        for (Card card : playerCards) {
                            if (card.getName().equals(selectedCardName)) {
                                selectedCard = card;
                                break;
                            }
                        }

                        if (selectedPlayer != null && selectedCard != null) {
                            gameController.getRoomController().sendGiveCardMessage(currentPlayer, selectedPlayer, selectedCard);
                        }
                    }
                }
            });
        }
    }

    /**
     * Handles the action of moving another player.
     * This method checks if the current player is a Navigator and if there are eligible players to move.
     * If valid, it shows a dialog for the user to select a player and the number of moves.
     */
    public void handleMoveOtherPlayerAction() {
        if (getRemainingActions() > 0) {
            Player currentPlayer = gameController.getCurrentPlayer();
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
                        Player selectedPlayer = room.getPlayerByUsername(selectedPlayerName);

                        if (selectedPlayer != null) {
                            // set the navigator target
                            navigator.setNavigatorTarget(selectedPlayer, selectedMoves);

                            // show the message to select the destination
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
     * Handles the action of capturing a treasure.
     * This method checks if the current player is on a treasure tile and has enough matching treasure cards.
     * If valid, it shows a confirmation dialog to discard the cards and capture the treasure.
     */
    public void handleCaptureTreasureAction() {
        if (getRemainingActions() > 0) {
            Player currentPlayer = gameController.getCurrentPlayer();

            // getting the current player's position and tile
            Position playerPosition = currentPlayer.getPosition();
            Tile currentTile = getIsland().getTile(playerPosition);

            if (currentTile == null || currentTile.getTreasureType() == null) {
                showMessage("No Treasure", "You are not on a treasure tile.");
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
                showMessage("Not Enough Cards", "You need 4 matching treasure cards to capture this treasure. You have " + treasureCardCount + " " + treasureType.getDisplayName() + " cards.");
                return;
            }

            // confirmation dialog
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Capture Treasure");
            confirmDialog.setHeaderText("Capture " + treasureType.getDisplayName());
            confirmDialog.setContentText("Do you want to discard 4 " + treasureType.getDisplayName() + " cards to capture this treasure?");

            confirmDialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    gameController.getRoomController().sendCaptureTreasureMessage(currentPlayer, treasureType);
                }
            });
        }
    }

    public void handleEndTurnAction() {
    }

    public void handlePlaySpecialAction() {
        gameController.handlePlaySpecialAction();
    }

    public void handleDrawFloodAction() {
    }

    public void setHasDrawnTreasureCards(boolean b) {
        gameController.getPlayerController().setHasDrawnTreasureCards(b);
    }

    public void handleDiscardAction() {
        gameController.handleDiscardAction();
    }

    public void nextTurn() {
        gameController.nextTurn();
    }

    public boolean isAnyPlayerSunk() {
        return false;
    }

    public void handlePlayerSunk(Player currentProgramPlayer) {
    }

    public void shutdown() {
    }

}
