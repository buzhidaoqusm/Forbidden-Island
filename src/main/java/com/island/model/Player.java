package com.island.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.island.controller.GameController;

public abstract class Player {
    private String id;
    private String name;
    private Position position;
    private List<Card> cards;
    private PlayerRole role;
    private boolean hasDrawnTreasureCards;
    private int drawFloodCards;
    private List<TreasureType> capturedTreasures;
    private int actions;
    private GameController gameController;
    private static final int MAX_ACTIONS = 3;
    
    private boolean isReady;      // Player ready status
    private boolean inGame;       // Player in game status
    private boolean isHost;       // Player host status

    protected Player(String name, PlayerRole role) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.role = role;
        this.cards = new ArrayList<>();
        this.capturedTreasures = new ArrayList<>();
        this.hasDrawnTreasureCards = false;
        this.drawFloodCards = 0;
        this.actions = MAX_ACTIONS;
        this.isReady = false;
        this.inGame = false;
        this.isHost = false;
    }

    public abstract List<Position> getMovePositions(Map<Position, Tile> tiles);
    public abstract List<Position> getShorePositions(Map<Position, Tile> tiles);

    public void startTurn() {
        this.actions = MAX_ACTIONS;
        this.hasDrawnTreasureCards = false;
        this.drawFloodCards = 0;
    }

    public boolean canPerformAction() {
        return actions > 0;
    }

    public void useAction() {
        if (actions > 0) {
            actions--;
        }
    }

    public void move(Position newPosition) {
        if (canPerformAction()) {
            this.position = newPosition;
            useAction();
        }
    }

    public void shoreUp(Position position, Map<Position, Tile> tiles) {
        if (canPerformAction() && tiles.containsKey(position)) {
            Tile tile = tiles.get(position);
            if (tile.isFlooded()) {
                tile.shoreUp();
                useAction();
            }
        }
    }

    public void giveCard(Player targetPlayer, Card card) {
        if (canPerformAction() && cards.contains(card)) {
            cards.remove(card);
            targetPlayer.addCard(card);
            useAction();
        }
    }

    public void useCard(Card card) {
        if (cards.contains(card)) {
            card.useCard(this);
        }
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public Card removeCard(String cardName) {
        for (Card card : cards) {
            if (card.getName().equals(cardName)) {
                cards.remove(card);
                card.setBelongingPlayer("");
                return card;
            }
        }
        return null;
    }

    public void removeCard(Card card) {
        if (card != null) {
            cards.remove(card);
            card.setBelongingPlayer(null);
        }
    }
    public void addCaptureTreasure(TreasureType treasure) {
        capturedTreasures.add(treasure);
    }

    public boolean canCaptureTreasure(TreasureType treasureType) {
        if (!canPerformAction()) return false;
        
        int treasureCardCount = 0;
        for (Card card : cards) {
            if (card.getType() == CardType.TREASURE && card.getTreasureType() == treasureType) {
                treasureCardCount++;
            }
        }
        return treasureCardCount >= 4;
    }

    /**
     * Captures a treasure and adds it to the player's collection
     * @param treasureType The type of treasure to capture
     */
    public void captureTreasure(TreasureType treasureType) {
        if (!capturedTreasures.contains(treasureType)) {
            capturedTreasures.add(treasureType);
        }
    }

    /**
     * Gets the list of treasures captured by this player
     * @return List of captured treasures
     */
    public List<TreasureType> getCapturedTreasures() {
        return new ArrayList<>(capturedTreasures);
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public List<Card> getCards() {
        return cards;
    }

    public PlayerRole getRole() {
        return role;
    }

    public boolean hasDrawnTreasureCards() {
        return hasDrawnTreasureCards;
    }

    public void setHasDrawnTreasureCards(boolean hasDrawnTreasureCards) {
        this.hasDrawnTreasureCards = hasDrawnTreasureCards;
    }

    public int getDrawFloodCards() {
        return drawFloodCards;
    }

    public void addDrawnFloodCards(int count) {
        this.drawFloodCards += count;
    }

    public int getActions() {
        return actions;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Player player = (Player) obj;
        return id.equals(player.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public GameController getGameController() { 
        return gameController; 
    }

    public void setGameController(GameController gameController) { 
        this.gameController = gameController; 
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        this.isReady = ready;
    }

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        this.isHost = host;
    }

    public void resetState() {
        this.actions = MAX_ACTIONS;
        this.hasDrawnTreasureCards = false;
        this.drawFloodCards = 0;
    }

}