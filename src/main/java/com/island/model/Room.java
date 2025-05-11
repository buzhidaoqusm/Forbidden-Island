package com.island.model;
import java.util.ArrayList;
import java.util.List;
public class Room {
    private String roomID;
    private List<Player> playerList;
   
    private Player hostPlayer;

    private boolean gameStarted;

    public Room(String roomID, Player roomOwner) {
        this.roomID = roomID;
        this.playerList = new ArrayList<>();
        this.hostPlayer = roomOwner;
        this.gameStarted = false;
        playerList.add(roomOwner);
    }
    public List<Player> getPlayers() {
        return playerList;
    }
    public void addPlayer(Player player) {
        playerList.add(player);
    }

    public void startGame() {
        // 检查房间是否满足开始游戏的条件
        if (playerList.size() < 2 || playerList.size() > 4) {
            throw new IllegalStateException("游戏需要2-4名玩家才能开始");
        }

        // 检查所有玩家是否都已准备
        for (Player player : playerList) {
            if (!player.isReady()) {
                throw new IllegalStateException("有玩家尚未准备就绪");
            }
        }

        // 初始化玩家状态
        for (Player player : playerList) {
            player.resetState();
            player.setInGame(true);
        }

        // 设置游戏状态
        gameStarted = true;
    }

    public String getRoomID() {
        return roomID;
    }

    public List<Player> getPlayerList() {
        return playerList;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public boolean isHost(String playerName) {
        return hostPlayer != null && hostPlayer.getName().equals(playerName);
    }

    public void setHostPlayer(Player player) {
        this.hostPlayer = player;
    }
}