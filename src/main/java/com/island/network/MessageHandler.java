package com.island.network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.island.controller.GameController;
import com.island.model.*;
import com.island.view.ActionLogView;

import java.util.*;
import java.util.concurrent.*;

public class MessageHandler {
    private final Map<String, UnconfirmedMessage> unconfirmedMessages;
    private final GameController gameController;
    private final RoomController roomController;
    private final Room room;
    private final ActionLogView actionLogView;
    private final PriorityBlockingQueue<Long> messageQueue;
    private final ScheduledExecutorService retryScheduler;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 5000;
    private final Object queueLock = new Object();
    private volatile boolean isProcessingQueue = false;

    public MessageHandler(GameController gameController,
                          RoomController roomController,
                          Room room,
                          ActionLogView actionLogView) {
        this.gameController = Objects.requireNonNull(gameController, "GameController cannot be null");
        this.roomController = Objects.requireNonNull(roomController, "RoomController cannot be null");
        this.room = Objects.requireNonNull(room, "Room cannot be null");
        this.actionLogView = Objects.requireNonNull(actionLogView, "ActionLogView cannot be null");
        this.unconfirmedMessages = new ConcurrentHashMap<>();
        this.messageQueue = new PriorityBlockingQueue<>();
        this.retryScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "MessageRetryThread");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void handleMessage(Message message) {
        try {
            if (message == null) {
                actionLogView.error("Received null message");
                return;
            }

            actionLogView.log("Processing message: " + message.getType());
            
            switch (message.getType()) {
                case LEAVE_ROOM -> handleLeaveRoom(message);
                case PLAYER_LEAVE -> handlePlayerLeave(message);
                case TURN_START -> handleTurnStart(message);
                case END_TURN -> handleEndTurn(message);
                case SHORE_UP -> handleShoreUp(message);
                case DISCARD_CARD -> handleDiscardCard(message);
                case DRAW_FLOOD_CARD -> handleDrawFloodCard(message);
                case DRAW_TREASURE_CARD -> handleDrawTreasureCard(message);
                case MOVE_PLAYER -> handleMovePlayer(message);
                case MOVE_PLAYER_BY_NAVIGATOR -> handleMoveByNavigator(message);
                case GIVE_CARD -> handleGiveCard(message);
                case CAPTURE_TREASURE -> handleCaptureTreasure(message);
                case SANDBAGS_USE -> handleUseSandbags(message);
                case HELICOPTER_MOVE -> handleMoveByHelicopter(message);
                case GAME_OVER -> handleGameOver(message);
                case GAME_START -> handleGameStart(message);
                case UPDATE_ROOM -> handleUpdateRoom(message);
                case MESSAGE_ACK -> handleMessageAck(message);
                case PLAYER_JOIN -> handlePlayerJoin(message);
                default -> actionLogView.warning("Unknown message type: " + message.getType());
            }
        } catch (Exception e) {
            actionLogView.error("Message processing error: " + e.getMessage());
            scheduleMessageRetry(Message.getMessageId());
        }
    }
    private void handlePlayerJoin(Message message) {
        try {
            gameController.handlePlayerJoin(message);
        } catch (Exception e) {
            actionLogView.error("Failed to handle player join: " + e.getMessage());
            scheduleMessageRetry(Message.getMessageId());
        }

    }

    private void handleLeaveRoom(Message message) {
        String username = (String) message.getData().get("username");
        roomController.handlePlayerDisconnect(username);
    }

    private void handlePlayerLeave(Message message) {
        room.removePlayer(room.getHostPlayer());
    }

    private void handleEndTurn(Message message) {
        handleSimple(() -> 
            roomController.sendEndTurnMessage((Player) message.getData().get("username")), 
            message
        );
    }

    private void handleShoreUp(Message message) {
        handleSimple(() -> 
            roomController.sendShoreUpMessage(
                (Player) message.getData().get("username"),
                (Position) message.getData().get("position")
            ), 
            message
        );
    }

    private void handleDiscardCard(Message message) {
        handleSimple(gameController::handleDiscardAction, message);
    }

    private void handleDrawFloodCard(Message message) {
        try {
            int count = (int) message.getData().get("count");
            List<Position> cards = gameController.drawFloodCards(count);
            roomController.sendDrawFloodMessage(count, gameController.getCurrentPlayer().getName());
        } catch (Exception ex) {
            actionLogView.error("Failed to draw flood cards: " + ex.getMessage());
            handleGameOver(createGameOverMessage("Flood pile exhausted"));
        }
    }

    private Message createGameOverMessage(String reason) {
        return new Message(MessageType.GAME_OVER, room.getRoomId(), "system")
            .addExtraData("reason", reason);
    }

    private void handleSimple(Runnable action, Message message) {
        try {
            action.run();
        } catch (Exception ex) {
            actionLogView.error("Action failed: " + ex.getMessage());
            scheduleMessageRetry(Message.getMessageId());
        }
    }

    public void handleMessageAck(Message message) {
        String messageId = String.valueOf(message.getMessageId());
        UnconfirmedMessage removed = unconfirmedMessages.remove(messageId);
        if (removed != null) {
            actionLogView.success("Acknowledgment received for message: " + messageId);
        }
    }

    public void putUnconfirmedMessage(long messageId, UnconfirmedMessage unconfirmedMessage) {
        String key = String.valueOf(messageId);
        unconfirmedMessages.put(key, unconfirmedMessage);
        messageQueue.offer(messageId);
        processMessageQueue();
    }

    private void processMessageQueue() {
        synchronized (queueLock) {
            if (isProcessingQueue) return;
            isProcessingQueue = true;
            
            try {
                Long messageId;
                while ((messageId = messageQueue.poll()) != null) {
                    String key = String.valueOf(messageId);
                    UnconfirmedMessage msg = unconfirmedMessages.get(key);
                    
                    if (msg != null && msg.canRetry()) {
                        roomController.broadcast(msg.getMessage());
                        msg.incrementRetryCount();
                        if (msg.getRetryCount() < MAX_RETRY_ATTEMPTS) {
                            scheduleMessageRetry(messageId);
                        }
                    } else {
                        unconfirmedMessages.remove(key);
                        if (msg != null) {
                            actionLogView.error("Message " + key + " exceeded retry limit");
                        }
                    }
                }
            } finally {
                isProcessingQueue = false;
            }
        }
    }

    private void scheduleMessageRetry(long messageId) {
        retryScheduler.schedule(
            () -> processMessageRetry(messageId),
            RETRY_DELAY_MS,
            TimeUnit.MILLISECONDS
        );
    }

    private void processMessageRetry(long messageId) {
        String key = String.valueOf(messageId);
        UnconfirmedMessage msg = unconfirmedMessages.get(key);
        
        if (msg != null && msg.canRetry()) {
            roomController.broadcast(msg.getMessage());
            if (msg.incrementRetryCount() && msg.getRetryCount() < MAX_RETRY_ATTEMPTS) {
                scheduleMessageRetry(messageId);
            }
        } else {
            unconfirmedMessages.remove(key);
            actionLogView.warning("Message retry failed for: " + key);
        }
    }

    private void handleTurnStart(Message message) {
        String username = (String) message.getData().get("username");
        actionLogView.log("Turn started for: " + username);
    }

    private void handleDrawTreasureCard(Message message) {
        int count = (int) message.getData().get("count");
        Player player = (Player) message.getData().get("player");
        gameController.handleDrawTreasureCard(count, player);
    }

    private void handleMovePlayer(Message message) {
        String username = (String) message.getData().get("username");
        Position to = (Position) message.getData().get("to");
        gameController.getPlayerController().getRoom().getPlayers().stream()
            .filter(p -> p.getName().equals(username))
            .findFirst()
            .ifPresent(player -> player.setPosition(to));
        gameController.getGameView().getPlayerView().update();
        actionLogView.log(username + " moved to " + to);
    }

    private void handleMoveByNavigator(Message message) {
        String target = (String) message.getData().get("target");
        Position to = (Position) message.getData().get("to");
        actionLogView.log("Navigator moved " + target + " to " + to);
    }

    private void handleGiveCard(Message message) {
        String from = (String) message.getData().get("from");
        String to = (String) message.getData().get("to");
        String cardName = (String) message.getData().get("card");
        actionLogView.log(from + " gave card " + cardName + " to " + to);
    }

    private void handleCaptureTreasure(Message message) {
        String player = (String) message.getData().get("player");
        String treasure = (String) message.getData().get("treasure");
        actionLogView.log(player + " captured treasure: " + treasure);
    }

    private void handleUseSandbags(Message message) {
        String user = (String) message.getData().get("username");
        Position pos = (Position) message.getData().get("position");
        actionLogView.log(user + " used Sandbags at: " + pos);
    }

    private void handleMoveByHelicopter(Message message) {
        String user = (String) message.getData().get("username");
        List<Position> path = (List<Position>) message.getData().get("path");
        actionLogView.log(user + " used helicopter to: " + path);
    }

    private void handleGameOver(Message message) {
        String reason = (String) message.getData().get("reason");
        actionLogView.log("Game over: " + reason);
    }

    private void handleGameStart(Message message) {
        try {
            // 1. 获取玩家信息
            Map<String, Object> data = message.getData();
            if (data == null || !data.containsKey("players")) {
                throw new IllegalArgumentException("Invalid game start message: missing player data");
            }

            // 2. 更新房间状态
            Player startingPlayer = (Player) data.get("players");
            if (startingPlayer != null) {
                room.setHostPlayer(startingPlayer);
                startingPlayer.setHost(true);
            }

            // 3. 启动游戏
            if (!gameController.isGameStart()) {
                gameController.startGame(System.currentTimeMillis());
            }

            // 4. 记录日志
            actionLogView.success("Game started successfully");
        } catch (Exception e) {
            actionLogView.error("Failed to handle game start: " + e.getMessage());
            throw new RuntimeException("Game start failed", e);
        }
    }

    private void handleUpdateRoom(Message message) {
        actionLogView.log("Room info updated.");
        // 可选：room.updateFrom(message.getData());
        // 获取message.getDate()中的players并反序列化成List<Player>
        Object raw = message.getData().get("players");
        List<Player> deserializedPlayers = Message.getMapper()
                .convertValue(raw, new TypeReference<List<Player>>() {});
        // 更新room
        room.setPlayers(deserializedPlayers);

    }

    public void shutdown() {
        retryScheduler.shutdownNow();
        try {
            if (!retryScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                actionLogView.warning("Retry scheduler did not terminate in time");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            actionLogView.error("Retry scheduler shutdown interrupted");
        }
    }
}