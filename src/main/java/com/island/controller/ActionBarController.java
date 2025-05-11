package com.island.controller;

import com.island.model.*;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

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

    public void handleMoveOtherPlayerAction() {
    }

    public void handleCaptureTreasureAction() {
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
