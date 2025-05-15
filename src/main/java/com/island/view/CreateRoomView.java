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

public class CreateRoomView {

    private Stage primaryStage;
    // Placeholder for Player object (host)
    // private Player hostPlayer;
    // GameController reference
    private GameController gameController;

    private ListView<String> playerListView;
    private ComboBox<String> difficultyComboBox;

    // Constructor (potentially needs Player and GameController)
    public CreateRoomView(Stage primaryStage /*, Player hostPlayer */) {
        this.primaryStage = primaryStage;
        // this.hostPlayer = hostPlayer;
        this.gameController = null;
        this.playerListView = new ListView<>();
        this.difficultyComboBox = new ComboBox<>();
    }
    
    // Constructor with GameController
    public CreateRoomView(Stage primaryStage, GameController gameController /*, Player hostPlayer */) {
        this.primaryStage = primaryStage;
        this.gameController = gameController;
        // this.hostPlayer = hostPlayer;
        this.playerListView = new ListView<>();
        this.difficultyComboBox = new ComboBox<>();
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // Center: Player List and Settings
        VBox centerBox = new VBox(15);
        centerBox.setAlignment(Pos.CENTER);

        Label playerListLabel = new Label("Players in Room:");
        // Initialize player list (e.g., add the host)
        // if (hostPlayer != null) {
        //     playerListView.getItems().add(hostPlayer.getName() + " (Host)");
        // }
        playerListView.setPrefHeight(150);

        Label difficultyLabel = new Label("Select Difficulty:");
        difficultyComboBox.getItems().addAll("Novice", "Normal", "Elite", "Legendary");
        difficultyComboBox.setValue("Normal"); // Default difficulty

        centerBox.getChildren().addAll(playerListLabel, playerListView, difficultyLabel, difficultyComboBox);
        root.setCenter(centerBox);

        // Bottom: Buttons
        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);

        Button returnButton = new Button("Return");
        returnButton.setOnAction(event -> {
            System.out.println("Return button clicked.");
            // Transition back to MenuView
            if (gameController != null) {
                // This method is not yet implemented in GameController
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

        Button startGameButton = new Button("Start Game");
        startGameButton.setOnAction(event -> {
            String selectedDifficulty = difficultyComboBox.getValue();
            System.out.println("Start Game button clicked. Difficulty: " + selectedDifficulty);
            // Validate if enough players have joined (usually handled by controller)
            // Notify the controller to start the game
            if (gameController != null) {
                // The controller's startGame method exists, but takes a seed parameter
                // that we need to pass directly
                gameController.startGame(System.currentTimeMillis());  // Use current time as seed
            } else {
                System.out.println("Starting game setup...");
                // Fallback when controller is not available
            }
            // Transition to GameView (handled by controller after setup)
        });

        bottomBox.getChildren().addAll(returnButton, startGameButton);
        root.setBottom(bottomBox);
        BorderPane.setMargin(bottomBox, new Insets(15, 0, 0, 0));

        return new Scene(root, 400, 350);
    }

    /**
     * Updates the list of players displayed in the view.
     * Called by the controller when a new player joins.
     * @param playerName The name of the player who joined.
     */
    public void addPlayerToList(String playerName) {
        // Ensure UI updates are run on the JavaFX Application Thread
        Platform.runLater(() -> {
            playerListView.getItems().add(playerName);
        });
    }

    /**
     * Updates the list of players displayed in the view.
     * Called by the controller when a player leaves.
     * @param playerName The name of the player who left.
     */
    public void removePlayerFromList(String playerName) {
        Platform.runLater(() -> {
            playerListView.getItems().remove(playerName);
            // Potentially remove host tag if needed
            playerListView.getItems().remove(playerName + " (Host)");
        });
    }

    // Getters for UI elements if needed by controller
    public ListView<String> getPlayerListView() {
        return playerListView;
    }

    public ComboBox<String> getDifficultyComboBox() {
        return difficultyComboBox;
    }
    
    /**
     * Get the root node of the view
     * @return The root node of the view
     */
    public Pane getView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        
        // Center: Player List and Settings
        VBox centerBox = new VBox(15);
        centerBox.setAlignment(Pos.CENTER);

        Label playerListLabel = new Label("Players in Room:");
        playerListView.setPrefHeight(150);

        Label difficultyLabel = new Label("Select Difficulty:");
        difficultyComboBox.getItems().addAll("Novice", "Normal", "Elite", "Legendary");
        difficultyComboBox.setValue("Normal"); // Default difficulty

        centerBox.getChildren().addAll(playerListLabel, playerListView, difficultyLabel, difficultyComboBox);
        root.setCenter(centerBox);

        // Bottom: Buttons
        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);

        Button returnButton = new Button("Return");
        returnButton.setOnAction(event -> {
            System.out.println("Return button clicked.");
            if (gameController != null) {
                // This method is not yet implemented in GameController
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

        Button startGameButton = new Button("Start Game");
        startGameButton.setOnAction(event -> {
            String selectedDifficulty = difficultyComboBox.getValue();
            System.out.println("Start Game button clicked. Difficulty: " + selectedDifficulty);
            if (gameController != null) {
                // The controller's startGame method exists, but takes a seed parameter
                // that we need to pass directly
                gameController.startGame(System.currentTimeMillis());  // Use current time as seed
            } else {
                System.out.println("Starting game setup...");
                // Fallback when controller is not available
            }
        });

        bottomBox.getChildren().addAll(returnButton, startGameButton);
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
            // Update player list
            Platform.runLater(() -> {
                playerListView.getItems().clear();
                // Add players from the game controller
                // These methods are not yet implemented in GameController
                // gameController.getPlayerList().forEach(player -> {
                //     playerListView.getItems().add(player.getName());
                // });
                System.out.println("CreateRoomView updated - would display player list here");
            });
        }
    }
}
