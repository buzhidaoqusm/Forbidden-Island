package com.island.network;

import com.island.controller.GameController;
import com.island.model.*;

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
    private MessageType messageType;

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
    public void handleMessage(Message message) {
        MessageType messageType = message.getType();
        switch (messageType) {
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
                actionLogView.log("未知消息类型: " + messageType);
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

    public synchronized void putUnconfirmedMessage(long messageId, UnconfirmedMessage unconfirmedMessage) {
        String key = String.valueOf(messageId);
        unconfirmedMessages.put(key, unconfirmedMessage);
        messageQueue.add(messageId);
        processMessageQueue(); // 触发队列处理
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

    // 消息处理方法实现
    //-------------------------
    private void handleDiscardCard(Message message) {
        String username = (String) message.getData().get("username");
        String cardId = (String) message.getData().get("cardId");
        // 通过RoomController获取GameController
        GameController gameController = roomController.getGameController();
        try {
            gameController.discardCard(username, cardId);
            actionLogView.log(username + "丢弃了卡牌: " + cardId);
            confirmMessageDelivery(message.getMessageId());
        } catch (GameException ex) {
            actionLogView.log("卡牌丢弃失败: " + ex.getMessage());
            scheduleMessageRetry(message.getMessageId());
        }
    }

    private void handleDrawFloodCard(Message message) {
        String username = (String) message.getData().get("username");
        int count = (int) message.getData().get("count");
        try {
            List<FloodCard> cards = gameController.drawFloodCards(username, count);
            roomController.sendPrivateMessage(username, new Message(MessageType.DRAW_FLOOD_CARD, cards));
            confirmMessageDelivery(message.getMessageId());
        } catch (DeckEmptyException ex) {
            handleGameOver(new Message(MessageType.GAME_OVER, Map.of("description", "洪水牌堆耗尽")));
        }
    }

    private void handleUseSandbags(Message message) {
        String username = (String) message.getData().get("username");
        Position position = (Position) message.getData().get("position");
        try {
            gameController.useSandbags(username, position);
            actionLogView.log(username + "在位置" + position + "使用了沙袋");
            confirmMessageDelivery(message.getMessageId());
        } catch (InvalidActionException ex) {
            actionLogView.log("沙袋使用失败: " + ex.getMessage());
            roomController.sendError(username, ex.getMessage());
        }
    }

    private void handleMoveByHelicopter(Message message) {
        String username = (String) message.getData().get("username");
        Position destination = (Position) message.getData().get("destination");
        try {
            gameController.helicopterMove(username, destination);
            actionLogView.log(username + "使用直升机移动到" + destination);
            confirmMessageDelivery(message.getMessageId());
        } catch (MovementException ex) {
            actionLogView.log("直升机移动失败: " + ex.getMessage());
            scheduleMessageRetry(message.getMessageId());
        }
    }

    private void handleEndTurn(Message message) {
        String username = (String) message.getData().get("username");
        try {
            gameController.endPlayerTurn(username);
            actionLogView.log(username + "结束了回合");
            confirmMessageDelivery(message.getMessageId());
        } catch (TurnOrderException ex) {
            actionLogView.log("回合结束失败: " + ex.getMessage());
            roomController.sendError(username, "非法回合结束请求");
        }
    }

    private void handleCaptureTreasure(Message message) {
        String username = (String) message.getData().get("username");
        TreasureType treasure = TreasureType.valueOf((String) message.getData().get("treasure"));
        try {
            gameController.captureTreasure(username, treasure);
            actionLogView.log(username + "夺取了" + treasure + "宝藏!");
            confirmMessageDelivery(message.getMessageId());
        } catch (TreasureException ex) {
            actionLogView.log("宝藏夺取失败: " + ex.getMessage());
            scheduleMessageRetry(message.getMessageId());
        }
    }

    private void handleMoveByNavigator(Message message) {
        String navigatorUser = (String) message.getData().get("navigator");
        String targetUser = (String) message.getData().get("target");
        Position position = (Position) message.getData().get("position");
        try {
            gameController.navigatorMove(navigatorUser, targetUser, position);
            actionLogView.log(navigatorUser + "使用导航员移动了" + targetUser);
            confirmMessageDelivery(message.getMessageId());
        } catch (CollaborationException ex) {
            actionLogView.log("导航员移动失败: " + ex.getMessage());
            roomController.sendError(navigatorUser, ex.getMessage());
        }
    }

    //-------------------------
    // 卡牌传递处理
    //-------------------------
    private void handleGiveCard(Message message) {
        String fromUser = (String) message.getData().get("from");
        String toUser = (String) message.getData().get("to");
        String cardId = (String) message.getData().get("cardId");

        try {
            // 验证卡牌所有权和玩家位置
            gameController.transferCard(fromUser, toUser, cardId);
            actionLogView.log(fromUser + "向" + toUser + "赠送了卡牌: " + cardId);
            confirmMessageDelivery(message.getMessageId());

            // 给双方发送更新通知
            roomController.sendPrivateMessage(fromUser, new Message(MessageType.GIVE_CARD,
                    Map.of("action", "send", "cardId", cardId, "target", toUser));
            roomController.sendPrivateMessage(toUser, new Message(MessageType.GIVE_CARD,
                    Map.of("action", "receive", "cardId", cardId, "from", fromUser));
        } catch (CardTransferException ex) {
            actionLogView.log("卡牌传递失败: " + ex.getMessage());
            roomController.sendError(fromUser, ex.getMessage());
            scheduleMessageRetry(message.getMessageId());
        }
    }

    //-------------------------
    // 地块加固处理
    //-------------------------
    private void handleShoreUp(Message message) {
        String username = (String) message.getData().get("username");
        Position position = (Position) message.getData().get("position");

        try {
            gameController.shoreUpTile(username, position);
            actionLogView.log(username + "加固了位置" + position);
            confirmMessageDelivery(message.getMessageId());

            // 广播地图状态更新
            roomController.broadcast(new Message(MessageType.UPDATE_ROOM,
                    Map.of("mapState", gameController.getCurrentMapState()));
        } catch (ShoreUpException ex) {
            actionLogView.log("加固操作失败: " + ex.getLocalizedMessage());
            roomController.sendError(username, "无法加固该位置: " + position);
        }
    }

    //-------------------------
    // 玩家移动基础逻辑
    //-------------------------
    private void handlePlayerMove(Message message) {
        String username = (String) message.getData().get("username");
        Position newPosition = (Position) message.getData().get("position");

        try {
            gameController.movePlayer(username, newPosition);
            actionLogView.log(username + "移动到了" + newPosition);
            confirmMessageDelivery(message.getMessageId());

            // 更新所有玩家视野
            roomController.broadcast(new Message(MessageType.UPDATE_ROOM,
                    Map.of("playerPositions", gameController.getPlayerPositions())));
        } catch (MovementException ex) {
            actionLogView.log("移动失败: " + ex.getMessage());
            roomController.sendError(username, "无法移动到该位置");
            scheduleMessageRetry(message.getMessageId());
        }
    }

    //-------------------------
    // 回合开始处理
    //-------------------------
    private void handleTurnStart(Message message) {
        String username = (String) message.getData().get("username");

        try {
            gameController.startPlayerTurn(username);
            actionLogView.log(username + "的回合开始");
            confirmMessageDelivery(message.getMessageId());

            // 发送回合初始化数据
            Message turnData = new Message(MessageType.TURN_START,
                    Map.of(
                            "actionPoints", 3,
                            "floodLevel", gameController.getCurrentFloodLevel(),
                            "handCards", gameController.getPlayerHand(username)
                    ));
            roomController.sendPrivateMessage(username, turnData);
        } catch (TurnOrderException ex) {
            actionLogView.log("回合启动异常: " + ex.getMessage());
            roomController.sendError(username, "非法的回合开始请求");
        }
    }

    //-------------------------
    // 宝藏卡牌抽取
    //-------------------------
    private void handleDrawTreasureCard(Message message) {
        String username = (String) message.getData().get("username");
        int drawCount = (int) message.getData().get("count");

        try {
            List<TreasureCard> drawnCards = gameController.drawTreasureCards(username, drawCount);
            actionLogView.log(username + "抽取了" + drawCount + "张宝藏卡牌");
            confirmMessageDelivery(message.getMessageId());

            // 私有消息发送抽卡结果
            Message resultMsg = new Message(MessageType.DRAW_TREASURE_CARD,
                    Map.of("cards", drawnCards.stream()
                            .map(TreasureCard::getBriefInfo)
                            .collect(Collectors.toList())));
            roomController.sendPrivateMessage(username, resultMsg);
        } catch (DeckEmptyException ex) {
            actionLogView.log("宝藏牌堆已空");
            roomController.sendError(username, "无法抽取更多卡牌");
        } catch (HandLimitExceededException ex) {
            actionLogView.log("手牌超过限制: " + ex.getMessage());
            handleDiscardCard(message); // 触发弃牌流程
        }
    }

    //-------------------------
    // 游戏全局启动
    //-------------------------
    private void handleGameStart(Message message) {
        try {
            GameConfig config = (GameConfig) message.getData().get("config");
            gameController.initializeGame(config);
            actionLogView.log("游戏已启动，模式: " + config.getGameMode());
            confirmMessageDelivery(message.getMessageId());

            // 广播初始状态
            roomController.broadcast(new Message(MessageType.GAME_START,
                    Map.of(
                            "players", gameController.getPlayerList(),
                            "initialMap", gameController.getCurrentMapState(),
                            "firstPlayer", gameController.getCurrentPlayer()
                    )));
        } catch (GameInitializationException ex) {
            actionLogView.log("游戏初始化失败: " + ex.getMessage());
            roomController.broadcast(new Message(MessageType.GAME_OVER,
                    Map.of("reason", "初始化失败: " + ex.getMessage())));
        }
    }

    //-------------------------
    // 房间状态更新
    //-------------------------
    private void handleUpdateRoom(Message message) {
        RoomUpdateType updateType = RoomUpdateType.valueOf(
                (String) message.getData().get("updateType"));

        switch (updateType) {
            case PLAYER_READY:
                String readyUser = (String) message.getData().get("username");
                boolean isReady = (boolean) message.getData().get("status");
                room.setPlayerReady(readyUser, isReady);
                break;
            case TEAM_CHANGE:
                String teamUser = (String) message.getData().get("username");
                Team newTeam = Team.valueOf((String) message.getData().get("team"));
                room.changePlayerTeam(teamUser, newTeam);
                break;
            case SETTING_CHANGE:
                GameSettings newSettings = (GameSettings) message.getData().get("settings");
                room.updateGameSettings(newSettings);
                break;
        }

        // 广播更新后的房间状态
        roomController.broadcast(new Message(MessageType.UPDATE_ROOM,
                Map.of(
                        "playerStatus", room.getPlayerStatus(),
                        "gameSettings", room.getCurrentSettings()
                )));
        confirmMessageDelivery(message.getMessageId());
    }

    //-------------------------
    // 消息确认管理增强方法
    //-------------------------
    private void confirmMessageDelivery(long messageId) {
        String key = String.valueOf(messageId);
        unconfirmedMessages.remove(key);
        actionLogView.log("消息处理完成: " + key);
    }
}