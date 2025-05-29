package com.island.network;

import com.island.controller.GameController;
import com.island.model.*;
import com.island.model.adventurers.Player;
import com.island.model.card.Card;
import com.island.model.island.Island;
import com.island.model.island.Position;
import com.island.model.island.Tile;
import com.island.model.treasure.TreasureType;
import com.island.model.Room;
import com.island.util.ui.Dialog;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RoomController {
    /** Current game room instance */
    private Room room;
    
    /** Broadcast sender for network messages */
    private final BroadcastSender sender;
    
    /** Broadcast receiver for network messages */
    private final BroadcastReceiver receiver;
    
    /** Map to track last heartbeat time for each player */
    private final Map<String, Long> playerLastHeartbeat;
    
    /** Scheduler for periodic tasks */
    private final ScheduledExecutorService scheduler;
    
    /** Handler for processing game messages */
    private MessageHandler messageHandler;
    
    /** Game controller reference */
    private GameController gameController;
    
    /** Island instance reference */
    private Island island;

    /** Interval for sending heartbeat messages (5 seconds) */
    private static final long HEARTBEAT_INTERVAL = 5000;
    
    /** Timeout threshold for player disconnection (15 seconds) */
    private static final long PLAYER_TIMEOUT = 15000;

    /**
     * Constructs a new RoomController
     * @param room The game room to be controlled
     */
    public RoomController(Room room) {
        this.room = room;
        this.sender = new BroadcastSender();
        this.receiver = new BroadcastReceiver(this);
        this.playerLastHeartbeat = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(2);

        // Start receiver thread
        new Thread(receiver).start();

        // Start heartbeat sending task
        startHeartbeat();

        // Start heartbeat checking task
        startHeartbeatCheck();
    }

    /**
     * Starts the heartbeat sending task
     * Sends periodic heartbeat messages to notify other players of this player's presence
     */
    private void startHeartbeat() {
        // Send heartbeat periodically
        scheduler.scheduleAtFixedRate(() -> {
            try {
                String heartbeatMsg = String.format("HEARTBEAT|%d|%s", room.getId(), room.getCurrentProgramPlayer().getName());
                // Heartbeat message will be encrypted by BroadcastSender.broadcast method
                sender.broadcast(heartbeatMsg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * Starts the heartbeat checking task
     * Monitors other players' heartbeats and handles disconnections
     */
    private void startHeartbeatCheck() {
        // Check for player timeouts
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            playerLastHeartbeat.entrySet().removeIf(entry -> {
                if (now - entry.getValue() > PLAYER_TIMEOUT) {
                    try {
                        handlePlayerDisconnect(entry.getKey());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return true;
                }
                return false;
            });
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * Removes a player's heartbeat tracking
     * @param username The username of the player to remove
     */
    public void removeHeartbeat(String username) {
        playerLastHeartbeat.remove(username);
    }

    /**
     * Handles player disconnection
     * Notifies the message handler about the disconnected player
     * @param username The username of the disconnected player
     * @throws Exception If there's an error handling the disconnection
     */
    private void handlePlayerDisconnect(String username) throws Exception {
        // Notify game message handler about player disconnection
        if (messageHandler != null) {
            Message leaveMsg = new Message(
                    MessageType.PLAYER_LEAVE,
                    room.getId(),
                    username
            );
            messageHandler.handleMessage(leaveMsg);
        }
    }

    /**
     * Updates the last heartbeat time for a player
     * @param username The username of the player
     */
    public void updatePlayerHeartbeat(String username) {
        playerLastHeartbeat.put(username, System.currentTimeMillis());
    }

    /**
     * Shuts down the room controller
     * Stops all scheduled tasks and closes network connections
     */
    public void shutdown() {
        // Stop the scheduler
        if (scheduler != null) {
            scheduler.shutdownNow();  // Immediately stop all tasks
            try {
                scheduler.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Stop the receiver
        if (receiver != null) {
            receiver.stop();
        }

        // Turn off the transmitter
        if (sender != null) {
            sender.close();
        }

        // Clean up resources
        playerLastHeartbeat.clear();
    }

    /**
     * Broadcasts a message to all players in the room
     * If the message requires acknowledgment, it will be tracked for retries
     * @param message The message to broadcast
     */
    public void broadcast(Message message) {
        if (sender != null) {
            try {
                sender.broadcast(message.toString());
                if (message.isAck()) {
                    if (messageHandler.getUnconfirmedMessages().containsKey(message.getMessageId())) return;
                    // Get all players who need to receive this message
                    Set<String> receivers = room.getPlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toSet());
                    // If it is a message that requires confirmation, add it to the list of unconfirmed messages
                    messageHandler.putUnconfirmedMessage(message.getMessageId(), new UnconfirmedMessage(message, receivers));
                    messageHandler.scheduleMessageRetry(message.getMessageId());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Gets the room ID
     * @return The current room ID
     */
    public int getRoomId() {
        return room.getId();
    }

    /**
     * Sets the message handler for this room
     * @param handler The message handler to set
     */
    public void setMessageHandler(MessageHandler handler) {
        this.messageHandler = handler;
    }

    /**
     * Handles game-related messages
     * Forwards messages to the game message handler
     * @param message The game message to handle
     * @throws Exception If there's an error handling the message
     */
    public void handleGameMessage(Message message) throws Exception {
        // Forward to game message processor
        if (messageHandler != null) {
            messageHandler.handleMessage(message);
        }
    }

    /**
     * Handles room join requests from other players
     * Processes both join requests and join responses
     * @param message The join request/response message
     * @throws Exception If there's an error handling the request
     */
    public void handleJoinRequest(Message message) throws Exception {
        // Don't process own messages
        if (message.getFrom().equals(room.getCurrentProgramPlayer().getName())) {
            return;
        }

        // Don't process if player is already in room
        if (room.getPlayers().stream().anyMatch(p -> p.getName().equals(message.getFrom()))) {
            return;
        }

        // Don't process if game has already started
        if (gameController.isGameStart()) {
            return;
        }

        // Check if this is a join request
        if (message.getData().containsKey("isRequest")) {
            // Process join request if current player is host
            if (room.isHost(room.getCurrentProgramPlayer().getName())) {
                if (room.getPlayers().size() >= 4) {
                    // Room is full, reject join request
                    sendJoinResponse(message.getFrom(), false);
                    return;
                }
                // Show confirmation dialog in JavaFX thread
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Player Join Request");
                    alert.setHeaderText("Player " + message.getFrom() + " requests to join the room");
                    alert.setContentText("Do you agree to let this player join?");

                    // Custom button text
                    ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Accept");
                    ((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Reject");

                    // Wait for user response
                    alert.showAndWait().ifPresent(response -> {
                        boolean isAccepted = response == ButtonType.OK;
                        sendJoinResponse(message.getFrom(), isAccepted);
                        if (isAccepted) {
                            room.addPlayer(new Player(message.getFrom()));
                            sendUpdateRoomMessage();

                            // Notify observers after player joins
                            if (gameController != null) {
                                gameController.updatePlayersInfo();
                            }
                        }
                    });
                });
            }
        } else {
            // Don't process messages not intended for this player
            if (message.getTo() == null || !message.getTo().equals(room.getCurrentProgramPlayer().getName())) {
                return;
            }
            // Process join response message
            if (message.getData().containsKey("isAccepted")) {
                boolean isAccepted = Boolean.parseBoolean(message.getData().get("isAccepted").toString());
                if (isAccepted) {
                    // Join successful, update room state
                    Player player = new Player(message.getFrom());
                    room.addPlayer(player);
                    room.setHostPlayer(player);
                    Dialog.showMessage("Join Success", "You have successfully joined the room!");

                    // Notify observers after player joins
                    if (gameController != null) {
                        gameController.updatePlayersInfo();
                    }
                } else {
                    // Join failed
                    Dialog.showMessage("Join Failed", "The host rejected your join request.");
                }
            }
        }
    }

    /**
     * Sends a response to a join request
     * @param username The username of the player requesting to join
     * @param b Whether the join request was accepted
     */
    private void sendJoinResponse(String username, boolean b) {
        Message response = new Message(
                MessageType.PLAYER_JOIN,
                room.getId(),
                room.getHostPlayer().getName(),
                username
        );
        response.addExtraData("isAccepted", b);
        try {
            broadcast(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the current room instance
     * @return The current room
     */
    public Room getRoom() {
        return room;
    }

    /**
     * Gets the message handler
     * @return The current message handler
     */
    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    /**
     * Sets the game controller
     * @param gameController The game controller to set
     */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    /**
     * Sets the island instance
     * Used for constructing game-related messages
     * @param island The island instance to set
     */
    public void setIsland(Island island) {
        this.island = island;
    }

    /**
     * Sends a room update message to all players
     * Updates include player count and player names
     */
    public void sendUpdateRoomMessage() {
        // Update room information
        Message updateRoomMessage = new Message(
                MessageType.UPDATE_ROOM,
                room.getId(),
                room.getHostPlayer().getName(),
                true
        );
        updateRoomMessage.addExtraData("playerCount", room.getPlayers().size());
        int i = 1;
        for (Player p : room.getPlayers()) {
            updateRoomMessage.addExtraData("player" + i, p.getName());
            i++;
        }
        broadcast(updateRoomMessage);
    }

    /**
     * Sends a message for drawing treasure cards
     * @param count Number of cards to draw
     * @param player Player drawing the cards
     */
    public void sendDrawTreasureCardsMessage(int count, Player player) {
        Message message = new Message(MessageType.DRAW_TREASURE_CARD,
                room.getId(),
                player.getName(),
                true
        );
        message.addExtraData("count", count);
        broadcast(message);
    }

    /**
     * Sends a player movement message
     * @param player Player who is moving
     * @param position New position of the player
     */
    public void sendMoveMessage(Player player, Position position) {
        Message message = new Message(MessageType.MOVE_PLAYER,
                room.getId(),
                player.getName(),
                true
        );
        message.addExtraData("positionX", position.getX());
        message.addExtraData("positionY", position.getY());
        message.addExtraData("tileName", island.getTile(position).getName());
        broadcast(message);
    }

    /**
     * Sends a shore up action message
     * @param currentPlayer Player performing the shore up
     * @param position Position of the tile being shored up
     */
    public void sendShoreUpMessage(Player currentPlayer, Position position) {
        Message message = new Message(MessageType.SHORE_UP,
                room.getId(),
                currentPlayer.getName(),
                true
        );
        message.addExtraData("positionX", position.getX());
        message.addExtraData("positionY", position.getY());
        message.addExtraData("tileName", island.getTile(position).getName());
        broadcast(message);
    }

    /**
     * Sends a message for giving a card to another player
     * @param currentPlayer Player giving the card
     * @param selectedPlayer Player receiving the card
     * @param selectedCard Card being given
     */
    public void sendGiveCardMessage(Player currentPlayer, Player selectedPlayer, Card selectedCard) {
        Message message = new Message(MessageType.GIVE_CARD,
                room.getId(),
                currentPlayer.getName(),
                true
        );
        message.addExtraData("Card", selectedCard.getName());
        message.addExtraData("playerName", selectedPlayer.getName());
        broadcast(message);
    }

    /**
     * Sends a message for navigator's special move ability
     * @param currentPlayer Navigator player
     * @param player Player being moved
     * @param tile Destination tile
     */
    public void sendMoveByNavigatorMessage(Player currentPlayer, Player player, Tile tile) {
        Message message = new Message(MessageType.MOVE_PLAYER_BY_NAVIGATOR,
                room.getId(),
                currentPlayer.getName(),
                true
        );
        message.addExtraData("positionX", tile.getPosition().getX());
        message.addExtraData("positionY", tile.getPosition().getY());
        message.addExtraData("playerName", player.getName());
        message.addExtraData("tileName", tile.getName());
        broadcast(message);
    }

    /**
     * Sends a message for capturing a treasure
     * @param player Player capturing the treasure
     * @param treasureType Type of treasure being captured
     */
    public void sendCaptureTreasureMessage(Player player, TreasureType treasureType) {
        Message message = new Message(MessageType.CAPTURE_TREASURE,
                room.getId(),
                player.getName(),
                true
        );
        message.addExtraData("treasureType", treasureType.toString());
        broadcast(message);
    }

    /**
     * Sends a message to end the current player's turn
     * @param currentPlayer Player ending their turn
     */
    public void sendEndTurnMessage(Player currentPlayer) {
        Message message = new Message(MessageType.END_TURN,
                room.getId(),
                currentPlayer.getName(),
                true
        );
        broadcast(message);
    }

    /**
     * Sends a message for using sandbags
     * @param user Player using the sandbags
     * @param position Position where sandbags are being used
     * @param cardIndex Index of the sandbags card being used
     */
    public void sendSandbagsMessage(Player user, Position position, int cardIndex) {
        Message message = new Message(
                MessageType.SANDBAGS_USE,
                room.getId(),
                user.getName(),
                true
        );
        message.addExtraData("positionX", position.getX());
        message.addExtraData("positionY", position.getY());
        message.addExtraData("tileName", island.getTile(position).getName());
        message.addExtraData("cardIndex", cardIndex);
        broadcast(message);
    }

    /**
     * Sends a message for helicopter movement
     * @param helicopterPlayers List of players being moved
     * @param user Player using the helicopter card
     * @param newPosition Destination position
     * @param cardIndex Index of the helicopter card being used
     */
    public void sendHelicopterMoveMessage(List<Player> helicopterPlayers, Player user, Position newPosition, int cardIndex) {
        Message message = new Message(
                MessageType.HELICOPTER_MOVE,
                room.getId(),
                user.getName(),
                true
        );
        message.addExtraData("newPositionX", newPosition.getX());
        message.addExtraData("newPositionY", newPosition.getY());
        message.addExtraData("tileName", island.getTile(newPosition).getName());
        message.addExtraData("cardIndex", cardIndex);
        message.addExtraData("playerCount", helicopterPlayers.size());
        for (int i = 0; i < helicopterPlayers.size(); i++) {
            message.addExtraData("player" + i, helicopterPlayers.get(i).getName());
        }
        broadcast(message);
    }

    /**
     * Sends a message for drawing flood cards
     * @param count Number of flood cards to draw
     * @param playerName Name of the player drawing cards
     */
    public void sendDrawFloodMessage(int count, String playerName) {
        Message message = new Message(MessageType.DRAW_FLOOD_CARD,
                room.getId(),
                playerName,
                true
        );
        message.addExtraData("count", count);
        broadcast(message);
    }

    /**
     * Sends a game over message
     * @param description Description of how the game ended
     */
    public void sendGameOverMessage(String description) {
        Message message = new Message(MessageType.GAME_OVER,
                room.getId(),
                "system",
                true
        );
        message.addExtraData("description", description);
        broadcast(message);
    }

    /**
     * Sends a message for discarding a card
     * @param player Player discarding the card
     * @param cardIndex Index of the card being discarded
     */
    public void sendDiscardMessage(Player player, int cardIndex) {
        Message message = new Message(MessageType.DISCARD_CARD,
                room.getId(),
                player.getName(),
                true
        );
        message.addExtraData("cardIndex", cardIndex);
        broadcast(message);
    }

    /**
     * Sends a message to start a player's turn
     * @param nextPlayer Player whose turn is starting
     */
    public void sendStartTurnMessage(Player nextPlayer) {
        Message message = new Message(MessageType.TURN_START,
                room.getId(),
                nextPlayer.getName(),
                true
        );
        broadcast(message);
    }

    /**
     * Sends a message to start the game
     * @param player Player starting the game
     * @param waterLevel Initial water level
     */
    public void sendStartGameMessage(Player player, AtomicInteger waterLevel) {
        long seed = System.currentTimeMillis();
        // Start game message
        Message startGameMessage = new Message(
                MessageType.GAME_START,
                room.getId(),
                player.getName(),
                true
        );
        startGameMessage.addExtraData("seed", seed);
        startGameMessage.addExtraData("waterLevel", waterLevel.get());
        broadcast(startGameMessage);
    }

    /**
     * Sends an acknowledgment message
     * @param message Original message being acknowledged
     */
    public void sendAckMessage(Message message) {
        Message ackMessage = new Message(
                message.getMessageId(),
                MessageType.MESSAGE_ACK,
                room.getId(),
                room.getCurrentProgramPlayer().getName(),
                message.getFrom()
        );
        System.out.println("send ack message" + message.toString());
        broadcast(ackMessage);
    }
}
