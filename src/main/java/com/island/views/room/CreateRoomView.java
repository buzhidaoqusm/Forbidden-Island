package com.island.views.room;

import com.island.controller.GameController;
import com.island.network.RoomController;
import com.island.models.adventurers.Player;
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
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.shape.Rectangle;

import java.util.concurrent.atomic.AtomicInteger;

public class CreateRoomView {
    private Scene scene;
    private RoomController roomController;
    private Label playerListLabel;
    private Room room;
    private static final double WINDOW_WIDTH = 800;
    private static final double WINDOW_HEIGHT = 600;
    private Thread updateThread;

    public CreateRoomView(Stage primaryStage, Player player) {
        // Generate random room number
        int roomNumber = (int) (Math.random() * 900 + 100);
        room = new Room(roomNumber, player);
        room.setHostPlayer(player);

        // Create room controller
        roomController = new RoomController(room);

        // Set up background
        StackPane root = new StackPane();
        String imagePath = "/background/CreateRoom.png";
        Image backgroundImage = new Image(getClass().getResourceAsStream(imagePath));
        ImageView backgroundView = new ImageView(backgroundImage);
        backgroundView.setFitWidth(WINDOW_WIDTH);
        backgroundView.setFitHeight(WINDOW_HEIGHT);
        backgroundView.setPreserveRatio(false);

        // Create glass effect pane
        VBox glassCard = new VBox(20);
        glassCard.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.85);" +
            "-fx-background-radius: 15;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 10, 0, 0, 0);"
        );
        glassCard.setPadding(new Insets(20));
        glassCard.setMaxWidth(450);
        glassCard.setMaxHeight(500);
        glassCard.setAlignment(Pos.TOP_CENTER);

        // Set up update thread for player list
        updateThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    Platform.runLater(this::updatePlayerList);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        updateThread.setDaemon(true);
        updateThread.start();

        // Back button
        Button backButton = createStyledButton("Back");
        backButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #2C3E50;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 8 16;" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: #2C3E50;" +
            "-fx-border-radius: 20;" +
            "-fx-cursor: hand;"
        );
        backButton.setOnAction(e -> {
            updateThread.interrupt();
            primaryStage.setScene(new MenuView().getMenuScene(primaryStage, player));
        });

        // Create header with back button and room ID
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        header.getChildren().add(backButton);

        // Create title
        Label titleLabel = new Label("Create Game Room");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        titleLabel.setStyle("-fx-text-fill: #2C3E50;");

        // Room ID display
        Label roomIdLabel = new Label("Room ID: " + roomNumber);
        roomIdLabel.setFont(Font.font("System", FontWeight.MEDIUM, 18));
        roomIdLabel.setStyle("-fx-text-fill: #34495E;");

        // Player list section
        playerListLabel = new Label();
        playerListLabel.setFont(Font.font("System", 16));
        playerListLabel.setStyle("-fx-text-fill: #2C3E50;");
        updatePlayerList();

        // Difficulty selection
        AtomicInteger waterLevel = new AtomicInteger(1);
        VBox difficultyBox = createDifficultyBox(waterLevel);

        GameController gameController = new GameController(roomController);
        GameView gameView = new GameView(primaryStage);
        gameView.setGameController(gameController);
        gameController.setGameView(gameView);
        MessageHandler messageHandler = new MessageHandler(gameController);
        roomController.setMessageHandler(messageHandler);

        // Game start button
        Button startGameButton = createStyledButton("Game Start");
        startGameButton.setOnAction(e -> {
            updateThread.interrupt();
            roomController.sendStartGameMessage(player, waterLevel);
            roomController.sendStartTurnMessage(player);
            primaryStage.setScene(gameView.getScene());
        });

        // Add all elements to glass card
        glassCard.getChildren().addAll(
            header,
            roomIdLabel,
            titleLabel,
            playerListLabel,
            difficultyBox,
            startGameButton
        );

        // Center the glass card in the window
        StackPane.setAlignment(glassCard, Pos.CENTER);

        // Add all components to root
        root.getChildren().addAll(backgroundView, glassCard);

        // Create scene
        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    private VBox createDifficultyBox(AtomicInteger waterLevel) {
        VBox difficultyBox = new VBox(15);
        difficultyBox.setAlignment(Pos.CENTER);

        Label difficultyLabel = new Label("Choose Difficulty");
        difficultyLabel.setFont(Font.font("System", FontWeight.MEDIUM, 18));
        difficultyLabel.setStyle("-fx-text-fill: #2C3E50;");

        HBox radioBox = new HBox(20);
        radioBox.setAlignment(Pos.CENTER);

        ToggleGroup difficultyGroup = new ToggleGroup();

        RadioButton noviceButton = createStyledRadioButton("NOVICE", difficultyGroup);
        noviceButton.setSelected(true);
        noviceButton.setOnAction(e -> waterLevel.set(1));

        RadioButton normalButton = createStyledRadioButton("NORMAL", difficultyGroup);
        normalButton.setOnAction(e -> waterLevel.set(2));

        RadioButton eliteButton = createStyledRadioButton("ELITE", difficultyGroup);
        normalButton.setOnAction(e -> waterLevel.set(3));

        RadioButton legendaryButton = createStyledRadioButton("LEGENDARY", difficultyGroup);
        legendaryButton.setOnAction(e -> waterLevel.set(4));

        radioBox.getChildren().addAll(noviceButton, normalButton, eliteButton, legendaryButton);
        difficultyBox.getChildren().addAll(difficultyLabel, radioBox);

        return difficultyBox;
    }

    private RadioButton createStyledRadioButton(String text, ToggleGroup group) {
        RadioButton radio = new RadioButton(text);
        radio.setToggleGroup(group);
        radio.setFont(Font.font("System", 14));
        radio.setStyle("-fx-text-fill: #2C3E50;");
        return radio;
    }

    private Button createStyledButton(String text) {
        Button button = new Button(text);
        if (text.equals("Game Start")) {
            button.setStyle(
                "-fx-background-color: #2C3E50;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;" +
                "-fx-padding: 12 30;" +
                "-fx-background-radius: 25;" +
                "-fx-cursor: hand;"
            );
            
            // Hover effect
            button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: #34495E;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;" +
                "-fx-padding: 12 30;" +
                "-fx-background-radius: 25;" +
                "-fx-cursor: hand;"
            ));
            
            button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: #2C3E50;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;" +
                "-fx-padding: 12 30;" +
                "-fx-background-radius: 25;" +
                "-fx-cursor: hand;"
            ));
        }
        return button;
    }

    private void updatePlayerList() {
        StringBuilder playerList = new StringBuilder("Players:\n");
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
