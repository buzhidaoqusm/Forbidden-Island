package com.island.controller;

import java.util.*;

import com.island.model.*;

/**
 * The PlayerController class manages all player-related operations and states in the Forbidden Island game.
 *
 * This controller:
 * - Handles player initialization and role assignment
 * - Manages player card distributions and interactions
 * - Evaluates player capabilities (what actions they can perform)
 * - Tracks player state changes during the game
 * - Coordinates between player models and the game controller
 * - Implements special role-specific abilities and constraints
 */
public class PlayerController {
    /**
     * Reference to the main game controller
     */
    private GameController gameController;

    /**
     * Reference to the current game room
     */
    private Room room;

    /**
     * Currently selected card for actions like discarding
     */
    private Card chosenCard;

    /**
     * Constructs a new PlayerController with default values
     */
    public PlayerController() {
        this.chosenCard = null;
    }

    /**
     * Establishes a bidirectional link with the game controller and retrieves
     * the room object for player management
     *
     * @param gameController The main controller for the game
     */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
        if (gameController.getRoomController() != null) {
            room = gameController.getRoomController().getRoom();
        }
    }

    /**
     * Initializes all players with random roles using the provided seed.
     * Creates specialized player instances based on assigned roles and
     * positions each player on their starting tile based on role color.
     *
     * @param seed The random seed for deterministic role assignment
     */
    public void initPlayers(long seed) {
        if (gameController == null) {
            System.err.println("GameController is null in PlayerController.initPlayers");
            return;
        }

        Island island = gameController.getIslandController().getIsland();
        if (island == null) {
            System.err.println("Island is null in PlayerController.initPlayers");
            return;
        }

        ArrayList<PlayerRole> roles = new ArrayList<>(Arrays.asList(PlayerRole.values()));
        Collections.shuffle(roles, new Random(seed));
        
        if (room == null) {
            System.err.println("Room is null in PlayerController.initPlayers");
            return;
        }

        int playerCount = room.getPlayers().size();
        List<Player> players = room.getPlayers();
        System.out.println("Initializing " + playerCount + " players");

        for (int i = 0; i < playerCount; i++) {
            Player player = players.get(i);
            PlayerRole role = roles.get(i);
            System.out.println("Initializing player " + player.getName() + " with role " + role);

            // 创建新的玩家实例
            Player newPlayer;
            switch (role) {
                case DIVER -> newPlayer = new Diver(player.getName());
                case ENGINEER -> newPlayer = new Engineer(player.getName());
                case EXPLORER -> newPlayer = new Explorer(player.getName());
                case MESSENGER -> newPlayer = new Messenger(player.getName());
                case NAVIGATOR -> newPlayer = new Navigator(player.getName());
                case PILOT -> newPlayer = new Pilot(player.getName());
                default -> {
                    System.err.println("Unknown role: " + role);
                    continue;
                }
            }

            // 设置初始位置
            String roleColor = PlayerRole.getColor(role);
            Tile startingTile = island.findTile(roleColor);
            Position startPosition;

            if (startingTile != null) {
                startPosition = startingTile.getPosition();
                System.out.println("Setting " + player.getName() + " to position " + startPosition);
            } else {
                // 如果找不到对应颜色的瓦片，寻找任何未沉没的瓦片
                System.out.println("Could not find starting tile for " + roleColor + ", looking for any non-sunk tile");
                startPosition = findAnyNonSunkPosition(island);
            }

            if (startPosition != null) {
                newPlayer.setPosition(startPosition);
            } else {
                System.err.println("Could not find any valid position for " + player.getName());
                newPlayer.setPosition(new Position(2, 2)); // 使用默认中心位置
            }

            // 更新玩家引用
            if (room.isHost(player.getName())) {
                room.setHostPlayer(newPlayer);
            }
            if (gameController.getCurrentPlayer() != null && 
                gameController.getCurrentPlayer().getName().equals(player.getName())) {
                gameController.setCurrentPlayer(newPlayer);
            }

            // 将新玩家添加到房间
            room.addPlayer(newPlayer);
        }

        // 移除原始玩家
        for (int i = 0; i < playerCount; i++) {
            room.getPlayers().remove(0);
        }

        // 更新玩家信息
        if (gameController != null) {
            gameController.updatePlayersInfo();
        }
    }

    private Position findAnyNonSunkPosition(Island island) {
        for (Map.Entry<Position, Tile> entry : island.getGameMap().entrySet()) {
            if (!entry.getValue().isSunk()) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Distributes initial treasure cards to all players.
     * Each player receives two non-water-rise cards.
     * Any water rise cards drawn are returned to the deck.
     *
     * @param treasureDeck The deck of treasure cards to draw from
     */
    public void dealCards(Deque<Card> treasureDeck) {
        for (Player player : room.getPlayers()) {
            while (player.getCards().size() < 2) {
                Card card = treasureDeck.poll();
                // Ensure the card is not WATER_RISE, otherwise put it back into the deck
                if (card != null && card.getType() != CardType.WATER_RISE) {
                    player.addCard(card);
                } else {
                    treasureDeck.add(card);
                }
            }
        }
    }

    /**
     * Determines if a player has a helicopter or sandbags special card that can be played.
     *
     * @param player The player to check for special card availability
     * @return true if the player has a playable special card, false otherwise
     */
    public boolean canPlaySpecialCard(Player player) {
        if (player == null) return false;

        // Check if player has any special cards
        for (Card card : player.getCards()) {
            if (card.getType() == CardType.HELICOPTER || card.getType() == CardType.SANDBAGS) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if a player has at least one flooded tile in range that can be shored up.
     *
     * @param player The player to check for shore-up capability
     * @return true if the player can shore up at least one tile, false otherwise
     */
    public boolean canShoreUpTile(Player player) {
        List<Position> validPositions = player.getShorePositions(gameController.getIsland().getGameMap());
        return !validPositions.isEmpty();
    }

    /**
     * Determines if a player can give a card to another player.
     * Messengers can give cards to any player, while other roles must share a tile with the recipient.
     *
     * @param player The player to check for card-giving capability
     * @return true if the player can give a card to at least one other player, false otherwise
     */
    public boolean canGiveCard(Player player) {
        if (player.getCards().isEmpty()) return false;
        if (player.getRole() == PlayerRole.MESSENGER) return true;

        // Other roles must be on the same tile
        Position playerPos = player.getPosition();
        if (playerPos == null) return false;
        for (Player tempPlayer : room.getPlayers()) {
            if (tempPlayer.equals(player)) continue;
            if (tempPlayer.getPosition().equals(playerPos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if a player can capture a treasure at their current location.
     * The player must be on a treasure tile and have four matching treasure cards.
     *
     * @param player The player to check for treasure-capturing capability
     * @return true if the player can capture a treasure, false otherwise
     */
    public boolean canCaptureTreasure(Player player) {
        Position playerPos = player.getPosition();
        Island island = gameController.getIsland();
        if (playerPos == null) return false;
        Tile currentTile = island.getTile(playerPos);
        if (currentTile == null) return false;

        // check if the player is on a treasure tile
        if (currentTile.getTreasureType() != null) {
            // check if the player has enough treasure cards
            int treasureCardCount = 0;
            for (Card card : player.getCards()) {
                if (card.getType() == CardType.TREASURE && card.getTreasureType() == currentTile.getTreasureType()) {
                    treasureCardCount++;
                }
            }
            return treasureCardCount >= 4;
        }
        return false;
    }

    /**
     * Gets the current game room containing all players
     *
     * @return The current Room object
     */
    public Room getRoom() {
        return room;
    }

    /**
     * Sets the currently selected card for actions like discarding
     *
     * @param chosenCard The card selected by the player
     */
    public void setChosenCard(Card chosenCard) {
        this.chosenCard = chosenCard;
    }

    /**
     * Gets the currently selected card
     *
     * @return The currently selected Card object
     */
    public Card getChosenCard() {
        return chosenCard;
    }

    /**
     * Checks if the current player has already drawn treasure cards during their turn
     *
     * @return true if the player has drawn treasure cards, false otherwise
     */
    public boolean hasDrawnTreasureCards() {
        if (room == null || room.getCurrentProgramPlayer() == null) {
            return false;
        }
        return room.getCurrentProgramPlayer().hasDrawnTreasureCards();
    }

    /**
     * Gets the number of flood cards drawn by the current player
     *
     * @return The number of flood cards drawn
     */
    public int getDrawnFloodCards() {
        if (room == null || room.getCurrentProgramPlayer() == null) {
            return 0;
        }
        return room.getCurrentProgramPlayer().getDrawFloodCards();
    }

    /**
     * Updates the flag indicating whether the current player has drawn treasure cards
     *
     * @param hasDrawnTreasureCards The new status to set
     */
    public void setHasDrawnTreasureCards(boolean hasDrawnTreasureCards) {
        if (room != null && room.getCurrentProgramPlayer() != null) {
            room.getCurrentProgramPlayer().setHasDrawnTreasureCards(hasDrawnTreasureCards);
        }
    }

    /**
     * Increases the count of flood cards drawn by the current player
     *
     * @param count The number of additional flood cards drawn
     */
    public void addDrawnFloodCards(int count) {
        if (room != null && room.getCurrentProgramPlayer() != null) {
            room.getCurrentProgramPlayer().addDrawnFloodCards(count);
        }
    }

    /**
     * Resets the current player's state at the beginning of a new turn
     */
    public void resetPlayerState() {
        if (room != null && room.getCurrentProgramPlayer() != null) {
            room.getCurrentProgramPlayer().resetState();
        }
    }

    /**
     * Cleans up resources when the game is shutting down
     */
    public void shutdown() {
        chosenCard = null;
        resetPlayerState();
    }

    public boolean canPerformAction(Player player) {
        return player != null && player.canPerformAction();
    }

    public void useAction(Player player) {
        if (player != null) {
            player.useAction();
        }
    }

    public void movePlayer(Player player, Position position) {
        if (player != null && position != null) {
            player.move(position);
        }
    }
}

