package com.island.network;

import com.island.controller.GameController;
import com.island.models.Room;
import com.island.models.adventurers.Navigator;
import com.island.models.adventurers.Pilot;
import com.island.models.adventurers.Player;
import com.island.models.card.Card;
import com.island.models.game.GameState;
import com.island.models.island.Position;
import com.island.models.island.Tile;
import com.island.models.treasure.TreasureType;
import com.island.util.Constant;
import com.island.views.ui.ActionLogView;

import static com.island.util.ui.Dialog.showMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Handles all network messages in the game, including message processing, retrying, and acknowledgment.
 * This class manages the communication between players and maintains game state consistency.
 */
public class MessageHandler {
    /** Map to store messages that haven't been acknowledged by all recipients */
    private final Map<Long, UnconfirmedMessage> unconfirmedMessages = new ConcurrentHashMap<>();
    
    /** Map to store received messages to prevent duplicate processing */
    private Map<Long, Message> receivedMessages = new ConcurrentHashMap<>();
    
    /** Reference to the game controller for managing game state */
    private GameController gameController;
    
    /** Reference to the action log view for displaying game events */
    private ActionLogView actionLogView;
    
    /** Reference to the current game room */
    private Room room;

    /** Scheduler for handling message retries and timeouts */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8);
    
    /** Time interval between message retry attempts (in milliseconds) */
    private static final long RETRY_INTERVAL = 3000;
    
    /** Maximum number of retry attempts for unconfirmed messages */
    private static final int MAX_RETRY_COUNT = 3;

    /** Queue for storing message IDs to ensure ordered processing */
    private final PriorityQueue<Long> messageQueue = new PriorityQueue<>();
    
    /** Lock object for synchronizing queue access */
    private final Object queueLock = new Object();
    
    /** Flag indicating whether the queue is currently being processed */
    private boolean isProcessingQueue = false;

    /**
     * Constructor for MessageHandler
     * @param gameController The game controller instance
     */
    public MessageHandler(GameController gameController) {
        this.gameController = gameController;
        room = gameController.getRoomController().getRoom();
    }

    /**
     * Handles incoming messages and routes them to appropriate handlers
     * @param message The message to be processed
     * @throws Exception if message handling fails
     */
    public void handleMessage(Message message) throws Exception {
        // Check if this message has already been processed
        if (message.isAck() && receivedMessages.containsKey(message.getMessageId())) {
            // If we've already processed this message, just send an ACK and return
            gameController.getRoomController().sendAckMessage(message);
            return;
        }

        if (message.isAck()) {
            gameController.getRoomController().sendAckMessage(message);
        }
        receivedMessages.put(message.getMessageId(), message);

        switch (message.getType()) {
            case PLAYER_JOIN -> gameController.handlePlayerJoin(message);
            case PLAYER_LEAVE -> handlePlayerLeave(message);
            case LEAVE_ROOM -> handleLeaveRoom(message);
            case UPDATE_ROOM -> handleUpdateRoom(message);
            case GAME_START -> handleGameStart(message);
            case DRAW_TREASURE_CARD -> handleDrawTreasureCard(message);
            case TURN_START -> handleTurnStart(message);
            case MOVE_PLAYER -> handlePlayerMove(message);
            case SHORE_UP -> handleShoreUp(message);
            case GIVE_CARD -> handleGiveCard(message);
            case MOVE_PLAYER_BY_NAVIGATOR -> handleMoveByNavigator(message);
            case CAPTURE_TREASURE -> handleCaptureTreasure(message);
            case END_TURN -> handleEndTurn(message);
            case HELICOPTER_MOVE -> handleMoveByHelicopter(message);
            case SANDBAGS_USE -> handleUseSandbags(message);
            case DRAW_FLOOD_CARD -> handleDrawFloodCard(message);
            case DISCARD_CARD -> handleDiscardCard(message);
            case GAME_OVER -> handleGameOver(message);
            case MESSAGE_ACK -> handleMessageAck(message);
        }
    }

    /**
     * Handles acknowledgment messages and removes confirmed messages from tracking
     * @param message The acknowledgment message
     */
    private void handleMessageAck(Message message) {
        long messageId = message.getMessageId();
        if (unconfirmedMessages.containsKey(messageId)) {
            UnconfirmedMessage unconfirmedMessage = unconfirmedMessages.get(messageId);
            unconfirmedMessage.removeReceiver(message.getFrom());
            if (!unconfirmedMessage.hasPendingReceivers()) {
                unconfirmedMessages.remove(messageId);
                synchronized (queueLock) {
                    messageQueue.remove(messageId);
                }
            }
        }
    }

    /**
     * Schedules a message for retry if not acknowledged
     * @param messageId The ID of the message to retry
     */
    public void scheduleMessageRetry(long messageId) {
        // Add message ID to the queue
        synchronized (queueLock) {
            messageQueue.offer(messageId);

            // Start queue processing if not already running
            if (!isProcessingQueue) {
                isProcessingQueue = true;
                scheduler.schedule(this::processMessageQueue, RETRY_INTERVAL, TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     * Processes the message queue in order
     */
    private void processMessageQueue() {
        try {
            Long messageId;

            // Get next message ID from queue
            synchronized (queueLock) {
                if (messageQueue.isEmpty()) {
                    isProcessingQueue = false;
                    return;
                }
                messageId = messageQueue.poll();
            }

            // Process the message
            processMessageRetry(messageId);
        } catch (Exception e) {
            e.printStackTrace();
            synchronized (queueLock) {
                isProcessingQueue = false;
            }
        }
    }

    /**
     * Processes a single message retry attempt
     * @param messageId The ID of the message to retry
     */
    private void processMessageRetry(long messageId) {
        UnconfirmedMessage unconfirmed = unconfirmedMessages.get(messageId);
        if (unconfirmed != null && unconfirmed.hasPendingReceivers()) {

            if (unconfirmed.getRetryCount() < MAX_RETRY_COUNT) {
                // Create a copy of pendingReceivers to avoid concurrent modification
                Set<String> receiversCopy = new HashSet<>(unconfirmed.getPendingReceivers());

                for (String receiver : receiversCopy) {
                    // Send message to unconfirmed receivers
                    Message message = unconfirmed.getMessage();
                    message.setTo(receiver);
                    gameController.getRoomController().broadcast(message);
                }

                // Increment retry count
                unconfirmed.incrementRetryCount();

                // Schedule next retry
                synchronized (queueLock) {
                    messageQueue.offer(messageId);
                }
                scheduler.schedule(this::processMessageQueue, RETRY_INTERVAL, TimeUnit.MILLISECONDS);
            } else {
                // Trigger state synchronization after max retries
                gameController.showErrorToast("Player(s) " + unconfirmed.getPendingReceivers().toString() + " did not receive the message!");
            }
        }
    }

    /**
     * Handles a player leaving the room
     * @param message The leave room message containing player information
     */
    private void handleLeaveRoom(Message message) {
        String username = message.getFrom();
        Player player = room.getPlayerByUsername(username);
        Player currentProgramPlayer = room.getCurrentProgramPlayer();
        if (room.isHost(username) && !currentProgramPlayer.getName().equals(username)) {
            // The host player leaves the room
            showMessage("Warning", "The host player leaves the room. The game will be closed.");
            gameController.getGameSubject().setGameState(GameState.GAME_OVER);
            gameController.getGameSubject().notifyObservers();
        } else if (!room.isHost(username)) {
            // Other players leave the room
            room.removePlayer(player);
            gameController.getRoomController().removeHeartbeat(username);
            if (!currentProgramPlayer.getName().equals(username)) showMessage("Warning", username + " leave the room.");
        }
    }

    /**
     * Handles a player leaving the game
     * @param message The player leave message
     */
    private void handlePlayerLeave(Message message) {
        String username = message.getFrom();
        showMessage("Warning", username + " leave the game. The game will be closed.");
        gameController.getGameSubject().setGameState(GameState.GAME_OVER);
        gameController.getGameSubject().notifyObservers();
    }

    /**
     * Handles game over condition
     * @param message The game over message containing the reason
     */
    private void handleGameOver(Message message) {
        String description = message.getData().get("description").toString();
        showMessage("Game Over", description);
        gameController.getGameSubject().setGameState(GameState.GAME_OVER);
        gameController.getGameSubject().notifyObservers();
    }

    /**
     * Handles card discarding action
     * @param message The discard card message containing card information
     */
    private void handleDiscardCard(Message message) {
        String playerName = message.getFrom();
        Player player = room.getPlayerByUsername(playerName);
        int cardIndex = Integer.parseInt(message.getData().get("cardIndex").toString());
        Card removedCard = player.removeCard(cardIndex);
        gameController.addTreasureDiscardPile(removedCard);

        // Notify using Observer pattern
        gameController.getGameSubject().notifyCardChanged();
        gameController.getGameSubject().notifyPlayerInfoChanged();
        gameController.getGameSubject().notifyActionBarChanged();

        actionLogView.addLog(playerName + " discard " + removedCard.getName());
    }

    /**
     * Handles drawing flood cards
     * @param message The draw flood card message containing count information
     */
    private void handleDrawFloodCard(Message message) {
        String playerName = message.getFrom();
        int count = Integer.parseInt(message.getData().get("count").toString());
        List<Position> positions = gameController.drawFloodCards(count);
        StringBuilder sb = new StringBuilder();
        sb.append(playerName).append(" draw ").append(count).append(" flood card(s): ");
        for (Position position : positions) {
            Tile floodedTile = gameController.getIsland().getTile(position);
            sb.append(Constant.tileNameMap.get(floodedTile.getName()));
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        actionLogView.addLog(sb.toString());

        // Notifying using Observer pattern
        gameController.getGameSubject().notifyActionBarChanged();
        gameController.getGameSubject().notifyBoardChanged();
        gameController.getGameSubject().notifyCardChanged();
    }

    /**
     * Handles using sandbags special action
     * @param message The sandbags use message containing tile information
     */
    private void handleUseSandbags(Message message) {
        String playerName = message.getFrom();
        Player player = room.getPlayerByUsername(playerName);
        String positionX = message.getData().get("positionX").toString();
        String positionY = message.getData().get("positionY").toString();
        int x = Integer.parseInt(positionX);
        int y = Integer.parseInt(positionY);
        Position position = new Position(x, y);
        String tileName = message.getData().get("tileName").toString();
        int cardIndex = Integer.parseInt(message.getData().get("cardIndex").toString());
        gameController.getIsland().getTile(position).shoreUp();

        Card removedCard =  player.removeCard(cardIndex);
        gameController.addTreasureDiscardPile(removedCard);

        // Notifying using Observer pattern
        gameController.getGameSubject().notifyActionBarChanged();
        gameController.getGameSubject().notifyBoardChanged();
        gameController.getGameSubject().notifyCardChanged();
        gameController.getGameSubject().notifyPlayerInfoChanged();

        actionLogView.addLog(playerName + " use sandbags to shore up " + Constant.tileNameMap.get(tileName));
    }

    /**
     * Handles helicopter lift special action
     * @param message The helicopter move message containing player and destination information
     */
    private void handleMoveByHelicopter(Message message) {
        String username = message.getFrom();
        Player user = room.getPlayerByUsername(username);
        int newPositionX = Integer.parseInt(message.getData().get("newPositionX").toString());
        int newPositionY = Integer.parseInt(message.getData().get("newPositionY").toString());
        Position newPosition = new Position(newPositionX, newPositionY);
        String tileName = message.getData().get("tileName").toString();
        int cardIndex = Integer.parseInt(message.getData().get("cardIndex").toString());
        int playerCount = Integer.parseInt(message.getData().get("playerCount").toString());
        StringBuilder sb = new StringBuilder();
        sb.append(username).append(" use helicopter to move ");
        int i = 0;
        while (i != playerCount) {
            String playerName = (String) message.getData().get("player" + i);
            Player player = room.getPlayerByUsername(playerName);
            player.setPosition(newPosition);
            sb.append(playerName).append(", ");
            i++;
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append(" to ").append(Constant.tileNameMap.get(tileName));
        actionLogView.addLog(sb.toString());

        Card removedCard = user.removeCard(cardIndex);
        gameController.addTreasureDiscardPile(removedCard);

        // Notify using Observer pattern
        gameController.getGameSubject().notifyActionBarChanged();
        gameController.getGameSubject().notifyBoardChanged();
        gameController.getGameSubject().notifyPlayerInfoChanged();
        gameController.getGameSubject().notifyCardChanged();
    }

    /**
     * Handles end of turn action
     * @param message The end turn message
     */
    private void handleEndTurn(Message message) {
        gameController.setRemainingActions(0);
        String playerName = message.getFrom();

        // Notify using Observer pattern
        gameController.getGameSubject().notifyActionBarChanged();

        actionLogView.addLog(playerName + " has ended turn");
    }

    /**
     * Handles treasure capture action
     * @param message The capture treasure message containing treasure type
     */
    private void handleCaptureTreasure(Message message) {
        String playerName = message.getFrom();
        Player player = room.getPlayerByUsername(playerName);
        String treasureName = message.getData().get("treasureType").toString();
        TreasureType treasureType = TreasureType.valueOf(treasureName);
        gameController.getIslandController().captureTreasure(player, treasureType);

        // Notify using Observer pattern
        gameController.getGameSubject().notifyPlayerInfoChanged();
        gameController.getGameSubject().notifyCardChanged();
        gameController.getGameSubject().notifyBoardChanged();

        actionLogView.addLog(playerName + " capture " + treasureName);
    }

    /**
     * Handles navigator special ability movement
     * @param message The navigator move message containing target player and destination
     */
    private void handleMoveByNavigator(Message message) {
        String playerName = message.getFrom();
        Player player = room.getPlayerByUsername(playerName);
        String toPlayer = message.getData().get("playerName").toString();
        Player playerTo = room.getPlayerByUsername(toPlayer);
        String positionX = message.getData().get("positionX").toString();
        String positionY = message.getData().get("positionY").toString();
        int x = Integer.parseInt(positionX);
        int y = Integer.parseInt(positionY);
        Position position = new Position(x, y);
        playerTo.setPosition(position);
        String tileName = message.getData().get("tileName").toString();
        gameController.decreaseRemainingActions();

        // Notify using Observer pattern
        gameController.getGameSubject().notifyActionBarChanged();
        gameController.getGameSubject().notifyBoardChanged();

        actionLogView.addLog(playerName + " use navigator ability to move " + toPlayer + " to " + Constant.tileNameMap.get(tileName));

        if (player instanceof Navigator navigator) {
            navigator.resetTargetAndMoves();
        }
    }

    /**
     * Handles giving a card to another player
     * @param message The give card message containing card and player information
     */
    private void handleGiveCard(Message message) {
        String fromPlayer = message.getFrom();
        Player playerFrom = room.getPlayerByUsername(fromPlayer);
        String toPlayer = message.getData().get("playerName").toString();
        Player playerTo = room.getPlayerByUsername(toPlayer);
        String cardName = message.getData().get("Card").toString();
        gameController.giveCard(playerFrom, playerTo, cardName);
        gameController.getGameSubject().notifyActionBarChanged();
        gameController.getGameSubject().notifyPlayerInfoChanged();
        actionLogView.addLog(fromPlayer + " give " + cardName + " to " + toPlayer);
    }

    /**
     * Handles shore up action on a tile
     * @param message The shore up message containing tile information
     */
    private void handleShoreUp(Message message) {
        String playerName = message.getFrom();
        Player player = room.getPlayerByUsername(playerName);
        String positionX = message.getData().get("positionX").toString();
        String positionY = message.getData().get("positionY").toString();
        int x = Integer.parseInt(positionX);
        int y = Integer.parseInt(positionY);
        Position position = new Position(x, y);
        String tileName = message.getData().get("tileName").toString();
        gameController.getIslandController().shoreUpTile(player, position);
        gameController.getGameSubject().notifyActionBarChanged();
        gameController.getGameSubject().notifyBoardChanged();
        actionLogView.addLog(playerName + " shore up " + Constant.tileNameMap.get(tileName));
    }

    /**
     * Handles player movement action
     * @param message The move player message containing destination information
     */
    private void handlePlayerMove(Message message) {
        String playerName = message.getFrom();
        Player player = room.getPlayerByUsername(playerName);
        String positionX = message.getData().get("positionX").toString();
        String positionY = message.getData().get("positionY").toString();
        int x = Integer.parseInt(positionX);
        int y = Integer.parseInt(positionY);
        Position position = new Position(x, y);
        player.setPosition(position);
        String tileName = message.getData().get("tileName").toString();
        gameController.decreaseRemainingActions();
        gameController.getGameSubject().notifyActionBarChanged();
        gameController.getGameSubject().notifyPlayerMoved(player, position);
        actionLogView.addLog(playerName + " move to " + Constant.tileNameMap.get(tileName));

        if (player instanceof Pilot pilot) {
            pilot.setHasFlewThisTurn(true);
        }
    }

    /**
     * Handles start of turn action
     * @param message The turn start message
     */
    private void handleTurnStart(Message message) {
        Player player = room.getPlayerByUsername(message.getTo());
        gameController.startTurn(player);
    }

    /**
     * Handles drawing treasure cards
     * @param message The draw treasure card message containing count information
     */
    private void handleDrawTreasureCard(Message message) {
        String playerName = message.getFrom();
        Player player = room.getPlayerByUsername(playerName);
        int count = Integer.parseInt(message.getData().get("count").toString());
        gameController.handleDrawTreasureCard(count, player);
        actionLogView.addLog(playerName + " draw " + count + " treasure card(s)");
        gameController.getGameSubject().notifyCardChanged();
        gameController.getGameSubject().notifyPlayerInfoChanged();
        gameController.getGameSubject().notifyActionBarChanged();
    }

    /**
     * Handles game start action
     * @param message The game start message containing initial game parameters
     */
    private void handleGameStart(Message message) {
        long seed = Long.parseLong(message.getData().get("seed").toString());
        int waterLevel = Integer.parseInt(message.getData().get("waterLevel").toString());
        // Game start
        gameController.startGame(seed);
        gameController.setWaterLevel(waterLevel);
        gameController.getGameSubject().notifyWaterLevelChanged(waterLevel);
    }

    /**
     * Handles room update action
     * Updates the room information with current player list
     * @param message The update room message containing player information
     */
    private void handleUpdateRoom(Message message) {
        // Update room information
        int playerCount = Integer.parseInt((String) message.getData().get("playerCount"));
        ArrayList<Player> players = new ArrayList<>();
        int i = 1;
        while (i != playerCount + 1) {
            String username = (String) message.getData().get("player" + i);
            players.add(new Player(username));
            i++;
        }
        room.setPlayers(players);

        // Notify observers of player information change
        if (gameController != null) {
            gameController.getGameSubject().notifyPlayerInfoChanged();
        }
    }

    /**
     * Sets the action log view for displaying game actions
     * @param actionLogView The action log view instance
     */
    public void setActionLogView(ActionLogView actionLogView) {
        this.actionLogView = actionLogView;
    }

    /**
     * Adds an unconfirmed message to the tracking map
     * @param messageId The ID of the message
     * @param unconfirmedMessage The unconfirmed message instance
     */
    public void putUnconfirmedMessage(long messageId, UnconfirmedMessage unconfirmedMessage) {
        unconfirmedMessages.put(messageId, unconfirmedMessage);
    }

    /**
     * Gets the map of all unconfirmed messages
     * @return Map of message IDs to unconfirmed messages
     */
    public Map<Long, UnconfirmedMessage> getUnconfirmedMessages() {
        return unconfirmedMessages;
    }

    /**
     * Shuts down the message handler
     * Stops the scheduler and cleans up resources
     */
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            try {
                scheduler.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Clear all pending messages
        unconfirmedMessages.clear();
        receivedMessages.clear();
        synchronized (queueLock) {
            messageQueue.clear();
            isProcessingQueue = false;
        }
    }
}
