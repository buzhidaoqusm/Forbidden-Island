package com.island.network;

import com.island.controller.GameController;
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
    // 可变组件
    private GameController gameController;
    private Room room;
    private MessageHandler messageHandler;
    private BroadcastSender sender;
    private BroadcastReceiver receiver;
    
    // 不可变组件
    private final Map<String, Long> playerHeartbeats;
    private final ScheduledExecutorService scheduler;
    private final ActionLogView logView;
    private final AtomicBoolean isRunning;
    private final Map<String, Integer> reconnectAttempts;
    
    private static final int DEFAULT_PORT = 8888;
    private static final long HEARTBEAT_INTERVAL_SECONDS = 10;
    private static final long HEARTBEAT_CHECK_INTERVAL_SECONDS = 15;
    private static final long PLAYER_TIMEOUT_MS = 30000;
    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    private static final int RECONNECT_DELAY_MS = 5000;

    public RoomController(GameController gameController, Room room, ActionLogView logView) throws SocketException {
        this.gameController = Objects.requireNonNull(gameController, "GameController cannot be null");
        this.room = Objects.requireNonNull(room, "Room cannot be null");
        this.logView = Objects.requireNonNull(logView, "ActionLogView cannot be null");
        
        this.playerHeartbeats = new ConcurrentHashMap<>();
        this.reconnectAttempts = new ConcurrentHashMap<>();
        this.isRunning = new AtomicBoolean(true);
        
        // 初始化调度器
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("RoomController-Scheduler");
            return t;
        });

        try {
            initializeComponents();
        } catch (Exception e) {
            shutdown();
            throw new SocketException("Failed to initialize room controller: " + e.getMessage());
        }
    }

    private void initializeComponents() throws SocketException {
        try {
            // 初始化消息处理器
            this.messageHandler = new MessageHandler(gameController, this, room, logView);
            
            // 初始化网络组件
            Set<String> broadcastAddresses = BroadcastAddressCalculator.getBroadcastAddresses();
            this.sender = new BroadcastSender(broadcastAddresses, DEFAULT_PORT, logView);
            this.receiver = new BroadcastReceiver(this, DEFAULT_PORT);
            
            logView.success("Room controller components initialized successfully");
        } catch (BroadcastAddressCalculator.NetworkInterfaceException e) {
            logView.error("Failed to initialize network components: " + e.getMessage());
            throw new SocketException("Network initialization failed: " + e.getMessage());
        } catch (Exception e) {
            logView.error("Unexpected error during initialization: " + e.getMessage());
            throw new SocketException("Initialization failed: " + e.getMessage());
        }
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
        if (!isRunning.get()) {
            logView.warning("Attempting to start an already stopped room controller");
            return;
        }

        // 启动心跳检测
        scheduler.scheduleAtFixedRate(
            () -> {
                try {
                    broadcastHeartbeat();
                } catch (Exception e) {
                    logView.error("Heartbeat broadcast failed: " + e.getMessage());
                }
            },
            0,
            HEARTBEAT_INTERVAL_SECONDS,
            TimeUnit.SECONDS
        );
        
        scheduler.scheduleAtFixedRate(
            () -> {
                try {
                    checkHeartbeats();
                } catch (Exception e) {
                    logView.error("Heartbeat check failed: " + e.getMessage());
                }
            },
            0,
            HEARTBEAT_CHECK_INTERVAL_SECONDS,
            TimeUnit.SECONDS
        );

        // 启动接收器
        CompletableFuture.runAsync(() -> {
            try {
                receiver.run();
            } catch (Exception e) {
                logView.error("Receiver failed: " + e.getMessage());
                handleReceiverFailure();
            }
        });

        logView.success("Room controller started successfully");
    }

    private void handleReceiverFailure() {
        if (isRunning.get()) {
            logView.warning("Attempting to restart receiver...");
            try {
                Thread.sleep(RECONNECT_DELAY_MS);
                receiver = new BroadcastReceiver(this, DEFAULT_PORT);
                CompletableFuture.runAsync(() -> {
                    try {
                        receiver.run();
                    } catch (Exception e) {
                        logView.error("Receiver restart failed: " + e.getMessage());
                        handleReceiverFailure();
                    }
                });
            } catch (Exception e) {
                logView.error("Failed to restart receiver: " + e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        shutdown();
    }

    public void shutdown() {
        if (isRunning.compareAndSet(true, false)) {
            logView.log("正在关闭房间控制器...");
            
            // 1. 发送离开房间消息
            try {
                if (room != null && room.getCurrentProgramPlayer() != null) {
                    Message leaveMsg = new Message(MessageType.LEAVE_ROOM, room.getRoomId(), room.getCurrentProgramPlayer().getName());
                    broadcast(leaveMsg);
                }
            } catch (Exception e) {
                logView.error("发送离开消息失败: " + e.getMessage());
            }
            
            // 2. 关闭调度器
            if (scheduler != null) {
        scheduler.shutdownNow();
                try {
                    if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        logView.warning("调度器未能在预期时间内关闭");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logView.error("调度器关闭被中断");
                }
            }
            
            // 3. 关闭网络组件
            if (receiver != null) {
        receiver.stop();
            }
            if (sender != null) {
        sender.close();
            }
            if (messageHandler != null) {
                messageHandler.shutdown();
            }
            
            // 4. 清理资源
            playerHeartbeats.clear();
            reconnectAttempts.clear();
            
            // 5. 重置房间状态
            if (room != null) {
                room.clear(); // 需要在Room类中添加clear方法
            }
            room = null;
            
            // 6. 重置控制器状态
            gameController = null;
            
            logView.success("房间控制器已完全关闭");
        }
    }

    private void broadcastHeartbeat() {
        if (!isRunning.get()) return;
        
        try {
        Message msg = new Message(MessageType.MESSAGE_ACK, room.getRoomId(), "system");
        broadcast(msg);
        } catch (Exception e) {
            logView.error("Failed to broadcast heartbeat: " + e.getMessage());
        }
    }

    private void checkHeartbeats() {
        if (!isRunning.get()) return;
        
        long now = System.currentTimeMillis();
        List<String> disconnectedPlayers = new ArrayList<>();

        playerHeartbeats.forEach((username, lastHeartbeat) -> {
            if (now - lastHeartbeat > PLAYER_TIMEOUT_MS) {
                disconnectedPlayers.add(username);
            }
        });

        for (String username : disconnectedPlayers) {
            handlePlayerDisconnect(username);
        }
    }

    public void broadcast(Message message) {
        if (!isRunning.get()) {
            logView.warning("Attempted to broadcast message while room controller is stopped");
            return;
        }
        
        try {
            sender.broadcast(message);
        } catch (BroadcastSender.BroadcastException e) {
            logView.error("Broadcast failed: " + e.getMessage());
            handleBroadcastFailure(message);
        }
    }

    private void handleBroadcastFailure(Message message) {
        if (!isRunning.get()) return;
        
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(RECONNECT_DELAY_MS);
                sender.broadcast(message);
            } catch (Exception e) {
                logView.error("Retry broadcast failed: " + e.getMessage());
            }
        });
    }

    public void handlePlayerDisconnect(String username) {
        if (!isRunning.get()) return;
        
        Player player = room.getPlayerByName(username);
        if (player != null) {
            int attempts = reconnectAttempts.getOrDefault(username, 0);
            if (attempts < MAX_RECONNECT_ATTEMPTS) {
                reconnectAttempts.put(username, attempts + 1);
                logView.warning("Player disconnected: " + username + " (Attempt " + (attempts + 1) + "/" + MAX_RECONNECT_ATTEMPTS + ")");
                handleReconnectAttempt(player);
            } else {
            room.removePlayer(player);
            broadcastAction(MessageType.PLAYER_LEAVE, player, Map.of("status", "disconnected"));
                removeHeartbeat(username);
                reconnectAttempts.remove(username);
                logView.warning("Player permanently disconnected: " + username);
            }
        }
    }

    private void handleReconnectAttempt(Player player) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(RECONNECT_DELAY_MS);
                Message reconnectMsg = new Message(MessageType.RECONNECT_REQUEST, room.getRoomId(), player.getName());
                broadcast(reconnectMsg);
            } catch (Exception e) {
                logView.error("Reconnect attempt failed for " + player.getName() + ": " + e.getMessage());
            }
        });
    }

    public void removeHeartbeat(String username) {
        playerHeartbeats.remove(username);
    }

    public void updatePlayerHeartbeat(String username) {
        if (isRunning.get()) {
            playerHeartbeats.put(username, System.currentTimeMillis());
            reconnectAttempts.remove(username); // Reset reconnect attempts on successful heartbeat
        }
    }

    public void handleJoinRequest(Message message) {
        if (!isRunning.get()) {
            logView.warning("收到加入请求，但房间控制器已停止");
            return;
        }
        
        try {
            // 检查消息有效性
            if (message == null || message.getData() == null) {
                logView.error("无效的加入请求：消息为空");
                throw new IllegalArgumentException("无效的加入请求：消息为空");
            }

            // 获取玩家信息
            Object playerObj = message.getData().get("username");
            if (!(playerObj instanceof Player)) {
                logView.error("无效的加入请求：玩家信息格式错误");
                throw new IllegalArgumentException("无效的加入请求：玩家信息格式错误");
            }

            Player player = (Player) playerObj;
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
            if (room.getPlayerByName(player.getName()) != null) {
                logView.warning("玩家 " + player.getName() + " 已在房间中");
                throw new IllegalStateException("玩家已在房间中");
            }

            // 尝试添加玩家
            boolean joined = room.addPlayer(player);
            logView.log("玩家加入结果: " + (joined ? "成功" : "失败"));
            
            // 发送响应
            sendJoinResponse(player, joined);
            
            if (joined) {
                // 广播玩家加入消息
                broadcastAction(MessageType.PLAYER_JOIN, player, Map.of("status", "joined"));
                updatePlayerHeartbeat(player.getName());
                logView.success("玩家加入成功: " + player.getName());
                
                // 更新房间状态
                broadcastAction(MessageType.UPDATE_ROOM, "system", Map.of(
                    "players", room.getPlayers(),
                    "roomId", room.getRoomId()
                ));
            } else {
                logView.warning("玩家加入请求被拒绝: " + player.getName());
                throw new IllegalStateException("玩家加入请求被拒绝");
            }
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
        if (!isRunning.get()) {
            logView.warning("Received game message while room controller is stopped");
            return;
        }
        
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
        if (!isRunning.get()) return;
        
        Message msg = new Message(type, room.getRoomId(), player.getName());
        data.forEach(msg::addExtraData);
        broadcast(msg);
    }

    public void broadcastAction(MessageType type, String sender, Map<String, Object> data) {
        if (!isRunning.get()) return;
        
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

    public boolean isRunning() {
        return isRunning.get();
    }
}