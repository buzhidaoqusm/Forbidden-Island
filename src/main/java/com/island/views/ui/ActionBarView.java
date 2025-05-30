package com.island.views.ui;

import com.island.controller.ActionBarController;
import com.island.models.Room;
import com.island.models.adventurers.Player;
import com.island.models.adventurers.PlayerRole;
import com.island.models.island.Tile;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ActionBarView {
    private ActionBarController actionBarController;

    private HBox actionBar; // 操作栏
    private Label actionLabel; // 操作提示文本
    private HBox actionButtons; // 操作按钮容器
    private Button moveButton;
    private Button shoreUpButton;
    private Button endTurnButton;
    private Button playSpecialButton;
    private Button drawFloodButton;
    private Button giveCardButton; // 新增给卡牌按钮
    private Button moveOtherPlayerButton; // 新增移动其他玩家按钮
    private Button captureTreasureButton; // 新增收集宝藏按钮
    private Button discardButton; // 新增弃牌按钮

    public ActionBarView(HBox actionBar) {
        this.actionBar = actionBar;
        this.actionBar.setAlignment(Pos.CENTER_LEFT);
        this.actionBar.setPadding(new Insets(10));
        this.actionBar.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #cccccc; -fx-border-width: 1px;");
    }

    public void initActionButtons() {
        actionLabel = new Label();
        actionLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        actionButtons = new HBox(5);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);

        // 初始化按钮
        moveButton = createActionButton("Move");
        shoreUpButton = createActionButton("Shore Up");
        giveCardButton = createActionButton("Give Card");
        moveOtherPlayerButton = createActionButton("Move Other");
        captureTreasureButton = createActionButton("Capture Treasure");
        endTurnButton = createActionButton("End Turn");
        playSpecialButton = createActionButton("Play Special");
        drawFloodButton = createActionButton("Draw Flood");
        discardButton = createActionButton("Discard");
        discardButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");

        // 设置按钮事件
        moveButton.setOnAction(e -> actionBarController.handleMoveAction());
        shoreUpButton.setOnAction(e ->actionBarController. handleShoreUpAction());
        giveCardButton.setOnAction(e -> actionBarController.handleGiveCardAction());
        moveOtherPlayerButton.setOnAction(e -> actionBarController.handleMoveOtherPlayerAction());
        captureTreasureButton.setOnAction(e -> actionBarController.handleCaptureTreasureAction());
        endTurnButton.setOnAction(e -> actionBarController.handleEndTurnAction());
        playSpecialButton.setOnAction(e -> actionBarController.handlePlaySpecialAction());
        drawFloodButton.setOnAction(e -> actionBarController.handleDrawFloodAction());
        discardButton.setOnAction(e -> actionBarController.handleDiscardAction());

        // 将标签和按钮容器添加到操作栏
        actionBar.getChildren().addAll(actionLabel, new Region(), actionButtons);
        HBox.setHgrow(actionBar.getChildren().get(1), Priority.ALWAYS);

        updateActionBar();
    }

    /**
     * 更新操作栏状态
     */
    public void updateActionBar() {
        if (actionButtons == null) {
            return;
        }
        actionButtons.getChildren().clear();
        if (actionBarController.getGameController().isGameOver()) {
            actionLabel.setText("Game Over!");
            return;
        }
        Player currentPlayer = actionBarController.getCurrentPlayer();
        Player currentProgramPlayer = actionBarController.getRoom().getCurrentProgramPlayer();
        int remainingActions = actionBarController.getRemainingActions();
        Room room = actionBarController.getRoom();
        // Check if current player exists
        if (currentPlayer == null) {
            actionLabel.setText("Waiting for game to start...");
            return;
        }

        // Update action label with player info
        actionLabel.setText("Current Player: " + currentPlayer.getName() +
                " | Actions: " + remainingActions);

//        Button testButton = createActionButton("Test");
//        actionButtons.getChildren().add(testButton);
//        testButton.setOnAction(e -> {
//            actionBarController.getGameController().receiveMessage = !actionBarController.getGameController().receiveMessage;
//            System.out.println(currentProgramPlayer.getName() + " can not receive message");
//        });

        if (actionBarController.isAnyPlayerSunk()) {
            if (actionBarController.getIsland().getTile(currentProgramPlayer.getPosition()).getState() == Tile.TileState.SUNK) {
                actionLabel.setText("You are sunk! Please choose a tile to swim to.");
                actionBarController.handlePlayerSunk(currentProgramPlayer);
                actionButtons.getChildren().add(moveButton);
            } else {
                actionLabel.setText("Someone is sunk! Please wait for them to swim to a tile.");
            }
        } else {
            if (currentPlayer.getName().equals(room.getCurrentProgramPlayer().getName())) {
                if (currentPlayer.getCards().size() > 5) {
                    // 设置提示dialog
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Discard Card");
                    alert.setHeaderText("You have more than 5 cards, please discard one or play a special card.");
                    alert.show();

                    actionButtons.getChildren().add(discardButton);
                    if (actionBarController.canPlaySpecialCard(currentPlayer)) {
                        actionButtons.getChildren().add(playSpecialButton);
                    }
                } else {
                    // 当前玩家的回合
                    if (remainingActions > 0) {
                        // 还有剩余行动次数
                        actionLabel.setText("You may take " + remainingActions + " action(s)");
                        actionButtons.getChildren().add(moveButton);

                        // 检查能否加固地板
                        if (actionBarController.canShoreUpTile(currentPlayer)) {
                            actionButtons.getChildren().add(shoreUpButton);
                        }

                        if (actionBarController.canGiveCard(currentPlayer)) {
                            actionButtons.getChildren().add(giveCardButton);
                        }

                        // 如果是领航员，添加移动其他玩家按钮
                        if (currentPlayer.getRole() == PlayerRole.NAVIGATOR) {
                            actionButtons.getChildren().add(moveOtherPlayerButton);
                        }

                        if (actionBarController.canCaptureTreasure(currentPlayer)) {
                            actionButtons.getChildren().add(captureTreasureButton);
                        }

                        actionButtons.getChildren().add(endTurnButton);

                        if (actionBarController.canPlaySpecialCard(currentPlayer)) {
                            actionButtons.getChildren().add(playSpecialButton);
                        }
                    } else if (!actionBarController.hasDrawnTreasureCards()) {
                        actionLabel.setText("Draw 2 Treasure Cards");
                        actionBarController.setHasDrawnTreasureCards(true);
                        actionBarController.sendDrawTreasureCardsMessage(2, currentPlayer);
                    } else if (actionBarController.getDrawnFloodCards() != 2) {
                        // 需要抽取洪水卡
                        actionLabel.setText("Draw A Flood Card or Play Special");
                        actionButtons.getChildren().addAll(drawFloodButton);
                        if (actionBarController.canPlaySpecialCard(currentPlayer)) {
                            actionButtons.getChildren().add(playSpecialButton);
                        }
                    } else {
                        // 回合结束
                        actionBarController.nextTurn();
                    }
                }
            } else {
                // 其他玩家的回合
                String status = "";
                if (remainingActions > 0) {
                    status = currentPlayer.getName() + " is taking actions (" + remainingActions + " remaining)";
                } else if (actionBarController.hasDrawnTreasureCards()) {
                    status = currentPlayer.getName() + " is drawing Treasure Cards";
                } else if (actionBarController.getDrawnFloodCards() != 2) {
                    status = currentPlayer.getName() + " is drawing Flood Cards";
                }
                actionLabel.setText(status);
                if (actionBarController.canPlaySpecialCard(room.getCurrentProgramPlayer())) {
                    actionButtons.getChildren().add(playSpecialButton);
                }
            }
        }

    }

    /**
     * 创建统一样式的操作按钮
     */
    private Button createActionButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        button.setPrefWidth(100);
        return button;
    }

    public void setActionBarController(ActionBarController actionBarController) {
        this.actionBarController = actionBarController;
    }

    /**
     * 关闭操作栏视图，清理资源
     */
    public void shutdown() {
        // 清理操作栏资源
        if (actionBar != null) {
            actionBar.getChildren().clear();
        }
        // 清理控制器引用
        actionBarController = null;
    }
}
