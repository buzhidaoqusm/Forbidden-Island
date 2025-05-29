package com.island.network;

import com.island.controller.GameController;
import com.island.launcher.Launcher;
import com.island.model.Player;
import com.island.model.Position;
import com.island.model.Room;
import com.island.model.Tile;
import com.island.view.ActionLogView;

import java.net.SocketException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RoomController implements AutoCloseable {
    private GameController gameController;
    private Room room;
    private MessageHandler messageHandler;
    private BroadcastSender sender;
    private BroadcastReceiver receiver;
    private Map<String, Long> playerHeartbeat = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final ActionLogView logView;

    public RoomController(GameController gameController, Room room, ActionLogView logView) throws SocketException {
        this.gameController = Objects.requireNonNull(gameController, "GameController not empty");
        this.room = Objects.requireNonNull(room, "Room not empty");
        this.messageHandler = new MessageHandler(gameController, this, room, new ActionLogView());
        this.logView = Objects.requireNonNull(logView, "ActionLogView cannot be null");
        try {
            this.sender = new BroadcastSender("255.255.255.255", 8888);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        try {
            this.receiver = new BroadcastReceiver(this, Launcher.networkConfig.getListenPort());
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        start();
    }


    /**
     * 设置游戏控制器
     * @param gameController 游戏控制器实例
     */
    public void setGameController(GameController gameController) {
        this.gameController = Objects.requireNonNull(gameController, "GameController cannot be null");
        if (this.messageHandler != null) {
            // 重新初始化消息处理器以更新GameController引用
            this.messageHandler = new MessageHandler(gameController, this, room, logView);
        }
    }

    /**
     * 设置房间
     * @param room 房间实例
     */
    public void setRoom(Room room) {
        this.room = Objects.requireNonNull(room, "Room cannot be null");
        if (this.messageHandler != null) {
            // 重新初始化消息处理器以更新Room引用
            this.messageHandler = new MessageHandler(gameController, this, room, logView);
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


    @Override
    public void close() {
        shutdown();
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

    public void handlePlayerDisconnect(String username) {
        Player player = room.getPlayerByName(username);
        if (player != null) {
            room.removePlayer(player);
            broadcastAction(MessageType.PLAYER_LEAVE, player, Map.of("status", "disconnected"));
        }
    }


    public void removeHeartbeat(String username) {
        playerHeartbeat.remove(username);
    }

    public void updatePlayerHeartbeat(String username) {
        playerHeartbeat.put(username, System.currentTimeMillis());
    }


    public void handleJoinRequest(Message message) {
        
        try {
            // 检查消息有效性
            if (message == null || message.getData() == null) {
                logView.error("无效的加入请求：消息为空");
                throw new IllegalArgumentException("无效的加入请求：消息为空");
            }

            // 获取玩家信息
            Object playerObj = Message.getMapper()
                    .convertValue(message.getData().get("username"), Player.class);
            if (playerObj == null) {
                logView.error("无效的加入请求：玩家信息格式错误");
                throw new IllegalArgumentException("无效的加入请求：玩家信息格式错误");
            }

            Player player = (Player) playerObj;
            if (!room.isHost(player.getName())) {
                logView.log("玩家 " + player.getName() + " 不是房主，不处理加入请求");
            }
            logView.log("处理玩家加入请求: " + player.getName());

            // 检查房间状态
            if (room == null) {
                logView.error("房间对象为空，无法处理加入请求");
                throw new IllegalStateException("房间对象为空，无法处理加入请求");
            }

            // 检查房间是否已满
            if (room.isFull()) {
                logView.warning("房间已满，无法加入更多玩家");
                throw new IllegalStateException("房间已满，无法加入更多玩家");
            }

            // 检查玩家是否已在房间中
//            if (room.getPlayerByName(player.getName()) != null) {
//                logView.warning("玩家 " + player.getName() + " 已在房间中");
//                throw new IllegalStateException("玩家已在房间中");
//            }

            // 尝试添加玩家
            boolean joined = room.addPlayer(player);
            logView.log("玩家加入结果: " + (joined ? "成功" : "失败"));


            // 更新房间状态
            broadcastAction(MessageType.UPDATE_ROOM, "system", Map.of(
                    "players", room.getPlayers(),
                    "roomId", room.getRoomId()
            ));
        } catch (Exception e) {
            logView.error("处理加入请求时发生错误: " + e.getMessage());
            // 将错误信息发送回客户端
            if (message != null && message.getFrom() != null) {
                Message errorResponse = new Message(MessageType.LEAVE_ROOM, 
                    room != null ? room.getRoomId() : "", 
                    "system")
                    .addExtraData("error", e.getMessage());
                broadcast(errorResponse);
            }
            throw new RuntimeException("加入房间失败: " + e.getMessage(), e);
        }
    }

    public void handleGameMessage(Message message) {
        
        try {
        messageHandler.handleMessage(message);
        } catch (Exception e) {
            logView.error("Error handling game message: " + e.getMessage());
        }
    }

    public void sendJoinResponse(Player player, boolean success) {
        Message response = new Message(
            success ? MessageType.PLAYER_JOIN : MessageType.LEAVE_ROOM,
            room.getRoomId(),
            "system"
        )
        .addExtraData("username", player)
                .addExtraData("status", success);
        
        handleGameMessage(response);
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

    // 游戏相关的消息发送方法
    public void sendMoveMessage(Player player, Position position) {
        broadcastAction(MessageType.MOVE_PLAYER, player, Map.of("position", position));
    }

    public void sendGameStartMessage(Message message) {
        try {
            // 1. 获取玩家信息
        String username = (String) message.getData().get("username");
            Player player = room.getPlayerByName(username);
            if (player == null) {
                throw new IllegalStateException("Player not found: " + username);
            }

            // 2. 验证房间状态
            if (room.getPlayers().isEmpty()) {
                throw new IllegalStateException("No players in room");
            }

            // 3. 启动游戏
            System.out.println("Starting game for player: " + username);
            gameController.startGame(System.currentTimeMillis());

            // 4. 广播游戏开始消息
            Message startMsg = new Message(MessageType.GAME_START, room.getRoomId(), "system")
                .addExtraData("players", player)
                .addExtraData("initialMap", gameController.updateBoard());
            broadcast(startMsg);

            // 5. 更新玩家状态
            for (Player p : room.getPlayers()) {
                p.setInGame(true);
            }

            System.out.println("Game start message sent successfully");
        } catch (Exception e) {
            logView.error("Failed to send game start message: " + e.getMessage());
            throw new RuntimeException("Failed to start game", e);
        }
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

    // Getters
    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public GameController getGameController() {
        return gameController;
    }

    public Room getRoom() {
        return room;
    }


}