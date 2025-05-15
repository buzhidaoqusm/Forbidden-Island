package com.island.view;

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

// import model.Player;
import com.island.controller.GameController;
import com.island.model.Room;

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
            String roomId = roomIdInput.getText().trim();
            feedbackLabel.setText(""); // Clear previous feedback
            if (isValidRoomId(roomId)) {
                System.out.println("Join button clicked. Attempting to join Room ID: " + roomId);
                // Send join request to the controller
                if (gameController != null) {
                    // This method is not implemented yet in the controller
                    // gameController.joinRoom(roomId);
                    // Using placeholder for now
                    System.out.println("Would join room with ID: " + roomId);
                    setFeedback("Join function not yet implemented in controller");
                } else {
                    // Placeholder action when controller is not available
                    System.out.println("Sending join request...");
                    setFeedback("Controller not available. Cannot join room.");
                }
                // The controller would handle the response and update the view
            } else {
                setFeedback("Invalid Room ID format.");
            }
        });

        bottomBox.getChildren().addAll(returnButton, joinButton);
        contentPane.setBottom(bottomBox);
        BorderPane.setMargin(bottomBox, new Insets(15, 0, 0, 0));

        return contentPane;
    }
    
    private boolean isValidRoomId(String roomId) {
        // Add actual validation logic here (e.g., check length, characters)
        return roomId != null && !roomId.isEmpty();
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
