package com.island.network;

import com.island.controller.GameController;
import com.island.model.Player;
import com.island.model.Room;
import com.island.model.Position;
import com.island.model.Tile;
import com.island.model.Navigator;
import com.island.util.ActionLogView;

import java.io.IOException;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RoomController {
    private GameController gameController;
    private Room room;
    private MessageHandler messageHandler;
    private BroadcastSender sender;
    private BroadcastReceiver receiver;
    private Map<String, Long> playerHeartbeat;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public RoomController(GameController gameController, Room room) {
        this.gameController = Objects.requireNonNull(gameController, "GameController not empty");
        this.room = Objects.requireNonNull(room, "Room not empty");
        this.messageHandler = new MessageHandler(gameController, this, room, new ActionLogView());
        try {
            this.sender = new BroadcastSender("255.255.255.255", 8888);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        try {
            this.receiver = new BroadcastReceiver(this, 8888);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::broadcastHeartbeat, 0, 10, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::checkHeartbeats, 0, 15, TimeUnit.SECONDS);
        receiver.run(); // 直接运行接收线程
    }

    public void shutdown() {
        scheduler.shutdownNow();
        receiver.stop();
        sender.close();
    }

    private void broadcastHeartbeat() {
        Message msg = new Message(MessageType.MESSAGE_ACK, room.getRoomId(), "system");
        broadcast(msg);
    }

    private void checkHeartbeats() {
        long now = System.currentTimeMillis();
        List<String> disconnected = new ArrayList<>();

        playerHeartbeat.entrySet().removeIf(entry -> {
            if (now - entry.getValue() > 30000) {
                disconnected.add(entry.getKey());
                return true;
            }
            return false;
        });
        disconnected.forEach(this::handlePlayerDisconnect);
    }

    // Heartbeat related methods
    public void startHeartbeat() {
        scheduler.scheduleAtFixedRate(() -> {
            Message heartbeatMsg = new Message(MessageType.MESSAGE_ACK, room.getRoomId(), "system");
            broadcast(heartbeatMsg);
        }, 0, 10, TimeUnit.SECONDS);
    }

    public void startHeartbeatCheck() {
        scheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            playerHeartbeat.entrySet().removeIf(entry ->
                    currentTime - entry.getValue() > 30000 // over 30 seconds
            );
            playerHeartbeat.keySet().forEach(this::handlePlayerDisconnect);
        }, 0, 15, TimeUnit.SECONDS);
    }

    public void removeHeartbeat(String username) {
        playerHeartbeat.remove(username);
    }

    // Handle players connection
    public void handlePlayerDisconnect(String username) {
        Player player = room.getPlayerByName(username); // 新增获取Player的方法
        if (player != null) {
            room.removePlayer(player);
            broadcast(new Message(MessageType.PLAYER_LEAVE, "username","status", "disconnected")); // 使用Player信息
            // updateRoomStatus();
        }
    }

//    public void updatePlayerDisconnect(String username) {
//        removeHeartbeat(username);
//        room.markPlayerOffline(username);
//    }

    public void updatePlayerHeartbeat(String username) {
        playerHeartbeat.put(username, System.currentTimeMillis());
    }

    // 心跳相关方法
    public void broadcast(Message message) {
        try {
            Set<String> addresses = BroadcastAddressCalculator.getBroadcastAddresses();
            for (String addr : addresses) {
                sendToAddress(addr, message);
            }
        } catch (Exception e) {
            System.err.println("Broadcast failed: " + e.getMessage());
        }
    }

    private void sendToAddress(String address, Message message) {
        sender.broadcast(message);
    }

    // Message processing related methods
    public void handleJoinRequest(Message message) {
        Player username = (Player) message.getData().get("username");
        if (room.addPlayer(username)) {
            sendJoinResponse(username, true);
            broadcast(new Message(MessageType.PLAYER_JOIN,"username", "wants to join"));
        } else {
            sendJoinResponse(username, false);
        }
    }

    public void handleGameMessage(Message message) {
        messageHandler.handleMessage(message);
    }

    // Game status message sending method
    public void sendGameOverMessage(String description) {
        broadcast(new Message(MessageType.GAME_OVER, "description", "gameover", description));
    }

    public void sendUpdateRoomMessage() {
        broadcast(new Message(MessageType.UPDATE_ROOM, "players", "status"));
    }

    // Method for sending player action messages
    public void sendDrawTreasureCardsMessage(int count, Player player) {
        Message msg = new Message(MessageType.DRAW_TREASURE_CARD, room.getRoomId(), "system")
                .addExtraData("username", player.getName())
                .addExtraData("count", count);
        broadcast(msg);
    }

    public void sendDrawFloodMessage(int count, Player player) {
        Message msg = new Message(MessageType.DRAW_FLOOD_CARD, room.getRoomId(), "system")
                .addExtraData("username", player.getName())
                .addExtraData("count", count);
        broadcast(msg);
    }

    public void sendJoinResponse(Player username, boolean b) {
        Message msg = new Message(b ? MessageType.PLAYER_JOIN : MessageType.LEAVE_ROOM,
                room.getRoomId(), "system")
                .addExtraData("username", username)
                .addExtraData("status", b);
        handleGameMessage(msg);
    }

    public void sendMoveMessage(Player player, Position position) {
        Message msg = new Message(MessageType.MOVE_PLAYER, room.getRoomId(), player.getName())
                .addExtraData("position", position);
        broadcast(msg);
    }

    public void sendShoreUpMessage(Player player, Position position) {
        Message msg = new Message(MessageType.SHORE_UP, room.getRoomId(), player.getName())
                .addExtraData("position", position);
        broadcast(msg);
    }

    // Method for sending complex action messages
    public void sendMoveByNavigatorMessage(Player navigator, Player target, Tile tile) {
        Message msg = new Message(MessageType.MOVE_PLAYER_BY_NAVIGATOR,
                room.getRoomId(), navigator.getName())
                .addExtraData("target", target.getName())
                .addExtraData("position", tile.getPosition());
        broadcast(msg);
    }

    public void sendGiveCardMessage(Player from, Player to, int cardIndex) {
        String cardId = from.getCards().get(cardIndex).getId();
        Message msg = new Message(MessageType.GIVE_CARD, room.getRoomId(), from.getName())
                .addExtraData("to", to.getName())
                .addExtraData("cardId", cardId);
        broadcast(msg);
    }

    public void sendCaptureTreasureMessage(Player player, List<Integer> cardIndices) {
        List<String> cardIds = cardIndices.stream()
                .map(i -> player.getCards().get(i).getId())
                .collect(Collectors.toList());

        Message msg = new Message(MessageType.CAPTURE_TREASURE,
                room.getRoomId(), player.getName())
                .addExtraData("cards", cardIds);
        broadcast(msg);
    }

    public void sendEndTurnMessage(Player player) {
        Message msg = new Message(MessageType.END_TURN, room.getRoomId(), player.getName());
        broadcast(msg);
    }

    public void sendHelicopterMoveMessage(List<Player> players, Player user, Position position, int cardIndex) {
        List<String> playerNames = players.stream()
                .map(Player::getName)
                .collect(Collectors.toList());

        Message msg = new Message(MessageType.HELICOPTER_MOVE,
                room.getRoomId(), user.getName())
                .addExtraData("players", playerNames)
                .addExtraData("position", position)
                .addExtraData("cardIndex", cardIndex);
        broadcast(msg);
    }

    public void sendSandbagsMessage(Player user, Position position, int cardIndex) {
        Message msg = new Message(MessageType.SANDBAGS_USE,
                room.getRoomId(), user.getName())
                .addExtraData("position", position)
                .addExtraData("cardIndex", cardIndex);
        broadcast(msg);
    }

    public void sendStartTurnMessage(Player player) {
        Message msg = new Message(MessageType.TURN_START,
                room.getRoomId(), "system")
                .addExtraData("username", player.getName());
        broadcast(msg);
    }

    public void sendDiscardMessage(Player player, int cardIndex) {
        String cardId = player.getCards().get(cardIndex).getId();
        Message msg = new Message(MessageType.DISCARD_CARD,
                room.getRoomId(), player.getName())
                .addExtraData("cardId", cardId);
        broadcast(msg);
    }

    public void sendGameStartMessage(Message message) {
        String username = (String) message.getData().get("username");
        Message msg = new Message(MessageType.GAME_START,
                room.getRoomId(), "system")
                .addExtraData("players", room.getPlayerByName(username))
                .addExtraData("initialMap", gameController.updateBoard());
        broadcast(msg);
    }

    public void sendAckMessage(Message message) {
        Message ack = new Message(MessageType.MESSAGE_ACK,
                room.getRoomId(), "system")
                .addExtraData("originalId", Message.getMessageId());
        handleGameMessage(ack);
    }

    public MessageHandler getMessageHandler() {
        return this.messageHandler;
    }

    public GameController setGameController(GameController gameController) {
        return gameController;
    }

    public Room getRoom() {
        return this.room = getRoom();
    }

    public GameController getGameController() {
        return this.gameController = getGameController();
    }
}