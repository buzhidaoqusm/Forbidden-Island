package com.forbiddenisland.views.game;

import com.forbiddenisland.controllers.game.GameController;
import com.forbiddenisland.models.adventurers.Player;
import com.forbiddenisland.models.game.GameState;
import com.forbiddenisland.models.island.Position;
import com.forbiddenisland.utils.observer.GameObserver;
import com.forbiddenisland.views.ui.ActionBarView;
import com.forbiddenisland.views.ui.ActionLogView;
import com.forbiddenisland.views.ui.CardView;
import com.forbiddenisland.views.ui.IslandView;
import com.forbiddenisland.views.ui.MenuView;
import com.forbiddenisland.views.ui.PlayerView;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class GameView implements GameObserver {
    private Scene scene;
    private Stage primaryStage;
    private GameController gameController;

    private IslandView islandView;
    private GridPane boardGrid;
    private VBox waterLevelBox;

    private PlayerView playerView;
    private VBox playersInfoBox; // 玩家信息区域

    private CardView cardView;
    private VBox cardsInfoBox; // 卡牌信息区域

    private ActionLogView actionLogView; // 添加日志视图

    private ActionBarView actionBarView;
    private HBox actionBar; // 操作栏

    public GameView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        boardGrid = new GridPane();
        waterLevelBox = new VBox(10);
        playersInfoBox = new VBox(10);
        cardsInfoBox = new VBox(20);
        actionBar = new HBox(10);

        islandView = new IslandView(boardGrid, waterLevelBox);
        playerView = new PlayerView(playersInfoBox);
        cardView = new CardView(cardsInfoBox);
        actionLogView = new ActionLogView(); // 创建日志视图
        actionBarView = new ActionBarView(actionBar); // 创建操作栏视图
    }

    public void initGame() {
        gameController.getMessageHandler().setActionLogView(actionLogView); // 设置日志视图

        islandView.initializeBoard();
        islandView.initWaterLevel();
        playerView.initPlayersInfo();
        cardView.initializeFloodCardsInfo();
        cardView.initializeTreasureCardsInfo();
        actionBarView.initActionButtons();

        // 创建主布局（使用BorderPane）
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Set background to root
        root.setBackground(islandView.getBackground());

        // 创建水平布局容器（用于放置棋盘和水位条）
        HBox topLayout = new HBox(20);
        topLayout.setAlignment(Pos.CENTER);
        actionLogView.setAlignment(Pos.CENTER_RIGHT);
        waterLevelBox.setAlignment(Pos.CENTER);
        boardGrid.setStyle("-fx-background-color: transparent;");
        topLayout.getChildren().addAll(boardGrid, waterLevelBox, actionLogView); // 添加日志视图

        VBox contentLayout = new VBox(20);
        contentLayout.setAlignment(Pos.TOP_CENTER);
        // 将玩家信息区域和卡牌信息区域改成半透明
        playersInfoBox.setStyle("-fx-background-color: rgba(240, 240, 240, 0.8); -fx-border-color: #cccccc; -fx-border-width: 1px;");
        cardsInfoBox.setStyle("-fx-background-color: rgba(240, 240, 240, 0.8); -fx-border-color: #cccccc; -fx-border-width: 1px;");
        contentLayout.getChildren().addAll(topLayout, playersInfoBox, cardsInfoBox);

        ScrollPane scrollPane = new ScrollPane(contentLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background: transparent;" +
                        "-fx-border-color: transparent;"
        );
        // This is the important part - making the viewport transparent
        scrollPane.getStyleClass().add("transparent-viewport");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        root.setCenter(scrollPane);

        // 将操作栏添加到顶部
        root.setTop(actionBar);
        scene = new Scene(root, 1000, 800);
    }
    
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
        islandView.setIslandController(gameController.getIslandController());
        playerView.setPlayerController(gameController.getPlayerController());
        cardView.setCardController(gameController.getCardController());
        actionBarView.setActionBarController(gameController.getActionBarController());
        
        // 注册为观察者
        gameController.getGameSubject().addObserver(this);
    }

    public Scene getScene() {
        return scene;
    }

    public IslandView getIslandView() {
        return islandView;
    }

    public void setPrimaryStage() {
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * 获取主窗口Stage
     * @return 主窗口Stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void addLog(String message) {
        actionLogView.addLog(message);
    }

    // 以下是GameObserver接口的实现方法
    @Override
    public void onGameStateChanged(GameState state) {
        Platform.runLater(() -> {
            // 处理游戏状态变化
            if (state == GameState.GAME_OVER) {
                // 游戏结束处理逻辑
                returnToMainMenu();
            } else if (state == GameState.TURN_START) {
                // 回合开始处理逻辑
                updateActionBar();
            }
        });
    }

    @Override
    public void onBoardChanged() {
        Platform.runLater(() -> {
            islandView.initializeBoard();
        });
    }

    @Override
    public void onPlayerMoved(Player player, Position newPosition) {
        Platform.runLater(() -> {
            // 玩家移动更新
            islandView.initializeBoard();
        });
    }

    @Override
    public void onWaterLevelChanged(int newLevel) {
        Platform.runLater(() -> {
            islandView.initWaterLevel();
        });
    }

    @Override
    public void onCardChanged() {
        Platform.runLater(() -> {
            cardView.initializeFloodCardsInfo();
            cardView.initializeTreasureCardsInfo();
        });
    }

    @Override
    public void onPlayerInfoChanged() {
        Platform.runLater(() -> {
            playerView.initPlayersInfo();
        });
    }

    @Override
    public void onActionBarChanged() {
        Platform.runLater(() -> {
            actionBarView.updateActionBar();
        });
    }

    public void updateActionBar() {
        actionBarView.updateActionBar();
    }

    public void returnToMainMenu() {
        Platform.runLater(() -> {
            try {
                MenuView mainMenuView = new MenuView();
                Scene menuScene = mainMenuView.getMenuScene(primaryStage, gameController.getRoom().getCurrentProgramPlayer());

                if (menuScene != null) {
                    primaryStage.setScene(menuScene);
                    primaryStage.show();
                } else {
                    System.err.println("Menu scene is null");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error returning to main menu: " + e.getMessage());
            }
        });
    }
}
