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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class JoinRoomView {
    private Scene scene;
    private Label statusLabel;
    private RoomController roomController;
    private GameController gameController;
    private Label playerListLabel;
    private boolean isWaitingConfirmation = false;
    private Room room;
    private Thread updateThread;
    private static final double WINDOW_WIDTH = 800;
    private static final double WINDOW_HEIGHT = 600;

    public JoinRoomView(Stage primaryStage, Player player) {
        // Set up background
        StackPane root = new StackPane();
        String imagePath = "/background/Room.png";
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
            if (room != null && room.getPlayers().size() > 1) {
                Message leaveMessage = new Message(
                    MessageType.LEAVE_ROOM,
                    room.getId(),
                    player.getName()
                );
                roomController.broadcast(leaveMessage);
            }

            // Close resources
            if (roomController != null) {
                roomController.shutdown();
            }
            if (gameController != null) {
                gameController.shutdown();
            }
            // Return to main menu
            primaryStage.setScene(new MenuView().getMenuScene(primaryStage, player));
        });

        // Create header with back button
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        header.getChildren().add(backButton);

        // Create title
        Label titleLabel = new Label("Join Room");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        titleLabel.setStyle("-fx-text-fill: #2C3E50;");

        // Create input area
        VBox inputSection = new VBox(15);
        inputSection.setAlignment(Pos.CENTER);
        
        Label roomLabel = new Label("Room ID");
        roomLabel.setFont(Font.font("System", FontWeight.MEDIUM, 18));
        roomLabel.setStyle("-fx-text-fill: #34495E;");
        
        TextField roomInput = new TextField();
        roomInput.setPromptText("Enter 3-digit room number");
        roomInput.setPrefWidth(200);
        roomInput.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-padding: 8 12;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: #BDC3C7;" +
            "-fx-border-radius: 8;"
        );
        
        inputSection.getChildren().addAll(roomLabel, roomInput);

        // Create status label
        statusLabel = new Label("");
        statusLabel.setFont(Font.font("System", 14));
        statusLabel.setTextFill(Color.web("#E74C3C"));

        // Create player list label
        playerListLabel = new Label();
        playerListLabel.setFont(Font.font("System", 16));
        playerListLabel.setStyle("-fx-text-fill: #2C3E50;");

        // Create join button
        Button joinButton = createStyledButton("Join Room");
        joinButton.setOnAction(e -> {
            if (isWaitingConfirmation) {
                statusLabel.setText("Waiting for confirmation...");
                return;
            }

            String roomNumberStr = roomInput.getText().trim();

            // Validate room number format
            if (!roomNumberStr.matches("\\d{3}")) {
                statusLabel.setText("Please enter a 3-digit room number.");
                return;
            }

            int roomNumber = Integer.parseInt(roomNumberStr);

            try {
                // Create room controller
                room = new Room(roomNumber, player);
                GameView gameView = new GameView(primaryStage);
                roomController = new RoomController(room);
                gameController = new GameController(roomController);
                gameView.setGameController(gameController);
                gameController.setGameView(gameView);
                MessageHandler messageHandler = new MessageHandler(gameController);
                roomController.setMessageHandler(messageHandler);

                // Send join request message
                Message joinRequestMessage = new Message(
                    MessageType.PLAYER_JOIN,
                    roomNumber,
                    player.getName()
                );
                joinRequestMessage.addExtraData("isRequest", true);
                roomController.broadcast(joinRequestMessage);

            } catch (Exception ex) {
                // Close resources
                if (roomController != null) {
                    roomController.shutdown();
                    roomController = null;
                }
                if (gameController != null) {
                    gameController.shutdown();
                    gameController = null;
                }
                statusLabel.setText("Join Room Failed: " + ex.getMessage());
                isWaitingConfirmation = false;
                throw new RuntimeException(ex);
            }
        });

        // Add all elements to glass card
        glassCard.getChildren().addAll(
            header,
            titleLabel,
            inputSection,
            statusLabel,
            playerListLabel,
            joinButton
        );

        // Center the glass card in the window
        StackPane.setAlignment(glassCard, Pos.CENTER);

        // Add all components to root
        root.getChildren().addAll(backgroundView, glassCard);

        // Create scene
        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    private Button createStyledButton(String text) {
        Button button = new Button(text);
        if (!text.equals("Back")) {
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
        if (room != null && room.getPlayers().size() > 1) {
            StringBuilder playerList = new StringBuilder("Players:\n");
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
