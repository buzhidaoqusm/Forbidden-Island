package com.island.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;

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

    // Constructor without GameController
    public JoinRoomView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.gameController = null;
        this.playerListView = new ListView<>();
        this.roomIdInput = new TextField();
        this.feedbackLabel = new Label();
    }
    
    // Constructor with GameController
    public JoinRoomView(Stage primaryStage, GameController gameController) {
        this.primaryStage = primaryStage;
        this.gameController = gameController;
        this.playerListView = new ListView<>();
        this.roomIdInput = new TextField();
        this.feedbackLabel = new Label();
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // Center: Room ID Input, Player List, Feedback
        VBox centerBox = new VBox(10);
        centerBox.setAlignment(Pos.CENTER_LEFT);

        Label roomIdLabel = new Label("Enter Room ID:");
        roomIdInput.setPromptText("Room ID");
        roomIdInput.setMaxWidth(150);

        Label playerListLabel = new Label("Players in Room:");
        playerListView.setPrefHeight(150);
        // Player list will be updated by the controller after successfully joining

        feedbackLabel.setStyle("-fx-text-fill: red;"); // Style for error messages

        centerBox.getChildren().addAll(roomIdLabel, roomIdInput, playerListLabel, playerListView, feedbackLabel);
        root.setCenter(centerBox);

        // Bottom: Buttons
        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);

        Button returnButton = new Button("Return");
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
        root.setBottom(bottomBox);
        BorderPane.setMargin(bottomBox, new Insets(15, 0, 0, 0));

        return new Scene(root, 400, 350);
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
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        
        // Center: Room ID Input, Player List, Feedback
        VBox centerBox = new VBox(10);
        centerBox.setAlignment(Pos.CENTER_LEFT);

        Label roomIdLabel = new Label("Enter Room ID:");
        roomIdInput.setPromptText("Room ID");
        roomIdInput.setMaxWidth(150);

        Label playerListLabel = new Label("Players in Room:");
        playerListView.setPrefHeight(150);

        feedbackLabel.setStyle("-fx-text-fill: red;"); // Style for error messages

        centerBox.getChildren().addAll(roomIdLabel, roomIdInput, playerListLabel, playerListView, feedbackLabel);
        root.setCenter(centerBox);

        // Bottom: Buttons
        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);

        Button returnButton = new Button("Return");
        returnButton.setOnAction(event -> {
            System.out.println("Return button clicked.");
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
                MenuView menuView = new MenuView(primaryStage);
                primaryStage.setScene(menuView.createScene());
                primaryStage.setTitle("Forbidden Island - Main Menu");
            }
        });

        Button joinButton = new Button("Join");
        joinButton.setOnAction(event -> {
            String roomId = roomIdInput.getText().trim();
            feedbackLabel.setText(""); // Clear previous feedback
            if (isValidRoomId(roomId)) {
                System.out.println("Join button clicked. Attempting to join Room ID: " + roomId);
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
            } else {
                setFeedback("Invalid Room ID format.");
            }
        });

        bottomBox.getChildren().addAll(returnButton, joinButton);
        root.setBottom(bottomBox);
        BorderPane.setMargin(bottomBox, new Insets(15, 0, 0, 0));
        
        return root;
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
            
            // For now, just log the update attempt
            System.out.println("Room and player information would be updated here");
        }
    }
}
