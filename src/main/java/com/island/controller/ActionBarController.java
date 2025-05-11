package com.island.controller;

import com.island.model.Island;
import com.island.model.Player;
import com.island.model.Tile;
import com.island.model.Position;

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

    public void handleShoreUpAction() {

    }

    public void handleGiveCardAction() {

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
