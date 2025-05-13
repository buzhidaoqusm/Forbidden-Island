package com.island.model;

import java.util.*;

public class GameStateManager {
    private static GameStateManager instance;
    private int waterLevel;
    private List<Card> floodDeck;
    private List<Card> floodDiscardPile;
    private List<Card> treasureDiscardPile;
    private List<Card> treasureDeck;
    private Map<Position, Tile> gameMap;
    private List<Player> players;
    private GameState gameState;
    private Position foolsLandingPosition;
    private static final int MAX_WATER_LEVEL = 10;
    private Player currentPlayer;
    private int remainingActions;
    private static final int MAX_ACTIONS = 3;
    private Island island;

    private GameStateManager() {
        this.waterLevel = 1;
        this.floodDeck = new ArrayList<>();
        this.floodDiscardPile = new ArrayList<>();
        this.treasureDiscardPile = new ArrayList<>();
        this.treasureDeck = new ArrayList<>();
        this.gameMap = new HashMap<>();
        this.players = new ArrayList<>();
        this.gameState = GameState.ONGOING;
        this.remainingActions = MAX_ACTIONS;
        this.island = new Island();
    }

    public static GameStateManager getInstance() {
        if (instance == null) {
            instance = new GameStateManager();
        }
        return instance;
    }

    public void handleWaterRise() {
        increaseWaterLevel();
        reshuffleFloodDiscardPile();
        checkGameState();
    }

    public void handleHelicopterLift(Player player) {
        // 检查是否所有玩家都在同一个非沉没板块上
        Position currentPos = player.getPosition();
        boolean allPlayersPresent = players.stream()
                .allMatch(p -> p.getPosition().equals(currentPos));
        
        if (allPlayersPresent) {
            // 允许选择目标位置
            List<Position> validPositions = getValidHelicopterDestinations();
            // 具体移动逻辑由UI层处理
        }
    }

    public void handleSandbag(Player player) {
        List<Position> floodedPositions = new ArrayList<>();
        Position playerPos = player.getPosition();
        
        // 获取周围的被淹没板块
        for (Map.Entry<Position, Tile> entry : gameMap.entrySet()) {
            if (entry.getValue().isFlooded() && isAdjacent(playerPos, entry.getKey())) {
                floodedPositions.add(entry.getKey());
            }
        }
        
        // 具体选择逻辑由UI层处理
    }

    public void drawFloodCards() {
        int cardsToDraw = getFloodCardsDrawCount();
        for (int i = 0; i < cardsToDraw; i++) {
            if (!floodDeck.isEmpty()) {
                Card card = floodDeck.remove(0);
                handleFloodCard((FloodCard) card);
                floodDiscardPile.add(card);
            }
        }
        checkGameState();
    }

    private void handleFloodCard(FloodCard card) {
        Position pos = card.getFloodPosition();
        Tile tile = gameMap.get(pos);
        if (tile != null) {
            tile.flood();
            if (tile.isSunk()) {
                checkPlayersOnSunkenTile(pos);
            }
        }
    }

    private void checkPlayersOnSunkenTile(Position pos) {
        for (Player player : players) {
            if (player.getPosition().equals(pos)) {
                // 玩家需要游到相邻的板块，否则游戏失败
                if (!canPlayerSwim(player)) {
                    gameState = GameState.DEFEAT_WATER_LEVEL;
                }
            }
        }
    }

    private boolean canPlayerSwim(Player player) {
        Position pos = player.getPosition();
        return getAdjacentTiles(pos).stream()
                .anyMatch(tile -> !tile.isSunk());
    }

    private List<Tile> getAdjacentTiles(Position pos) {
        List<Tile> adjacent = new ArrayList<>();
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, 1, 0, -1};
        
        for (int i = 0; i < 4; i++) {
            Position newPos = new Position(pos.getX() + dx[i], pos.getY() + dy[i]);
            Tile tile = gameMap.get(newPos);
            if (tile != null) {
                adjacent.add(tile);
            }
        }
        return adjacent;
    }

    private boolean isAdjacent(Position pos1, Position pos2) {
        int dx = Math.abs(pos1.getX() - pos2.getX());
        int dy = Math.abs(pos1.getY() - pos2.getY());
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    public void checkGameState() {
        // 检查胜利条件
        if (checkVictoryCondition()) {
            gameState = GameState.VICTORY;
            return;
        }

        // 检查失败条件
        if (waterLevel >= MAX_WATER_LEVEL) {
            gameState = GameState.DEFEAT_WATER_LEVEL;
            return;
        }

        if (checkTreasureLostCondition()) {
            gameState = GameState.DEFEAT_TREASURE_LOST;
            return;
        }

        if (isFoolsLandingSunk()) {
            gameState = GameState.DEFEAT_FOOLS_LANDING_SUNK;
        }

        // 检查玩家是否被困
        if (arePlayersTrapped()) {
            gameState = GameState.DEFEAT_PLAYERS_TRAPPED;
        }
    }

    private boolean checkVictoryCondition() {
        // 检查是否收集了所有宝藏
        Set<TreasureType> collectedTreasures = new HashSet<>();
        for (Player player : players) {
            collectedTreasures.addAll(player.getCapturedTreasures());
        }
        
        // 检查是否所有玩家都到达了愚者降临点且收集了所有宝藏
        boolean allTreasuresCollected = collectedTreasures.size() == TreasureType.values().length - 1; // 减去NONE
        boolean allPlayersOnFoolsLanding = players.stream()
                .allMatch(p -> p.getPosition().equals(foolsLandingPosition));
        
        return allTreasuresCollected && allPlayersOnFoolsLanding;
    }

    private boolean checkTreasureLostCondition() {
        // 检查每种宝藏类型的板块是否都还有可用的
        for (TreasureType type : TreasureType.values()) {
            if (type == TreasureType.NONE) continue;
            
            boolean treasureAvailable = false;
            for (Tile tile : gameMap.values()) {
                if (tile.getTreasureType() == type && !tile.isSunk()) {
                    treasureAvailable = true;
                    break;
                }
            }
            
            if (!treasureAvailable) {
                return true;
            }
        }
        return false;
    }

    private boolean isFoolsLandingSunk() {
        Tile foolsLanding = gameMap.get(foolsLandingPosition);
        return foolsLanding != null && foolsLanding.isSunk();
    }

    // 检查玩家是否被困
    private boolean arePlayersTrapped() {
        for (Player player : players) {
            if (!canPlayerMove(player)) {
                return true;
            }
        }
        return false;
    }

    // 检查玩家是否可以移动
    private boolean canPlayerMove(Player player) {
        Position pos = player.getPosition();
        return getAdjacentTiles(pos).stream()
                .anyMatch(tile -> !tile.isSunk());
    }

    // 获取有效的直升机目标位置
    public List<Position> getValidHelicopterDestinations() {
        List<Position> validPositions = new ArrayList<>();
        for (Map.Entry<Position, Tile> entry : gameMap.entrySet()) {
            if (!entry.getValue().isSunk()) {
                validPositions.add(entry.getKey());
            }
        }
        return validPositions;
    }

    // 获取当前游戏状态
    public GameState getGameState() {
        return gameState;
    }

    // 获取当前水位
    public int getWaterLevel() {
        return waterLevel;
    }

    // 获取当前玩家
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    // 获取剩余行动点数
    public int getRemainingActions() {
        return remainingActions;
    }

    // 获取岛屿地图
    public Map<Position, Tile> getGameMap() {
        return Collections.unmodifiableMap(gameMap);
    }

    // 获取玩家列表
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    // 获取 Fool's Landing 位置
    public Position getFoolsLandingPosition() {
        return foolsLandingPosition;
    }

    // 游戏初始化
    public void initializeGame(List<Player> players, long seed) {
        this.players = new ArrayList<>(players);
        this.currentPlayer = players.get(0);
        initializeIsland(seed);
        initializeDecks(seed);
        distributeInitialCards();
    }

    // 初始化岛屿
    private void initializeIsland(long seed) {
        // 创建岛屿布局
        createIslandLayout();
        // 设置初始水位
        setWaterLevel(1);
        // 记录 Fool's Landing 位置
        for (Map.Entry<Position, Tile> entry : gameMap.entrySet()) {
            if (entry.getValue().getName().equals("Fool's Landing")) {
                foolsLandingPosition = entry.getKey();
                break;
            }
        }
    }

    // 初始化牌组
    private void initializeDecks(long seed) {
        // 初始化洪水牌组
        initializeFloodDeck(seed);
        // 初始化宝藏牌组
        initializeTreasureDeck(seed);
    }

    // 分发初始卡牌
    private void distributeInitialCards() {
        for (Player player : players) {
            for (int i = 0; i < 2; i++) {
                if (!treasureDeck.isEmpty()) {
                    Card card = treasureDeck.remove(0);
                    if (card.getType() != CardType.WATER_RISE) {
                        player.addCard(card);
                    } else {
                        treasureDeck.add(card);
                    }
                }
            }
        }
    }

    // 处理玩家回合
    public void handlePlayerTurn(Player player) {
        currentPlayer = player;
        remainingActions = MAX_ACTIONS;
        player.startTurn();
    }

    // 处理玩家移动
    public boolean handlePlayerMove(Player player, Position newPosition) {
        if (remainingActions <= 0) {
            return false;
        }

        if (isValidMove(player, newPosition)) {
            player.setPosition(newPosition);
            remainingActions--;
            return true;
        }
        return false;
    }

    // 处理加固板块
    public boolean handleShoreUp(Player player, Position position) {
        if (remainingActions <= 0) {
            return false;
        }

        Tile tile = gameMap.get(position);
        if (tile != null && tile.isFlooded() && isAdjacent(player.getPosition(), position)) {
            tile.shoreUp();
            remainingActions--;
            return true;
        }
        return false;
    }

    // 处理收集宝藏
    public boolean handleTreasureCapture(Player player, TreasureType treasureType) {
        if (remainingActions <= 0) {
            return false;
        }

        if (canCaptureTreasure(player, treasureType)) {
            player.addCaptureTreasure(treasureType);
            remainingActions--;
            return true;
        }
        return false;
    }

    // 处理抽牌
    public void handleDrawCards(Player player, int count) {
        for (int i = 0; i < count; i++) {
            if (!treasureDeck.isEmpty()) {
                Card card = treasureDeck.remove(0);
                if (card.getType() == CardType.WATER_RISE) {
                    handleWaterRise();
                    treasureDeck.add(card);
                } else {
                    player.addCard(card);
                }
            }
        }
    }

    // 增加水位
    private void increaseWaterLevel() {
        if (waterLevel < MAX_WATER_LEVEL) {
            waterLevel++;
        }
    }

    // 重新洗牌洪水弃牌堆
    private void reshuffleFloodDiscardPile() {
        floodDeck.addAll(floodDiscardPile);
        floodDiscardPile.clear();
        Collections.shuffle(floodDeck);
    }

    // 获取当前水位需要抽取的洪水卡牌数量
    private int getFloodCardsDrawCount() {
        return waterLevel;
    }

    // 设置水位
    private void setWaterLevel(int level) {
        if (level >= 1 && level <= MAX_WATER_LEVEL) {
            this.waterLevel = level;
        }
    }

    // 创建岛屿布局
    private void createIslandLayout() {
        // 创建6x6的岛屿布局
        for (int x = 0; x < 6; x++) {
            for (int y = 0; y < 6; y++) {
                // 跳过角落位置（这些位置是空的）
                if ((x == 0 || x == 5) && (y == 0 || y == 5)) {
                    continue;
                }
                // 创建板块并添加到地图中
                Position pos = new Position(x, y);
                String tileName = getTileName(x, y);
                TreasureType treasureType = getTileTreasureType(x, y);
                Tile tile = new Tile(tileName, pos, treasureType);
                gameMap.put(pos, tile);
            }
        }
    }

    // 根据位置获取板块名称
    private String getTileName(int x, int y) {
        // 这里需要根据游戏规则实现具体的板块名称分配
        // 示例实现
        if (x == 2 && y == 2) return "Fool's Landing";
        if (x == 1 && y == 1) return "Bronze Gate";
        if (x == 1 && y == 4) return "Cave of Shadows";
        if (x == 2 && y == 1) return "Coral Palace";
        if (x == 2 && y == 4) return "Tidal Palace";
        if (x == 3 && y == 1) return "Cave of Embers";
        if (x == 3 && y == 4) return "Temple of the Moon";
        if (x == 4 && y == 1) return "Whispering Garden";
        if (x == 4 && y == 4) return "Howling Garden";
        return "Unknown Tile";
    }

    // 根据位置获取宝藏类型
    private TreasureType getTileTreasureType(int x, int y) {
        // 根据游戏规则实现具体的宝藏类型分配
        if ((x == 1 && y == 1) || (x == 1 && y == 4)) return TreasureType.EARTH_STONE;
        if ((x == 2 && y == 1) || (x == 2 && y == 4)) return TreasureType.OCEAN_CHALICE;
        if ((x == 3 && y == 1) || (x == 3 && y == 4)) return TreasureType.FIRE_CRYSTAL;
        if ((x == 4 && y == 1) || (x == 4 && y == 4)) return TreasureType.WIND_STATUE;
        return TreasureType.NONE;
    }

    // 初始化洪水牌组
    private void initializeFloodDeck(long seed) {
        // 为每个板块创建一张洪水卡牌
        for (Map.Entry<Position, Tile> entry : gameMap.entrySet()) {
            Tile tile = entry.getValue();
            floodDeck.add(Card.createFloodCard(tile.getName(), entry.getKey(), null));  // 使用工厂方法创建洪水卡
        }
        // 使用种子洗牌
        Collections.shuffle(floodDeck, new Random(seed));
    }

    // 初始化宝藏牌组
    private void initializeTreasureDeck(long seed) {
        // 为每种宝藏类型创建对应的卡牌
        for (TreasureType type : TreasureType.values()) {
            if (type != TreasureType.NONE) {
                // 每种宝藏类型创建3张卡牌
                for (int i = 0; i < 3; i++) {
                    treasureDeck.add(Card.createTreasureCard(type, null));  // 使用工厂方法创建宝藏卡
                }
            }
        }
        // 添加水位上升卡牌
        for (int i = 0; i < 3; i++) {
            treasureDeck.add(Card.createSpecialCard(CardType.WATER_RISE));  // 使用工厂方法创建水位上升卡
        }
        // 使用种子洗牌
        Collections.shuffle(treasureDeck, new Random(seed));
    }

    // 检查移动是否有效
    private boolean isValidMove(Player player, Position newPosition) {
        // 检查目标位置是否存在且未被淹没
        Tile targetTile = gameMap.get(newPosition);
        if (targetTile == null || targetTile.isSunk()) {
            return false;
        }

        // 检查是否相邻
        return isAdjacent(player.getPosition(), newPosition);
    }

    // 检查是否可以收集宝藏
    private boolean canCaptureTreasure(Player player, TreasureType treasureType) {
        // 检查玩家位置是否有对应类型的宝藏
        Tile currentTile = gameMap.get(player.getPosition());
        if (currentTile == null || currentTile.getTreasureType() != treasureType) {
            return false;
        }

        // 检查玩家是否有足够的对应类型卡牌
        int requiredCards = 4;
        int playerCards = (int) player.getCards().stream()
                .filter(card -> card instanceof TreasureCard && 
                        ((TreasureCard) card).getTreasureType() == treasureType)
                .count();

        return playerCards >= requiredCards;
    }

    // 获取岛屿实例
    public Island getIsland() {
        return island;
    }

    // 其他必要的 getter 和 setter 方法
}