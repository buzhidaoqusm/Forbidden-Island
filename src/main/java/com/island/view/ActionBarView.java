package com.island.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.ListView;
import javafx.application.Platform;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.island.controller.GameController;
import com.island.controller.ActionBarController;
import com.island.model.Player;
import com.island.view.ActionType;

public class ActionBarView {

    private BorderPane viewPane; // 使用BorderPane作为根容器
    private HBox actionBarControls; // HBox for the action buttons and label
    private ListView<String> logListView; // For displaying game log messages
    private VBox logPane; // Container for the log title and list view
    private Label actionsRemainingLabel;
    private Button moveButton;
    private Button shoreUpButton;
    private Button giveCardButton;
    private Button captureTreasureButton;
    private Button useAbilityButton;
    private Button endTurnButton;
    
    // 水位计相关控件
    private ImageView waterMeterImageView;
    private Map<Integer, Image> waterMeterImages = new HashMap<>();
    private VBox waterMeterPane;

    // GameController reference
    private GameController gameController;

    // Constructor
    public ActionBarView(GameController gameController) {
        this.gameController = gameController;
        loadWaterMeterImages();
        initialize();
    }
    
    /**
     * 加载水位计的图片资源
     */
    private void loadWaterMeterImages() {
        try {
            // 加载所有的水位计图片（0到10）
            for (int i = 0; i <= 10; i++) {
                String path = "/WaterMeter/" + i + ".png";
                waterMeterImages.put(i, new Image(getClass().getResourceAsStream(path)));
            }
        } catch (Exception e) {
            System.err.println("Error loading water meter images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initialize() {
        // 使用BorderPane作为主容器
        viewPane = new BorderPane();
        viewPane.setStyle("-fx-background-color: #d3d3d3;"); // Grey background
        
        // --- Initialize Action Bar Controls --- 
        actionBarControls = new HBox(10); // Spacing between action elements
        actionBarControls.setPadding(new Insets(5, 10, 5, 10)); // Reduced padding for controls
        actionBarControls.setAlignment(Pos.CENTER_LEFT);

        actionsRemainingLabel = new Label("行动点: ?");
        actionsRemainingLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        moveButton = new Button("移动");
        shoreUpButton = new Button("加固");
        giveCardButton = new Button("交换卡牌");
        captureTreasureButton = new Button("获取宝藏");
        useAbilityButton = new Button("使用能力");
        endTurnButton = new Button("结束回合");

        // --- Button Actions (Send requests to Controller) ---
        moveButton.setOnAction(event -> {
            System.out.println("Move button clicked");
            if (gameController != null) gameController.getActionBarController().handleMoveAction();
        });
        shoreUpButton.setOnAction(event -> {
            System.out.println("Shore Up button clicked");
            if (gameController != null) gameController.getActionBarController().handleShoreUpAction();
        });
        giveCardButton.setOnAction(event -> {
            System.out.println("Give Card button clicked");
            if (gameController != null) gameController.getActionBarController().handleGiveCardAction();
        });
        captureTreasureButton.setOnAction(event -> {
            System.out.println("Capture Treasure button clicked");
            if (gameController != null) gameController.getActionBarController().handleCaptureTreasureAction();
        });
        useAbilityButton.setOnAction(event -> {
            System.out.println("Use Ability button clicked");
            if (gameController != null) gameController.getActionBarController().handlePlaySpecialAction();
        });
        endTurnButton.setOnAction(event -> {
            System.out.println("End Turn button clicked");
            if (gameController != null) gameController.getActionBarController().handleEndTurnAction();
        });

        actionBarControls.getChildren().addAll(
            actionsRemainingLabel,
            moveButton,
            shoreUpButton,
            giveCardButton,
            captureTreasureButton,
            useAbilityButton,
            endTurnButton
        );

        // --- Initialize Log View --- 
        logPane = new VBox(2); // Reduce spacing
        logPane.setPadding(new Insets(2, 10, 2, 10)); // Reduced padding for log area

        Label logTitle = new Label("游戏日志");
        logTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12)); // Smaller font

        logListView = new ListView<>();
        logListView.setPrefHeight(40); // Significantly reduced height
        logListView.setMaxHeight(40); // Enforce maximum height
        logListView.setMouseTransparent(true); // Make read-only
        logListView.setFocusTraversable(false);
        // Make the log list view more compact
        logListView.setStyle("-fx-font-size: 11px; -fx-cell-size: 16px;");

        logPane.getChildren().addAll(logTitle, logListView);
        
        // --- 初始化水位计显示 ---
        waterMeterPane = new VBox(2); // 减少间距
        waterMeterPane.setPadding(new Insets(2)); // 减少内边距
        waterMeterPane.setAlignment(Pos.CENTER);
        waterMeterPane.setMaxWidth(80); // 限制最大宽度
        
        Label waterMeterTitle = new Label("水位计");
        waterMeterTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12)); // 减小字体大小
        
        // 创建水位计图像视图，默认显示0级
        waterMeterImageView = new ImageView();
        waterMeterImageView.setFitWidth(70);  // 减小宽度
        waterMeterImageView.setFitHeight(120); // 减小高度
        waterMeterImageView.setPreserveRatio(true);
        
        // 设置默认水位计图片
        if (waterMeterImages.containsKey(0)) {
            waterMeterImageView.setImage(waterMeterImages.get(0));
        }
        
        waterMeterPane.getChildren().addAll(waterMeterTitle, waterMeterImageView);

        // --- 设置布局 ---
        // 创建一个更紧凑的布局
        BorderPane topSection = new BorderPane();
        topSection.setCenter(actionBarControls); // 中间放置动作按钮
        topSection.setRight(waterMeterPane);     // 右边放置水位计
        
        viewPane.setTop(topSection);             // 顶部放置按钮和水位计
        viewPane.setBottom(logPane);             // 底部放置日志（更紧凑）
        
        // 设置边距
        BorderPane.setMargin(actionBarControls, new Insets(5));
        BorderPane.setMargin(logPane, new Insets(5));
        BorderPane.setMargin(waterMeterPane, new Insets(5));

        // Initially disable all action buttons until updated
        setAvailableActions(new ArrayList<>(), 0);
        
        // 初始化水位计
        updateWaterLevelIndicator(0);
    }

    public void updateActions(List<Object> availableActions, int actionsRemaining) {
        Platform.runLater(() -> {
            actionsRemainingLabel.setText("行动点: " + actionsRemaining);
            setAvailableActions(availableActions, actionsRemaining);
        });
    }

    private void setAvailableActions(List<Object> availableActions, int actionsRemaining) {
        boolean canAct = actionsRemaining > 0;
        
        if (gameController != null) {
            ActionBarController actionBarController = gameController.getActionBarController();
            Player currentPlayer = actionBarController.getCurrentPlayer();
            
            // 检查当前玩家是否为空
            if (currentPlayer == null) {
                // 如果没有当前玩家，禁用所有按钮
                moveButton.setDisable(true);
                shoreUpButton.setDisable(true);
                giveCardButton.setDisable(true);
                captureTreasureButton.setDisable(true);
                useAbilityButton.setDisable(true);
                return;
            }
            
            // Enable/disable buttons based on game state and player abilities
            moveButton.setDisable(!(canAct));
            shoreUpButton.setDisable(!(canAct && actionBarController.canShoreUpTile(currentPlayer)));
            giveCardButton.setDisable(!(canAct && actionBarController.canGiveCard(currentPlayer)));
            captureTreasureButton.setDisable(!(canAct && actionBarController.canCaptureTreasure(currentPlayer)));
            useAbilityButton.setDisable(!actionBarController.canPlaySpecialCard(currentPlayer)); // Special abilities may have different rules
            
            // End Turn is always available after drawing treasure cards
            endTurnButton.setDisable(!actionBarController.hasDrawnTreasureCards());
        } else {
            // Fallback if controller is not available
            moveButton.setDisable(!canAct);
            shoreUpButton.setDisable(!canAct);
            giveCardButton.setDisable(!canAct);
            captureTreasureButton.setDisable(!canAct);
            useAbilityButton.setDisable(!canAct);
            endTurnButton.setDisable(false);
        }

        // Highlight active button (optional visual feedback)
        clearHighlights();
    }

    public void clearHighlights() {
        moveButton.setStyle(null);
        shoreUpButton.setStyle(null);
        giveCardButton.setStyle(null);
        captureTreasureButton.setStyle(null);
        useAbilityButton.setStyle(null);
        // endTurnButton usually doesn't need highlighting
    }

    public Pane getView() {
        return viewPane;
    }

    // --- Log Methods (migrated from ActionLogView) ---
    public void addLogMessage(String logMessage) {
        Platform.runLater(() -> {
            if (logListView != null) {
                logListView.getItems().add(logMessage);
                // Auto-scroll to the latest message
                logListView.scrollTo(logListView.getItems().size() - 1);
            }
        });
    }

    public void clearLog() {
        Platform.runLater(() -> {
            if (logListView != null) {
                logListView.getItems().clear();
            }
        });
    }
    
    /**
     * Update the action bar view, get the latest action status information from GameController
     */
    public void update() {
        if (gameController != null) {
            try {
                // Get the current player's remaining action points
                int actionsRemaining = gameController.getRemainingActions();
                
                // Get available actions list
                // Since getAvailableActions method might not exist, use an empty list
                List<Object> availableActions = new ArrayList<>(); 
                
                // Update action button states
                updateActions(availableActions, actionsRemaining);
                
                // Update water level indicator if the method exists
                try {
                    int waterLevel = gameController.getIslandController().getWaterLevel();
                    updateWaterLevelIndicator(waterLevel);
                } catch (Exception e) {
                    System.err.println("Error updating water level: " + e.getMessage());
                }
                
                System.out.println("Action bar view updated");
            } catch (Exception e) {
                System.err.println("Error updating action bar view: " + e.getMessage());
            }
        }
    }
    
    /**
     * 更新水位计显示
     * @param waterLevel 当前水位等级（0-10）
     */
    public void updateWaterLevelIndicator(int waterLevel) {
        Platform.runLater(() -> {
            // 确保水位值在有效范围内
            int level = Math.min(Math.max(waterLevel, 0), 10);
            
            // 更新水位计图片
            if (waterMeterImages.containsKey(level)) {
                waterMeterImageView.setImage(waterMeterImages.get(level));
            }
            
            // 打印日志
            System.out.println("水位更新为: " + level);
        });
    }
}
