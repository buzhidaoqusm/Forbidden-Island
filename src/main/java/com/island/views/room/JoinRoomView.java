package com.island.views.room;

import com.island.controller.GameController;
import com.island.network.RoomController;
import com.island.models.adventurers.Player;
import com.island.network.Message;
import com.island.network.MessageType;
import com.island.models.Room;
import com.island.network.MessageHandler;
import com.island.views.game.GameView;
import com.island.views.ui.MenuView;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class JoinRoomView {
    private Scene scene;
    private Label statusLabel;
    private RoomController roomController;
    private GameController gameController;
    private Label playerListLabel; // 用于显示玩家列表
    private boolean isWaitingConfirmation = false;
    private Room room;

    public JoinRoomView(Stage primaryStage, Player player) {
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
        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(50, 20, 20, 20));

        // 创建顶部工具栏
        HBox topBar = new HBox(10);
        topBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);


        // 创建标题
        Label titleLabel = new Label("Join Room");
        titleLabel.setFont(Font.font("System", 24));

        // 创建输入区域
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);
        Label roomLabel = new Label("Room ID：");
        TextField roomInput = new TextField();
        roomInput.setPromptText("Please enter 3-digit room number");
        roomInput.setPrefWidth(200);
        inputBox.getChildren().addAll(roomLabel, roomInput);

        // 创建状态标签
        statusLabel = new Label("");
        statusLabel.setTextFill(Color.RED);

        // 创建按钮区域
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        // 创建返回按钮
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            updateThread.interrupt();
            if (room != null && room.getPlayers().size() > 1) {
                Message leaveMessage = new Message(
                        MessageType.LEAVE_ROOM,
                        room.getId(),
                        player.getName()
                );
                roomController.broadcast(leaveMessage);
            }

            // 关闭资源
            if (roomController != null) {
                roomController.shutdown();
            }
            if (gameController != null) {
                gameController.shutdown();
            }
            // 返回到主界面
            primaryStage.setScene(new MenuView().getMenuScene(primaryStage, player));
        });

        topBar.getChildren().addAll(backButton);

        // 创建玩家列表标签
        playerListLabel = new Label();
        playerListLabel.setStyle("-fx-font-size: 14px;");

        // 创建加入按钮
        Button joinButton = new Button("Join Room");
        joinButton.setOnAction(e -> {
            if (isWaitingConfirmation) {
                statusLabel.setText("Waiting for confirmation...");
                return;
            }

            String roomNumberStr = roomInput.getText().trim();

            // 验证房间号格式
            if (!roomNumberStr.matches("\\d{3}")) {
                statusLabel.setText("Please enter a 3-digit room number.");
                return;
            }

            int roomNumber = Integer.parseInt(roomNumberStr);

            try {
                // 创建房间控制器
                room = new Room(roomNumber, player);
                GameView gameView = new GameView(primaryStage);
                roomController = new RoomController(room);
                gameController = new GameController(roomController);
                gameView.setGameController(gameController);
                gameController.setGameView(gameView);
                MessageHandler messageHandler = new MessageHandler(gameController);
                roomController.setMessageHandler(messageHandler);

                // 发送加入请求消息
                Message joinRequestMessage = new Message(
                        MessageType.PLAYER_JOIN,
                        roomNumber,
                        player.getName()
                );
                joinRequestMessage.addExtraData("isRequest", true);
                roomController.broadcast(joinRequestMessage);

            } catch (Exception ex) {
                // 关闭资源
                if (roomController != null) {
                    roomController.shutdown();
                    roomController = null;
                }
                if (gameController != null) {
                    gameController.shutdown();
                    gameController = null;
                }
                statusLabel.setText("Join Room Fails：" + ex.getMessage());
                isWaitingConfirmation = false;
                throw new RuntimeException(ex);
            }
        });

        buttonBox.getChildren().addAll(backButton, joinButton);

        // 将所有组件添加到主布局
        root.getChildren().addAll(
                titleLabel,
                inputBox,
                statusLabel,
                buttonBox,
                playerListLabel
        );

        // 创建场景
        scene = new Scene(root, 800, 500);

    }

    private void updatePlayerList() {
        if (room != null && room.getPlayers().size() > 1) {
            StringBuilder playerList = new StringBuilder("Players：\n");
            for (Player p : room.getPlayers()) {
                playerList.append("• ").append(p.getName());
                if (p == room.getHostPlayer()) {
                    playerList.append(" (Host)");
                }
                playerList.append("\n");
            }
            playerListLabel.setText(playerList.toString());
        }
    }

    public Scene getScene() {
        return scene;
    }
}
