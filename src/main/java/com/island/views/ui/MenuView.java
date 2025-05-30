package com.island.views.ui;

import com.island.models.adventurers.Player;
import com.island.views.room.CreateRoomView;
import com.island.views.room.JoinRoomView;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MenuView {
    public Scene getMenuScene(Stage primaryStage, Player player) {
        // 创建欢迎信息标签
        Label welcomeLabel = new Label("Welcome, " + player.getName() + "!");

        // 设置字体大小
        welcomeLabel.setFont(new Font("Arial", 46));  // 设置字体为 Arial，大小为 24
        // 设置字体颜色
        welcomeLabel.setStyle("-fx-text-fill: #000000;");  // 设置字体颜色为黑色

        Button createRoomButton = new Button("Create Room");
        Button joinRoomButton = new Button("Join Room");

        // 按钮点击事件（跳转到相应场景）
        createRoomButton.setOnAction(e -> {
            // 创建房间界面
            primaryStage.setScene(new CreateRoomView(primaryStage, player).getScene());
        });

        joinRoomButton.setOnAction(e -> {
            // 加入房间界面
            primaryStage.setScene(new JoinRoomView(primaryStage, player).getScene());
        });

        // 创建 VBox 布局，设置按钮间距为 20 像素
        VBox layout = new VBox(20);
        layout.setStyle("-fx-alignment: center;");  // 设置水平方向居中

        // 将按钮添加到 VBox 中
        layout.getChildren().addAll(welcomeLabel, createRoomButton, joinRoomButton);

        // 创建并返回场景
        return new Scene(layout, 800, 500);
    }
}
