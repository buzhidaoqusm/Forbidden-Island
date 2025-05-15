package com.island.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.island.model.Player;
import com.island.controller.GameController;

public class MenuView {

    private Stage primaryStage;
    private Player player;
    private Button createRoomButton;
    private Button joinRoomButton;
    private Button exitButton;
    
    // GameController reference
    private GameController gameController;

    // Constructor without Player
    public MenuView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.gameController = null;
    }
    
    // Constructor with GameController
    public MenuView(Stage primaryStage, GameController gameController) {
        this.primaryStage = primaryStage;
        this.gameController = gameController;
    }

    // Constructor with Player
    public MenuView(Stage primaryStage, Player player) {
        this.primaryStage = primaryStage;
        this.player = player;
    }
    
    /**
     * Creates the menu scene
     * @return Scene object for menu
     */
    public Scene createScene() {
        VBox root = createMenuLayout();
        return new Scene(root, 350, 250);
    }

    /**
     * Creates the menu layout with buttons
     * @return VBox containing menu buttons
     */
    private VBox createMenuLayout() {
        VBox root = new VBox(15); // Spacing between buttons
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(25));

        createRoomButton = new Button("Create Room");
        createRoomButton.setPrefWidth(150);
        createRoomButton.setOnAction(event -> {
            System.out.println("Create Room button clicked.");
            // Transition to CreateRoomView
            if (gameController != null) {
                // Note: This method is not yet implemented in GameController
                // Future implementation will handle this
                System.out.println("Transitioning to Create Room view via GameController");
                // Future implementation: gameController.showCreateRoomView();
                
                // For now, use direct transition
                CreateRoomView createRoomView = new CreateRoomView(primaryStage);
                primaryStage.setScene(createRoomView.createScene());
                primaryStage.setTitle("Forbidden Island - Create Room");
            } else {
                System.out.println("Transitioning to Create Room view...");
                CreateRoomView createRoomView = new CreateRoomView(primaryStage);
                primaryStage.setScene(createRoomView.createScene());
                primaryStage.setTitle("Forbidden Island - Create Room");
            }
        });

        joinRoomButton = new Button("Join Room");
        joinRoomButton.setPrefWidth(150);
        joinRoomButton.setOnAction(event -> {
            System.out.println("Join Room button clicked.");
            // Transition to JoinRoomView
            if (gameController != null) {
                // Note: This method is not yet implemented in GameController
                // Future implementation will handle this
                System.out.println("Transitioning to Join Room view via GameController");
                // Future implementation: gameController.showJoinRoomView();
                
                // For now, use direct transition
                JoinRoomView joinRoomView = new JoinRoomView(primaryStage);
                primaryStage.setScene(joinRoomView.createScene());
                primaryStage.setTitle("Forbidden Island - Join Room");
            } else {
                System.out.println("Transitioning to Join Room view...");
                JoinRoomView joinRoomView = new JoinRoomView(primaryStage);
                primaryStage.setScene(joinRoomView.createScene());
                primaryStage.setTitle("Forbidden Island - Join Room");
            }
        });

        exitButton = new Button("Exit Game");
        exitButton.setPrefWidth(150);
        exitButton.setOnAction(event -> {
            System.out.println("Exit Game button clicked.");
            Platform.exit(); // Closes the JavaFX application
        });

        root.getChildren().addAll(createRoomButton, joinRoomButton, exitButton);
        return root;
    }

    /**
     * Get create room button
     * @return Create room button
     */
    public Button getCreateRoomButton() { 
        return createRoomButton; 
    }
    
    /**
     * Get join room button
     * @return Join room button
     */
    public Button getJoinRoomButton() { 
        return joinRoomButton; 
    }
    
    /**
     * Get exit button
     * @return Exit button
     */
    public Button getExitButton() { 
        return exitButton; 
    }
    
    /**
     * Update the view
     * Implements Observer pattern, updates the interface when game state changes
     * Will be used when Observer pattern is fully implemented
     */
    public void update() {
        if (gameController != null) {
            // In menu view, we may need to update some status information
            // For example: player online status, available room list, etc.
            System.out.println("MenuView updated");
        }
    }
    
    /**
     * Get the root node of the view
     * @return The root node of the view
     */
    public Pane getView() {
        return createMenuLayout();
    }
    
    /**
     * Load main menu with Stage and Player objects
     * @param stage The primary stage
     * @param player The player object
     */
    public void loadMainMenu(Stage stage, Player player) {
        this.primaryStage = stage;
        this.player = player;
        stage.setScene(createScene());
        stage.setTitle("Forbidden Island - Main Menu");
        stage.show();
    }
}
