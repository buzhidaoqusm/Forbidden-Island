package com.island.controller;

import com.island.model.*;
import com.island.network.Message;
import com.island.network.MessageHandler;
import com.island.network.RoomController;

import java.util.*;

/**
 * The GameController class is responsible for managing the core logic and state of the game.
 * It interacts with multiple other controllers (such as RoomController and PlayerController) to coordinate different aspects of the game.
 * This class maintains objects such as the game view, players, rooms, islands, etc., and manages the flow of the game, such as action rounds, game starts and ends, etc.
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
    private Card activeSpecialCard; // 当前正在使用的特殊卡

    // Reaming actions of current player
    private int remainingActions = 3;

    // Game start flag
    private boolean gameStart = false;

    // Game over flag
    private boolean gameOver = false;

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
     * Handle game start logic. Initialize island, players and cards. And deal cards for players
     * @param seed Random seed for game initialization
     * */
    public void startGame(long seed) {
        gameStart = true;
        currentPlayer = room.getPlayers().getFirst();

        islandController.initIsland(seed);
        playerController.initPlayers(seed);
        cardController.initCards(seed);

        // Deal cards for players
        playerController.dealCards(cardController.getTreasureDeck());

        gameView.initGame();
        gameView.setPrimaryStage();
    }

    /**
     * Handle turn start logic. Reset player state and action bar
     * @param player The player whose turn will start
     * */
    public void startTurn(Player player) {
        currentPlayer = player;
        currentPlayer.resetState();
        actionBarController.setCurrentPlayer(player);
        remainingActions = 3;
        playerController.resetPlayerState();
    }

    public void handlePlayerJoin(Message message) throws Exception {
        roomController.handleJoinRequest(message);
    }

    public RoomController getRoomController() {
        return roomController;
    }

    public void shutdown() {

    }

    /**
     * Handle the logic of drawing the water rise card.
     * */
    public void handleWaterRise() {
        islandController.increaseWaterLevel();
        if (islandController.getWaterLevel() == 10 ) {
            gameOver = true;
            roomController.sendGameOverMessage("Water level has reached the maximum!");
        }
        cardController.handleWaterRise();
    }

    /**
     * Handle the logic of drawing treasure cards.
     * @param count The number of cards to draw
     * @param player The player who is drawing the cards
     * */
    public void handleDrawTreasureCard(int count, Player player) {
        cardController.drawTreasureCard(count, player);
    }

    /**
     * Handle the logic of giving a card from one player to another.
     * @param fromPlayer The player giving the card
     * @param toPlayer The player receiving the card
     * @param card The card to be given
     * */
    public void giveCard(Player fromPlayer, Player toPlayer, String card) {
        Card removedCard = fromPlayer.removeCard(card);
        toPlayer.addCard(removedCard);
        decreaseRemainingActions();
    }

    public void addTreasureDiscardPile(Card card) {
        cardController.addTreasureDiscardPile(card);
    }

    /**
     * Handle the logic of drawing flood cards.
     * @param count The number of flood cards to draw
     * */
    public List<Position> drawFloodCards(int count) {
        List<Position> floodedPositions = cardController.drawFloodCards(count);
        // TODO: Check if the game is over
        return floodedPositions;
    }


    /**
     * Handle the logic of discarding a card.
     * */
    public void handleDiscardAction() {
        Card chosenCard = playerController.getChosenCard();
        if (chosenCard != null) {
            int cardIndex = room.getCurrentProgramPlayer().getCards().indexOf(chosenCard);
            roomController.sendDiscardMessage(room.getCurrentProgramPlayer(), cardIndex);
            playerController.setChosenCard(null);
        }
    }

    public IslandController getIslandController() {
        return islandController;
    }

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

    }

    private void handleSandbagsCard(Card chosenCard) {

    }

    private void handleHelicopterCard(Card chosenCard) {

    }

    public void handleUseSpecialCard(Position position) {

    }

    private void executeSandbagsUse(Position position) {

    }

    private void executeHelicopterMove(Position position) {

    }





    public void nextTurn() {

    }

    public void handlePlayerSunk(Player currentProgramPlayer) {

    }

    // 获取玩家可移动的板块
    public List<Tile> getValidTilesOnSunk(Player player) {
        return null;
    }

    public void showToast(String message) {

    }

    public void showSuccessToast(String message) {

    }

    public void showWarningToast(String message) {

    }

    public void showErrorToast(String message) {
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
    }

    public void resetTileBorders() {
    }

    public boolean isGameStart() {
        return gameStart;
    }
}

