package com.island.controller;

import java.util.*;

/**
 * PlayerController is responsible for managing player-related operations in the game, including player character initialization, card distribution, player status check, etc.
 * As a component of the game controller, this class coordinates the interaction between the game logic and the player.
 * */
public class PlayerController {
    private GameController gameController;
    private Room room;
    private Card chosenCard;

    public PlayerController() {

    }

    /**
     * Set up the game controller and get the room object in the game.
     * @param gameController the GameController
     * */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
        room = gameController.getRoomController().getRoom();
    }

    /**
     * Initialize players and randomly assign roles.
     * Assign each player a role and set its initial position.
     * @param seed The seed for randomization.
     * */
    public void initPlayers(long seed) {
        Island island = gameController.getIslandController().getIsland();
        ArrayList<PlayerRole> roles = new ArrayList<>(Arrays.asList(PlayerRole.values()));
        Collections.shuffle(roles, new Random(seed));
        int playerCount = room.getPlayers().size();
        List<Player> players = room.getPlayers();
        for (int i = 0; i < playerCount; i++) {
            Player player = players.get(i);
            switch (roles.get(i)) {
                case DIVER -> player = new Diver(player.getName());
                case ENGINEER -> player = new Engineer(player.getName());
                case EXPLORER -> player = new Explorer(player.getName());
                case MESSENGER -> player = new Messenger(player.getName());
                case NAVIGATOR -> player = new Navigator(player.getName());
                case PILOT -> player = new Pilot(player.getName());
            }
            // update the host player
            if (room.isHost(player.getName())) {
                room.setHostPlayer(player);
            }
            // update the current player
            if (gameController.getCurrentPlayer().getName().equals(player.getName())) {
                gameController.setCurrentPlayer(player);
            }
            // set initial position according to the player role
            player.setPosition(island.findTile(PlayerRole.getColor(player.getRole())));
            room.addPlayer(player);
        }
        // Remove the characters of the first playerCount players from the room
        room.getPlayers().subList(0, playerCount).clear();

        // update player info after initialization
        if (gameController != null) {
            gameController.updatePlayersInfo();
        }
    }

    /**
     * Deal the initial cards to each player
     * @param treasureDeck The deck of treasure cards
     * */
    public void dealCards(Deque<Card> treasureDeck) {
        for (Player player : room.getPlayers()) {
            while (player.getCards().size() < 2) {
                Card card = treasureDeck.poll();
                // Ensure the card is not WATER_RISE, otherwise put it back into the deck
                if (card != null && card.getType() != CardType.WATER_RISE) {
                    player.addCard(card);
                } else {
                    treasureDeck.add(card);
                }
            }
        }
    }

    public Room getRoom() {
        return room;
    }

    public void setChosenCard(Card chosenCard) {
        this.chosenCard = chosenCard;
    }

    public Card getChosenCard() {
        return chosenCard;
    }

    public boolean canPlaySpecialCard(Player player) {

        return false;
    }

    public boolean canShoreUpTile(Player player) {
        return false;
    }

    public boolean canGiveCard(Player player) {

        return false;
    }

    public boolean canCaptureTreasure(Player player) {

        return false;
    }

    public boolean hasDrawnTreasureCards() {
    }

    public int getDrawnFloodCards() {
    }

    public void setHasDrawnTreasureCards(boolean hasDrawnTreasureCards) {

    }

    public void addDrawnFloodCards(int count) {
    }

    public void resetPlayerState() {
    }

    public void shutdown() {

    }

    public void movePlayer(Player player, Position position) {

    }

    public void addCard(Player player, Card card) {

    }

    public void removeCard(Player player, Card card) {

    }
}

