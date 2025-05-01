package com.island.network;

import com.island.controller.GameController;

import java.util.*;
import java.util.concurrent.*;

public class MessageHandler {
    // 字段声明
    private Map<String, UnconfirmedMessage> unconfirmedMessages;
    private GameController gameController;
    private RoomController roomController;
    private Room room;
    private ActionLogView actionLogView;
    private PriorityQueue<Long> messageQueue;
    private final Object queueLock = new Object();
    private boolean isProcessingQueue;

    //-------------------------
    // 构造函数（依赖注入）
    //-------------------------
    public MessageHandler(GameController gameController,
                          RoomController roomController,
                          Room room,
                          ActionLogView actionLogView) {
        this.gameController = gameController;
        this.roomController = roomController;
        this.room = room;
        this.actionLogView = actionLogView;
        this.unconfirmedMessages = new ConcurrentHashMap<>();
        this.messageQueue = new PriorityQueue<>();
    }

    //-------------------------
    // 公有方法
    //-------------------------
    public static void handleMessage(Message message) {
        switch (message.getType()) {
            case LEAVE_ROOM:
                handleLeaveRoom(message);
                break;
            case PLAYER_LEAVE:
                handlePlayerLeave(message);
                break;
            case GAME_OVER:
                handleGameOver(message);
                break;
            case DISCARD_CARD:
                handleDiscardCard(message);
                break;
            case DRAW_FLOOD_CARD:
                handleDrawFloodCard(message);
                break;
            case SANDBAGS_USE:
                handleUseSandbags(message);
                break;
            case HELICOPTER_MOVE:
                handleMoveByHelicopter(message);
                break;
            case END_TURN:
                handleEndTurn(message);
                break;
            case CAPTURE_TREASURE:
                handleCaptureTreasure(message);
                break;
            case MOVE_PLAYER_BY_NAVIGATOR:
                handleMoveByNavigator(message);
                break;
            case GIVE_CARD:
                handleGiveCard(message);
                break;
            case SHORE_UP:
                handleShoreUp(message);
                break;
            case MOVE_PLAYER:
                handlePlayerMove(message);
                break;
            case TURN_START:
                handleTurnStart(message);
                break;
            case DRAW_TREASURE_CARD:
                handleDrawTreasureCard(message);
                break;
            case GAME_START:
                handleGameStart(message);
                break;
            case UPDATE_ROOM:
                handleUpdateRoom(message);
                break;
            default:
                actionLogView.log("未知消息类型: " + message.getType());
        }
    }

    public void handleMessageAck(Message message) {
        String messageId = String.valueOf(message.getMessageId());
        if (unconfirmedMessages.containsKey(messageId)) {
            unconfirmedMessages.remove(messageId);
            actionLogView.log("消息确认成功: " + messageId);
        }
    }

    public void scheduleMessageRetry(long messageId) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> processMessageRetry(messageId), 5, TimeUnit.SECONDS);
    }

    public void putUnconfirmedMessage(long messageId, UnconfirmedMessage unconfirmedMessage) {
        unconfirmedMessages.put(String.valueOf(messageId), unconfirmedMessage);
    }

    //-------------------------
    // 私有方法（消息队列处理）
    //-------------------------
    private void processMessageQueue() {
        synchronized (queueLock) {
            if (isProcessingQueue) return;
            isProcessingQueue = true;
            try {
                while (!messageQueue.isEmpty()) {
                    long messageId = messageQueue.poll();
                    processMessageRetry(messageId);
                }
            } finally {
                isProcessingQueue = false;
            }
        }
    }

    private void processMessageRetry(long messageId) {
        String key = String.valueOf(messageId);
        UnconfirmedMessage msg = unconfirmedMessages.get(key);
        if (msg != null && msg.getRetryCount() < 3) {
            roomController.broadcast(msg.getMessage());
            msg.incrementRetryCount();
            scheduleMessageRetry(messageId);
        } else {
            unconfirmedMessages.remove(key);
            actionLogView.log("消息重试失败: " + key);
        }
    }

    //-------------------------
    // 私有方法（具体消息处理）
    //-------------------------
    private void handleLeaveRoom(Message message) {
        // TODO: 实现离开房间逻辑
        String username = (String) message.getData().get("username");
        roomController.handlePlayerDisconnect(username);
    }

    private void handlePlayerLeave(Message message) {
        // TODO: 处理玩家主动离开
        String username = (String) message.getData().get("username");
        room.removePlayer(username);
    }

    private void handleGameOver(Message message) {
        // TODO: 处理游戏结束
        String description = (String) message.getData().get("description");
        gameController.endGame(description);
    }

    // ... 其他 handleXXX 方法按相同模式实现 ...
}