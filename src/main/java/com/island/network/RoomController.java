package com.island.network;

import com.island.controller.GameController;
import com.island.model.Player;
import com.island.model.Room;
import com.island.model.Position;
import com.island.model.Tile;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RoomController {
    private GameController gameController;
    private Room room;
    private MessageHandler messageHandler;
    private BroadcastSender sender;
    private BroadcastReceiver receiver;
    private Map<String, Long> playerHeartbeat;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    // 构造函数
    public RoomController(Room room) {
        this.room = room;
        // 其他字段可能需要初始化（根据你的具体逻辑）
    }

    //-------------------------
    // 心跳相关方法
    //-------------------------
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
                    currentTime - entry.getValue() > 30000 // 30秒超时
            );
            playerHeartbeat.keySet().forEach(this::handlePlayerDisconnect);
        }, 0, 15, TimeUnit.SECONDS);
    }

    public void removeHeartbeat(String username) {
        playerHeartbeat.remove(username);
    }

    //-------------------------
    // 玩家连接处理
    //-------------------------
    public void handlePlayerDisconnect(String username) {
        room.removePlayer(username);
        broadcast(new Message(MessageType.PLAYER_LEAVE,
                Map.of("username", username)));
        updateRoomStatus();
    }

    public void updatePlayerDisconnect(String username) {
        removeHeartbeat(username);
        room.markPlayerOffline(username);
    }

    public void updatePlayerHeartbeat(String username) {
        playerHeartbeat.put(username, System.currentTimeMillis());
    }

    //-------------------------
    // 消息广播基础方法
    //-------------------------
    public void broadcast(Message message) {
        Set<String> addresses = BroadcastAddressCalculator.getBroadcastAddresses();
        addresses.forEach(addr -> {
            try (BroadcastSender sender = new BroadcastSender(addr, 8888)) {
                sender.broadcast(message);
            } catch (Exception e) {
                System.err.println("广播到 " + addr + " 失败: " + e.getMessage());
            }
        });
    }

    //-------------------------
    // 消息处理相关方法
    //-------------------------
    public void handleJoinRequest(Message message) {
        String username = (String) message.getData().get("username");
        if (room.addPlayer(username)) {
            sendJoinResponse(username, true);
            broadcast(new Message(MessageType.PLAYER_JOIN,
                    Map.of("username", username)));
        } else {
            sendJoinResponse(username, false);
        }
    }

    public void handleGameMessage(Message message) {
        messageHandler.handleMessage(message);
    }

    //-------------------------
    // 游戏状态消息发送方法
    //-------------------------
    public void sendGameOverMessage(String description) {
        broadcast(new Message(MessageType.GAME_OVER,
                Map.of("description", description)));
    }

    public void sendUpdateRoomMessage() {
        broadcast(new Message(MessageType.UPDATE_ROOM,
                Map.of(
                        "players", room.getPlayers().stream().map(Player::getName).collect(Collectors.toList()),
                        "status", room.getStatus()
                )));
    }

    //-------------------------
    // 玩家动作消息发送方法
    //-------------------------
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

    public void sendJoinResponse(String username, boolean b) {
        Message msg = new Message(success ? MessageType.PLAYER_JOIN : MessageType.LEAVE_ROOM,
                room.getRoomId(), "system")
                .addExtraData("username", username)
                .addExtraData("status", success);
        sendPrivateMessage(username, msg);
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

    //-------------------------
    // 复杂动作消息发送方法
    //-------------------------
    public void sendMoveByNavigatorMessage(Player player, Player target, Tile tile) {
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

    public void sendGameStartMessage() {
        Message msg = new Message(MessageType.GAME_START,
                room.getRoomId(), "system")
                .addExtraData("players", room.getPlayerNames())
                .addExtraData("initialMap", gameController.getCurrentMapState());
        broadcast(msg);
    }

    public void sendAckMessage(Message message) {
        Message ack = new Message(MessageType.MESSAGE_ACK,
                room.getRoomId(), "system")
                .addExtraData("originalId", originalMessage.getMessageId());
        sendPrivateMessage(originalMessage.getFrom(), ack);
    }

    public MessageHandler getMessageHandler() {
        return this.messageHandler;
    }
}