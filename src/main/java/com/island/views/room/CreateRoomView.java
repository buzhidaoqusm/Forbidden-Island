package com.island.views.room;

import com.island.controller.GameController;
import com.island.network.RoomController;
import com.island.models.adventurers.Player;
import com.island.models.Room;
import com.island.network.MessageHandler;
import com.island.views.game.GameView;
import com.island.views.ui.MenuView;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicInteger;

public class CreateRoomView {
    private Scene scene;
    private RoomController roomController;
    private Label playerListLabel; // 用于显示玩家列表
    private Room room;

    public CreateRoomView(Stage primaryStage, Player player) {
        // 产生随机3位数房间号
        int roomNumber = (int) (Math.random() * 900 + 100);
        room = new Room(roomNumber, player);
        room.setHostPlayer(player);

        // 创建房间控制器
        roomController = new RoomController(room);

        // 设置定时更新玩家列表
        Thread updateThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000); // 每秒更新一次
                    Platform.runLater(this::updatePlayerList);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        updateThread.setDaemon(true); // 设置为守护线程
        updateThread.start();

        // 创建主布局
        VBox root = new VBox(10); // 10是各组件之间的间距
        root.setAlignment(javafx.geometry.Pos.TOP_CENTER);

        // 创建顶部工具栏
        HBox topBar = new HBox(10);
        topBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // 创建返回按钮
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            updateThread.interrupt();
            // 返回到主界面
            primaryStage.setScene(new MenuView().getMenuScene(primaryStage, player));
        });

        // 显示房间号
        Label roomNumberLabel = new Label("Room ID: " + roomNumber);

        // 将按钮和房间号添加到顶部工具栏
        topBar.getChildren().addAll(backButton, roomNumberLabel);

        // 创建玩家列表标签
        playerListLabel = new Label();
        playerListLabel.setStyle("-fx-font-size: 14px;");
        updatePlayerList();

        GameController gameController = new GameController(roomController);
        GameView gameView = new GameView(primaryStage);
        gameView.setGameController(gameController);
        gameController.setGameView(gameView);
        MessageHandler messageHandler = new MessageHandler(gameController);
        roomController.setMessageHandler(messageHandler);

        // 创建checkbox，选择难度
        AtomicInteger waterLevel = new AtomicInteger(1);
        HBox difficultyBox = getDifficultyBox(waterLevel);

        // 创建开始游戏按钮
        Button startGameButton = new Button("Game Start");
        startGameButton.setStyle("-fx-font-size: 16px;");
        startGameButton.setOnAction(e -> {
            updateThread.interrupt();
            roomController.sendStartGameMessage(player, waterLevel);

            // 使用RoomController发送开始回合消息
            roomController.sendStartTurnMessage(player);

            // 跳转到游戏界面
            primaryStage.setScene(gameView.getScene());
        });

        // 将所有组件添加到主布局
        root.getChildren().addAll(topBar, playerListLabel, startGameButton, difficultyBox);

        // 创建场景
        scene = new Scene(root, 800, 500);
    }

    private static HBox getDifficultyBox(AtomicInteger waterLevel) {
        Label difficultyLabel = new Label("Choose Difficulty:");
        difficultyLabel.setStyle("-fx-font-size: 16px;");

        ToggleGroup difficultyGroup = new ToggleGroup();

        RadioButton noviceButton = new RadioButton("NOVICE");
        noviceButton.setToggleGroup(difficultyGroup);
        noviceButton.setSelected(true);
        noviceButton.setOnAction(e -> {
            waterLevel.set(1);
        });

        RadioButton normalButton = new RadioButton("NORMAL");
        normalButton.setToggleGroup(difficultyGroup);
        normalButton.setOnAction(e -> {
            waterLevel.set(2);
        });

        RadioButton eliteButton = new RadioButton("ELITE");
        eliteButton.setToggleGroup(difficultyGroup);
        eliteButton.setOnAction(e -> {
            waterLevel.set(3);
        });

        RadioButton legendaryButton = new RadioButton("LEGENDARY");
        legendaryButton.setToggleGroup(difficultyGroup);
        legendaryButton.setOnAction(e -> {
            waterLevel.set(4);
        });

        HBox difficultyBox = new HBox(10, difficultyLabel, noviceButton, normalButton, eliteButton, legendaryButton);
        difficultyBox.setAlignment(Pos.CENTER);
        return difficultyBox;
    }

    /**
     * 更新玩家列表显示
     */
    private void updatePlayerList() {
        StringBuilder playerList = new StringBuilder("Players：\n");
        for (Player p : room.getPlayers()) {
            playerList.append("• ").append(p.getName());
            if (p == room.getPlayers().get(0)) {
                playerList.append(" (Host)");
            }
            playerList.append("\n");
        }
        playerListLabel.setText(playerList.toString());
    }

    public Scene getScene() {
        return scene;
    }
}
