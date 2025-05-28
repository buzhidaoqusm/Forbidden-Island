package com.island.network;

import com.island.controller.GameController;
import com.island.model.Player;
import com.island.model.Position;
import com.island.model.Room;
import com.island.model.Tile;
import com.island.view.ActionLogView;

import java.net.SocketException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RoomController {
    private GameController gameController;
    private Room room;
    private MessageHandler messageHandler;
    private BroadcastSender sender;
    private BroadcastReceiver receiver;
    private Map<String, Long> playerHeartbeat = new HashMap<>();
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
        // 开启监听线程
        Executors.newSingleThreadExecutor().submit(receiver);
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

    public void removeHeartbeat(String username) {
        playerHeartbeat.remove(username);
    }

    public void handlePlayerDisconnect(String username) {
        Player player = room.getPlayerByName(username);
        if (player != null) {
            room.removePlayer(player);
            broadcastAction(MessageType.PLAYER_LEAVE, player, Map.of("status", "disconnected"));
        }
    }

    public void updatePlayerHeartbeat(String username) {
        playerHeartbeat.put(username, System.currentTimeMillis());
    }

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

    public void handleJoinRequest(Message message) {
        Player username = (Player) message.getData().get("username");
        if (room.addPlayer(username)) {
            sendJoinResponse(username, true);
            broadcastAction(MessageType.PLAYER_JOIN, username, Map.of("status", "joined"));
        } else {
            sendJoinResponse(username, false);
        }
    }

    public void handleGameMessage(Message message) {
        messageHandler.handleMessage(message);
    }

    public void sendJoinResponse(Player username, boolean success) {
        Message msg = new Message(success ? MessageType.PLAYER_JOIN : MessageType.LEAVE_ROOM,
                room.getRoomId(), "system")
                .addExtraData("username", username)
                .addExtraData("status", success);
        handleGameMessage(msg);
    }

    public void broadcastAction(MessageType type, Player player, Map<String, Object> data) {
        Message msg = new Message(type, room.getRoomId(), player.getName());
        data.forEach(msg::addExtraData);
        broadcast(msg);
    }

    public void broadcastAction(MessageType type, String sender, Map<String, Object> data) {
        Message msg = new Message(type, room.getRoomId(), sender);
        data.forEach(msg::addExtraData);
        broadcast(msg);
    }

    public void broadcastAction(MessageType type, Player player) {
        broadcastAction(type, player, new HashMap<>());
    }

    public void sendMoveMessage(Player player, Position position) {
        broadcastAction(MessageType.MOVE_PLAYER, player, Map.of("position", position));
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

    public void sendGameOverMessage(String description) {
        broadcastAction(MessageType.GAME_OVER, "system", Map.of("description", description));
    }

    public void sendDiscardMessage(Player player, int cardIndex) {
        broadcastAction(MessageType.DISCARD_CARD, player, Map.of("cardIndex", cardIndex));
    }

    public void sendStartTurnMessage(Player player) {
        broadcastAction(MessageType.TURN_START, player);
    }

    public void sendDrawFloodMessage(int count, String name) {
        broadcastAction(MessageType.DRAW_FLOOD_CARD, name, Map.of("count", count));
    }

    public void sendMoveByNavigatorMessage(Player navigator, Player target, Tile tile) {
        broadcastAction(MessageType.MOVE_PLAYER_BY_NAVIGATOR, navigator, Map.of(
                "target", target,
                "tile", tile
        ));
    }

    public void sendDrawTreasureCardsMessage(int count, Player player) {
        broadcastAction(MessageType.DRAW_TREASURE_CARD, player, Map.of("count", count));
    }

    public void sendShoreUpMessage(Player player, Position position) {
        broadcastAction(MessageType.SHORE_UP, player, Map.of("position", position));
    }

    public void sendGiveCardMessage(Player from, Player to, int cardIndex) {
        broadcastAction(MessageType.GIVE_CARD, from, Map.of(
                "to", to,
                "cardIndex", cardIndex
        ));
    }

    public void sendCaptureTreasureMessage(Player player, List<Integer> cardIndices) {
        broadcastAction(MessageType.CAPTURE_TREASURE, player, Map.of("cardIndices", cardIndices));
    }

    public void sendEndTurnMessage(Player player) {
        broadcastAction(MessageType.END_TURN, player);
    }

    public MessageHandler getMessageHandler() {
        return this.messageHandler;
    }

    public GameController getGameController() {
        return this.gameController;
    }

    /**
     * Sets the game controller for this room controller
     * @param gameController The game controller to set
     */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    /**
     * Gets the room managed by this controller
     * @return The room instance
     */
    public Room getRoom() {
        return this.room;
    }

    /**
     * Sets the room for this controller
     * @param room The room instance to set
     */
    public void setRoom(Room room) {
        this.room = room;
    }

//    public void startBroadcast(String roomId) {
//        scheduler.scheduleAtFixedRate(() -> {
//            Message msg = new Message(MessageType.ROOM_ADVERTISEMENT)
//                    .setRoomId(roomId)
//                    .setData("difficulty", currentDifficulty);
//            broadcast(msg); // 通过BroadcastSender周期性发送
//        }, 0, 5, TimeUnit.SECONDS);
//    }
//
//    public void joinRoom(String roomId) {
//        // 验证房间存在性（通过已接收的广播消息）
//        if (knownRooms.containsKey(roomId)) {
//            Message joinMsg = new Message(MessageType.JOIN_REQUEST)
//                    .setPlayer(currentPlayer)
//                    .setRoomId(roomId);
//            broadcast(joinMsg); // 广播加入请求
//        }
//    }
}