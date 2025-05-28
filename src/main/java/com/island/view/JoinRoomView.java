package com.island.view;

import com.island.model.Player;
import com.island.network.RoomController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.island.controller.GameController;
import com.island.model.Room;
import com.island.network.Message;
import com.island.network.MessageType;

// import model.Player;

public class JoinRoomView {

    private Stage primaryStage;
    // GameController reference
    private GameController gameController;

    private ListView<String> playerListView;
    private TextField roomIdInput;
    private Label feedbackLabel; // To display join status/errors
    
    // Image resources
    private Image roomBackgroundImage;
    private Image roomTitleImage;
    private Player player;
    private RoomController roomController;

    // Constructor without GameController
    public JoinRoomView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.gameController = null;
        this.playerListView = new ListView<>();
        this.roomIdInput = new TextField();
        this.feedbackLabel = new Label();
        loadImages();
    }
    
    // Constructor with GameController
    public JoinRoomView(Stage primaryStage, GameController gameController) {
        this.primaryStage = primaryStage;
        this.gameController = gameController;
        this.playerListView = new ListView<>();
        this.roomIdInput = new TextField();
        this.feedbackLabel = new Label();
        loadImages();
    }
    
    /**
     * Load room-related images
     */
    private void loadImages() {
        try {
            // Load room background image
            roomBackgroundImage = new Image(getClass().getResourceAsStream("/image/UI/room_background.jpg"));
            // Load room title image
            roomTitleImage = new Image(getClass().getResourceAsStream("/image/UI/join_room_title.png"));
        } catch (Exception e) {
            System.err.println("Room image resources loading failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Scene createScene() {
        StackPane root = new StackPane();
        
        // Add background image
        if (roomBackgroundImage != null) {
            ImageView backgroundImageView = new ImageView(roomBackgroundImage);
            backgroundImageView.setFitWidth(600);
            backgroundImageView.setFitHeight(400);
            backgroundImageView.setPreserveRatio(true);
            root.getChildren().add(backgroundImageView);
        }
        
        // Add content panel
        BorderPane contentPane = createContentPane();
        root.getChildren().add(contentPane);
        
        return new Scene(root, 600, 400);
    }
    
    private BorderPane createContentPane() {
        BorderPane contentPane = new BorderPane();
        contentPane.setPadding(new Insets(20));
        
        // Room title or image
        if (roomTitleImage != null) {
            ImageView titleImageView = new ImageView(roomTitleImage);
            titleImageView.setFitWidth(200);
            titleImageView.setFitHeight(50);
            titleImageView.setPreserveRatio(true);
            HBox titleBox = new HBox(titleImageView);
            titleBox.setAlignment(Pos.CENTER);
            contentPane.setTop(titleBox);
            BorderPane.setMargin(titleBox, new Insets(0, 0, 15, 0));
        } else {
            Label titleLabel = new Label("Join Room");
            titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
            titleLabel.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.7), 3, 0, 0, 0);");
            HBox titleBox = new HBox(titleLabel);
            titleBox.setAlignment(Pos.CENTER);
            contentPane.setTop(titleBox);
            BorderPane.setMargin(titleBox, new Insets(0, 0, 15, 0));
        }

        // Center: Room ID Input, Player List, Feedback
        VBox centerBox = new VBox(10);
        centerBox.setAlignment(Pos.CENTER_LEFT);

        Label roomIdLabel = new Label("Enter Room ID:");
        roomIdLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        roomIdInput.setPromptText("Room ID");
        roomIdInput.setMaxWidth(150);

        Label playerListLabel = new Label("Players in Room:");
        playerListLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        playerListView.setPrefHeight(150);
        playerListView.setStyle("-fx-control-inner-background: rgba(255, 255, 255, 0.7);");
        // Player list will be updated by the controller after successfully joining

        feedbackLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;"); // Style for error messages

        centerBox.getChildren().addAll(roomIdLabel, roomIdInput, playerListLabel, playerListView, feedbackLabel);
        contentPane.setCenter(centerBox);

        // Bottom: Buttons
        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);

        Button returnButton = new Button("Return");
        returnButton.setStyle("-fx-font-size: 14px; -fx-background-radius: 10;");
        returnButton.setOnAction(event -> {
            System.out.println("Return button clicked.");
            // Transition back to MenuView using controller
            if (gameController != null) {
                // This method is not implemented yet in the controller
                // gameController.showMainMenu();
                // Using direct transition for now
                System.out.println("Returning to Main Menu...");
                MenuView menuView = new MenuView(primaryStage);
                primaryStage.setScene(menuView.createScene());
                primaryStage.setTitle("Forbidden Island - Main Menu");
            } else {
                System.out.println("Returning to Main Menu...");
                // Fallback if controller is not available
                MenuView menuView = new MenuView(primaryStage);
                primaryStage.setScene(menuView.createScene());
                primaryStage.setTitle("Forbidden Island - Main Menu");
            }
        });

        Button joinButton = new Button("Join");
        joinButton.setStyle("-fx-font-size: 14px; -fx-background-radius: 10;");
        joinButton.setOnAction(event -> {
//            String inputRoomId = roomIdInput.getText();
//            if (isValidRoomId(inputRoomId)) {
//                gameController.joinRoom(inputRoomId); // 触发RoomController处理
//            }
            String roomId = roomIdInput.getText().trim();
            feedbackLabel.setText(""); // 清除之前的反馈
            if (isValidRoomId(roomId)) {
                handleJoinRoom(roomId);
            } else {
                setFeedback("无效的房间ID格式，请输入正确的UUID");
            }
        });

        bottomBox.getChildren().addAll(returnButton, joinButton);
        contentPane.setBottom(bottomBox);
        BorderPane.setMargin(bottomBox, new Insets(15, 0, 0, 0));

        return contentPane;
    }
    
    private boolean isValidRoomId(String roomId) {
        // 验证UUID格式
        try {
            UUID.fromString(roomId);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void handleJoinRoom(String roomId) {
        if (gameController != null) {
            final AtomicInteger retryCount = new AtomicInteger(0);
            final int maxRetries = 3;
            final long retryDelay = 2000; // 2秒延迟

            new Thread(() -> {
                while (retryCount.get() < maxRetries) {
                    try {
                        Platform.runLater(() -> setFeedback("正在尝试加入房间... 尝试 " + (retryCount.get() + 1) + "/" + maxRetries));
                        
                        // 尝试加入房间
                        Message joinMessage = new Message(MessageType.PLAYER_JOIN, roomId, "system")
                                .addExtraData("username", gameController.getCurrentPlayer());
                        gameController.getRoomController().broadcast(joinMessage);

                        // 等待响应
                        Thread.sleep(retryDelay);
                        
                        // 检查是否成功加入
                        if (gameController.getRoom() != null && 
                            gameController.getRoom().getRoomId() != null && 
                            gameController.getRoom().getRoomId().equals(roomId)) {
                            Platform.runLater(() -> setFeedback("成功加入房间！"));
                            return;
                        }
                        
                        retryCount.incrementAndGet();
                    } catch (Exception e) {
                        Platform.runLater(() -> setFeedback("加入失败: " + e.getMessage()));
                        retryCount.incrementAndGet();
                    }
                }
                
                // 所有重试都失败
                Platform.runLater(() -> setFeedback("无法加入房间，请检查房间ID是否正确"));
            }).start();
        } else {
            setFeedback("游戏控制器未初始化");
        }
    }

    public void updatePlayerList(java.util.List<String> playerNames) {
        Platform.runLater(() -> {
            playerListView.getItems().setAll(playerNames);
        });
    }

    public void setFeedback(String message) {
        Platform.runLater(() -> {
            feedbackLabel.setText(message);
        });
    }

    public void clearFeedback() {
        setFeedback("");
    }

    // Getters for UI elements if needed by controller
    public ListView<String> getPlayerListView() {
        return playerListView;
    }

    public TextField getRoomIdInput() {
        return roomIdInput;
    }
    
    /**
     * Get the root node of the view
     * @return The root node of the view
     */
    public Pane getView() {
        return createContentPane();
    }
    
    /**
     * Update the view
     * Implements Observer pattern, updates the interface when game state changes
     */
    public void update() {
        if (gameController != null) {
            // Update room information
            System.out.println("JoinRoomView updated");
            
            // The following methods are not yet implemented in the controller
            // Get available rooms list
            // java.util.List<String> availableRooms = gameController.getAvailableRooms();
            // if (availableRooms != null) {
            //     // Update available rooms list display
            // }
            //
            // Get current players in room
            // java.util.List<String> playersInRoom = gameController.getPlayersInRoom();
            // if (playersInRoom != null) {
            //     updatePlayerList(playersInRoom);
            // }
        }
    }
}
