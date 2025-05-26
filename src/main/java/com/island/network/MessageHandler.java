package com.island.network;

import com.island.controller.GameController;
import com.island.model.*;
import com.island.view.ActionLogView;
import com.island.view.GameView;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class MessageHandler {
    private Map<String, UnconfirmedMessage> unconfirmedMessages;
    private GameController gameController;
    private RoomController roomController;
    private Room room;
    private ActionLogView actionLogView;
    private PriorityQueue<Long> messageQueue = new PriorityQueue<>();
    private final Object queueLock = new Object();
    private volatile boolean isProcessingQueue = false;
    private MessageType messageType;
    private List drawnCards;

    public MessageHandler(GameController gameController,
                          RoomController roomController,
                          Room room,
                          ActionLogView actionLogView) {
        this.gameController = Objects.requireNonNull(gameController, "GameController can not be null");
        this.roomController = Objects.requireNonNull(roomController, "RoomController can not be null");
        this.room = Objects.requireNonNull(room, "Room can not be null");
        
        // 创建ActionLogView实例（如果为null）
        if (actionLogView == null) {
            this.actionLogView = new ActionLogView();
        } else {
            this.actionLogView = actionLogView;
        }
        
        this.unconfirmedMessages = new ConcurrentHashMap<>();
        this.messageQueue = new PriorityQueue<>();
    }

    public void handleMessage(Message message) {
        try {
            MessageType messageType = message.getType();
            switch (messageType) {
                case LEAVE_ROOM:
                    handleLeaveRoom(message);
                    break;
                case PLAYER_LEAVE:
                    handlePlayerLeave();
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
//                    handleUpdateRoom(message);
                    break;
                default:
                    actionLogView.log("Unknown message type: " + messageType);
            }
        } catch (Exception e) {
                actionLogView.log("Message processing exception: " + e.getMessage());
        }
    }

    public void handleMessageAck(Message message) {
        String messageId = String.valueOf(message.getMessageId());
        if (unconfirmedMessages.containsKey(messageId)) {
            unconfirmedMessages.remove(messageId);
            actionLogView.log("Message confirmation successful: " + messageId);
        }
    }

    public void scheduleMessageRetry(long messageId) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> processMessageRetry(messageId), 5, TimeUnit.SECONDS);
    }

    public synchronized void putUnconfirmedMessage(long messageId, UnconfirmedMessage unconfirmedMessage) {
        String key = String.valueOf(messageId);
        unconfirmedMessages.put(key, unconfirmedMessage);
        messageQueue.add(messageId);
        processMessageQueue(); // Trigger queue processing
    }

    private void processMessageQueue() {
        synchronized (queueLock) {
            if (isProcessingQueue) return;
            isProcessingQueue = true;
            try {
                while (!messageQueue.isEmpty()) {
                    long messageId = messageQueue.poll();
                    UnconfirmedMessage msg = unconfirmedMessages.get(String.valueOf(messageId));
                    if (msg != null && msg.getRetryCount() < 3) {
                        roomController.broadcast(msg.getMessage());
                        msg.incrementRetryCount();
                    } else {
                        unconfirmedMessages.remove(String.valueOf(messageId));
                    }
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
            actionLogView.log("Message retry failed: " + key);
        }
    }

    private void handleLeaveRoom(Message message) {
        String username = (String) message.getData().get("username");
        roomController.handlePlayerDisconnect(username);
    }

    private void handlePlayerLeave() {
        room.removePlayer(room.getHostPlayer());
    }

    private void handleGameOver(Message message) {
        String description = (String) message.getData().get("description");
        gameController.setGameOver(true); // Set room status
        roomController.broadcast(new Message(MessageType.GAME_OVER, room.getRoomId(), "system")
                .addExtraData("description", description));
        gameController.shutdown(); // Trigger game closure process
    }

    // Implementation of message processing methods
    private void handleDiscardCard(Message message) {
        String username = (String) message.getData().get("username");
        String cardId = (String) message.getData().get("cardId");
        // Obtain GameController through RoomController
        GameController gameController = roomController.getGameController();
        try {
            gameController.handleDiscardAction();
            actionLogView.log(username + "discard card: " + cardId);
            confirmMessageDelivery(message.getMessageId());
        } catch (Exception ex) {
            actionLogView.log("Failed to discard card: " + ex.getMessage());
            scheduleMessageRetry(message.getMessageId());
        }
    }

    private void handleDrawFloodCard(Message message) {
        int count = (int) message.getData().get("count");
        try {
            List<Position> cards = gameController.drawFloodCards(count);
            roomController.sendDrawFloodMessage(count, gameController.getCurrentPlayer().getName());
            confirmMessageDelivery(message.getMessageId());
        } catch (Exception ex) {
            handleGameOver(new Message(MessageType.GAME_OVER, "description", "The flood pile is exhausted."));
        }
    }

    private void handleUseSandbags(Message message) {
        Player username = (Player) message.getData().get("username");
        Position position = (Position) message.getData().get("position");
        int cardIndex = (int) message.getData().get("count");
        try {
            gameController.executeSandbagsUse(position);
            actionLogView.log(username + "at" + position + "use the sandbag");
            confirmMessageDelivery(message.getMessageId());
        } catch (Exception ex) {
            actionLogView.log("Failed using sandbag: " + ex.getMessage());
            roomController.sendSandbagsMessage(username, position, cardIndex);
        }
    }

    private void handleMoveByHelicopter(Message message) {
        String username = (String) message.getData().get("username");
        Position destination = (Position) message.getData().get("destination");
        try {
            gameController.executeHelicopterMove(destination);
            actionLogView.log(username + "using helicopter moving to " + destination);
            confirmMessageDelivery(message.getMessageId());
        } catch (Exception ex) {
            actionLogView.log("Failed moving by helicopter: " + ex.getMessage());
            scheduleMessageRetry(message.getMessageId());
        }
    }

    private void handleEndTurn(Message message) {
        Player username = (Player) message.getData().get("username");
        try {
            roomController.sendEndTurnMessage(username);
            actionLogView.log(username + "end the turn!");
            confirmMessageDelivery(message.getMessageId());
        } catch (Exception ex) {
            actionLogView.log("End of turn failed: " + ex.getMessage());
            gameController.showErrorToast("Illegal turn end request");
        }
    }

    private void handleCaptureTreasure(Message message) {
        Player username = (Player) message.getData().get("username");
        TreasureType treasure = TreasureType.valueOf((String) message.getData().get("treasure"));
        List<Integer> cardIndice = (List<Integer>) message.getData().get("count");
        try {
           roomController.sendCaptureTreasureMessage(username, cardIndice);
            actionLogView.log(username + "capture the treasure " + treasure);
            confirmMessageDelivery(message.getMessageId());
        } catch (Exception ex) {
            actionLogView.log("Failed to capture the treasure: " + ex.getMessage());
            scheduleMessageRetry(message.getMessageId());
        }
    }

    private void handleMoveByNavigator(Message message) {
        Player navigatorUser = (Player) message.getData().get("navigator");
        Player targetUser = (Player) message.getData().get("target");
        Tile position = (Tile) message.getData().get("position");
        try {
            roomController.sendMoveByNavigatorMessage(navigatorUser, targetUser, position);
            actionLogView.log(navigatorUser + "uses the navigator to move " + targetUser);
            confirmMessageDelivery(message.getMessageId());
        } catch (Exception ex) {
            actionLogView.log("Navigator failed to move: " + ex.getMessage());
            gameController.showErrorToast(ex.getMessage());
        }
    }

    // Gard passing
    private void handleGiveCard(Message message) {
        Player fromUser = (Player) message.getData().get("from");
        Player toUser = (Player) message.getData().get("to");
        String cardId = (String) message.getData().get("cardId");
        int card_id = (int) message.getData().get("card_id");
        try {
            // Verify card ownership and player position
            gameController.giveCard(fromUser, toUser, cardId);
            actionLogView.log(fromUser + "send card " + cardId + "to " + toUser);
            confirmMessageDelivery(message.getMessageId());
            // Send update notifications to both parties
            roomController.sendGiveCardMessage(fromUser, toUser, card_id);
            roomController.sendGiveCardMessage(toUser, fromUser, card_id);
        } catch (Exception ex) {
            actionLogView.log("Failed to give card: " + ex.getMessage());
            gameController.showErrorToast(ex.getMessage());
            scheduleMessageRetry(message.getMessageId());
        }
    }

    // Land consolidation treatment
    private void handleShoreUp(Message message) {
        Player username = (Player) message.getData().get("username");
        Position position = (Position) message.getData().get("position");
        try {
            roomController.sendShoreUpMessage(username, position);
            actionLogView.log(username + "shore up the tile: " + position);
            confirmMessageDelivery(message.getMessageId());
            // Broadcast map status update
            roomController.broadcast(new Message(MessageType.UPDATE_ROOM,"mapState", "Tile board reset"));
        } catch (Exception ex) {
            actionLogView.log("Reinforcement operation failed: " + ex.getLocalizedMessage());
            gameController.showErrorToast("Can not shore up the tile.");
        }
    }

    // Player Mobile Basic Logic
    private void handlePlayerMove(Message message) {
        Player username = (Player) message.getData().get("username");
        Position newPosition = (Position) message.getData().get("position");
        try {
            roomController.sendMoveMessage(username, newPosition);
            actionLogView.log(username + "move to " + newPosition);
            confirmMessageDelivery(message.getMessageId());
            // Update all player perspectives
            roomController.broadcast(new Message(MessageType.UPDATE_ROOM, "fromPlayerPositions", "toPlayerPositions"));
        } catch (Exception ex) {
            actionLogView.log("Movement failed: " + ex.getMessage());
            gameController.showErrorToast("Can not move to the position!");
            scheduleMessageRetry(message.getMessageId());
        }
    }

    // Handling start turns
    private void handleTurnStart(Message message) {
        Player username = (Player) message.getData().get("username");
        try {
            gameController.startTurn(username);
            actionLogView.log(username + "'s turn starts");
            confirmMessageDelivery(message.getMessageId());
            // Send round initialization data
            Message turnData = new Message(MessageType.TURN_START, "actionPoints", "floodLevel", "handCards");
            roomController.sendStartTurnMessage(username);
        } catch (Exception ex) {
            actionLogView.log("Start exception: " + ex.getMessage());
            gameController.showErrorToast("Illegal start turn request!");
        }
    }

    // Draw treasure card
    private void handleDrawTreasureCard(Message message) {
        Player username = (Player) message.getData().get("username");
        int drawCount = (int) message.getData().get("count");
        try {
            drawnCards = gameController.handleDrawTreasureCard(drawCount, username);
            actionLogView.log(username + "draw " + drawCount + "numbers of treasure cards");
            confirmMessageDelivery(message.getMessageId());
            // Private message sending of card draw results
            Message resultMsg = new Message(MessageType.DRAW_TREASURE_CARD, "cards", "drawCards");
            roomController.sendDrawTreasureCardsMessage(drawCount, username);
        } catch (Exception ex) {
            actionLogView.log("Treasure cards stack is empty");
            gameController.showErrorToast("Can not draw any more treasure cards!");
        } finally {
            actionLogView.log("Cards in hand out of restriction");
            handleDiscardCard(message); // Trigger the discard process
        }
    }

    // Game global startup
    private void handleGameStart(Message message) {
        try {
            long config = (long) message.getData().get("config");
            gameController.startGame(config);
            actionLogView.log("Game started, pattern");
            confirmMessageDelivery(message.getMessageId());
            // Broadcast initial state
            roomController.broadcast(new Message(MessageType.GAME_START, "players", "initialMap","firstPlayer"));
        } catch (Exception ex) {
            actionLogView.log("Failed to initialize game: " + ex.getMessage());
            roomController.broadcast(new Message(MessageType.GAME_OVER,"reason", "Failing: " + ex.getMessage()));
        }
    }

//    // Room status update
//    private void handleUpdateRoom(Message message) {
//        RoomUpdateType updateType = RoomUpdateType.valueOf((String) message.getData().get("updateType"));
//        switch (updateType) {
//            case PLAYER_READY:
//                String readyUser = (String) message.getData().get("username");
//                boolean isReady = (boolean) message.getData().get("status");
//                room.setPlayerReady(readyUser, isReady);
//                break;
//            case TEAM_CHANGE:
//                String teamUser = (String) message.getData().get("username");
//                Team newTeam = Team.valueOf((String) message.getData().get("team"));
//                room.changePlayerTeam(teamUser, newTeam);
//                break;
//            case SETTING_CHANGE:
//                GameSettings newSettings = (GameSettings) message.getData().get("settings");
//                room.updateGameSettings(newSettings);
//                break;
//        }
//        // Broadcast updated room status
//        roomController.broadcast(new Message(MessageType.UPDATE_ROOM, "playerStatus", "gameSettings"));
//        confirmMessageDelivery(message.getMessageId());
//    }

    // Enhanced methods for message confirmation management
    private void confirmMessageDelivery(long messageId) {
        String key = String.valueOf(messageId);
        unconfirmedMessages.remove(key);
        actionLogView.log("Message dealing complete: " + key);
    }

    // Timed tasks ensure the final delivery of messages
    public void startMessageRetryTask() {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            if (isProcessingQueue) return;
            processMessageQueue();
        }, 0, 5, TimeUnit.SECONDS);
    }
}