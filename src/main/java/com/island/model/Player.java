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
    
    // 添加新的字段
    private boolean isReady;      // 玩家是否准备就绪
    private boolean inGame;       // 玩家是否在游戏中
    private boolean isHost;       // 玩家是否是房主

    protected Player(String name, PlayerRole role) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.role = role;
        this.cards = new ArrayList<>();
        this.capturedTreasures = new ArrayList<>();
        this.hasDrawnTreasureCards = false;
        this.drawFloodCards = 0;
        this.actions = MAX_ACTIONS;
        this.isReady = false;     // 初始化时未准备
        this.inGame = false;      // 初始化时不在游戏中
        this.isHost = false;      // 初始化时不是房主
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
        cards.remove(card);
    }

    public void addCaptureTreasure(TreasureType treasure) {
        capturedTreasures.add(treasure);
    }

    public boolean canCaptureTreasure(TreasureType treasureType) {
        if (!canPerformAction()) return false;
        
        int treasureCardCount = 0;
        for (Card card : cards) {
            if (card instanceof TreasureCard && card.getTreasureType() == treasureType) {
                treasureCardCount++;
            }
        }
        return treasureCardCount >= 4;
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

    public void setDrawFloodCards(int drawFloodCards) {
        this.drawFloodCards = drawFloodCards;
    }

    public List<TreasureType> getCapturedTreasures() {
        return capturedTreasures;
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
    public GameController getGameController() { return gameController; }
    public void setGameController(GameController gameController) { this.gameController = gameController; }

    // 添加新的方法
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

    // 修改 resetState 方法
    public void resetState() {
        this.actions = MAX_ACTIONS;
        this.hasDrawnTreasureCards = false;
        this.drawFloodCards = 0;
        // 不重置 isReady 和 inGame 状态，因为这些是游戏会话级别的状态
    }
}