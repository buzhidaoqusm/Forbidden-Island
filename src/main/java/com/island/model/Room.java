package com.island.model;
import java.util.ArrayList;
import java.util.List;
public class Room {
    private String roomID;
    private List<Player> playerList;
    private Player roomOwner;

    public Room(String roomID, Player roomOwner) {
        this.roomID = roomID;
        this.playerList = new ArrayList<>();
        this.roomOwner = roomOwner;
        playerList.add(roomOwner);
    }

    public void addPlayer(Player player) {
        playerList.add(player);
    }

    public void startGame() {
        // 实现游戏开始逻辑
    }

    public String getRoomID() {
        return roomID;
    }

    public List<Player> getPlayerList() {
        return playerList;
    }

    public Player getRoomOwner() {
        return roomOwner;
    }
}