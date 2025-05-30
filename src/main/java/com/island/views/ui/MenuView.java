package com.forbiddenisland.views.ui;

import com.forbiddenisland.models.adventurers.Player;
import com.forbiddenisland.views.room.CreateRoomView;
import com.forbiddenisland.views.room.JoinRoomView;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MenuView {
    public Scene getMenuScene(Stage primaryStage, Player player) {
        // Create welcome message label
        Label welcomeLabel = new Label("Welcome, " + player.getName() + "!");

        // Set font size
        welcomeLabel.setFont(new Font("Arial", 46));  // Set font to Arial, size 46
        // Set font color
        welcomeLabel.setStyle("-fx-text-fill: #000000;");  // Set font color to black

        Button createRoomButton = new Button("Create Room");
        Button joinRoomButton = new Button("Join Room");

        // Button click events (navigate to corresponding scenes)
        createRoomButton.setOnAction(e -> {
            // Create room interface
            primaryStage.setScene(new CreateRoomView(primaryStage, player).getScene());
        });

        joinRoomButton.setOnAction(e -> {
            // Join room interface
            primaryStage.setScene(new JoinRoomView(primaryStage, player).getScene());
        });

        // Create VBox layout, set button spacing to 20 pixels
        VBox layout = new VBox(20);
        layout.setStyle("-fx-alignment: center;");  // Set horizontal alignment to center

        // Add buttons to VBox
        layout.getChildren().addAll(welcomeLabel, createRoomButton, joinRoomButton);

        // Create and return the scene
        return new Scene(layout, 800, 500);
    }
}
