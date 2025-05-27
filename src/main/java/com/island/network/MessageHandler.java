package com.island.network;

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
    private final PriorityQueue<Long> messageQueue;
    private final Object queueLock = new Object();
    private volatile boolean isProcessingQueue = false;

    public MessageHandler(GameController gameController,
                          RoomController roomController,
                          Room room,
                          ActionLogView actionLogView) {
        this.gameController = Objects.requireNonNull(gameController);
        this.roomController = Objects.requireNonNull(roomController);
        this.room = Objects.requireNonNull(room);
        this.actionLogView = Objects.requireNonNull(actionLogView);
        this.unconfirmedMessages = new ConcurrentHashMap<>();
        this.messageQueue = new PriorityQueue<>();
    }

    public void handleMessage(Message message) {
        try {
            switch (message.getType()) {
                case LEAVE_ROOM -> roomController.handlePlayerDisconnect((String) message.getData().get("username"));
                case PLAYER_LEAVE -> room.removePlayer(room.getHostPlayer());
                case TURN_START -> handleTurnStart(message);
                case END_TURN -> handleSimple(() -> roomController.sendEndTurnMessage((Player) message.getData().get("username")), message);
                case SHORE_UP -> handleSimple(() -> roomController.sendShoreUpMessage((Player) message.getData().get("username"), (Position) message.getData().get("position")), message);
                case DISCARD_CARD -> handleSimple(gameController::handleDiscardAction, message);
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
                default -> actionLogView.log("Unknown message type: " + message.getType());
            }
        } catch (Exception e) {
            actionLogView.log("Message processing error: " + e.getMessage());
        }
    }

    private void handleDrawFloodCard(Message message) {
        int count = (int) message.getData().get("count");
        try {
            List<Position> cards = gameController.drawFloodCards(count);
            roomController.sendDrawFloodMessage(count, gameController.getCurrentPlayer().getName());
        } catch (Exception ex) {
            handleGameOver(new Message(MessageType.GAME_OVER, "system", "Flood pile exhausted"));
        }
    }

    private void handleSimple(Runnable action, Message message) {
        try {
            action.run();
        } catch (Exception ex) {
            actionLogView.log("Action failed: " + ex.getMessage());
            scheduleMessageRetry(Message.getMessageId());
        }
    }

    public void handleMessageAck(Message message) {
        String messageId = String.valueOf(message.getMessageId());
        if (unconfirmedMessages.remove(messageId) != null) {
            actionLogView.log("Ack received for: " + messageId);
        }
    }

    public void putUnconfirmedMessage(long messageId, UnconfirmedMessage unconfirmedMessage) {
        String key = String.valueOf(messageId);
        unconfirmedMessages.put(key, unconfirmedMessage);
        messageQueue.add(messageId);
        processMessageQueue();
    }

    private void processMessageQueue() {
        synchronized (queueLock) {
            if (isProcessingQueue) return;
            isProcessingQueue = true;
            try {
                while (!messageQueue.isEmpty()) {
                    long id = messageQueue.poll();
                    UnconfirmedMessage msg = unconfirmedMessages.get(String.valueOf(id));
                    if (msg != null && msg.getRetryCount() < 3) {
                        roomController.broadcast(msg.getMessage());
                        msg.incrementRetryCount();
                    } else {
                        unconfirmedMessages.remove(String.valueOf(id));
                    }
                }
            } finally {
                isProcessingQueue = false;
            }
        }
    }

    private void scheduleMessageRetry(long messageId) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> processMessageRetry(messageId), 5, TimeUnit.SECONDS);
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
        actionLogView.log("Game started.");
    }

    private void handleUpdateRoom(Message message) {
        actionLogView.log("Room info updated.");
        // 可选：room.updateFrom(message.getData());
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
            actionLogView.log("Message retry failed for: " + key);
        }
    }

}