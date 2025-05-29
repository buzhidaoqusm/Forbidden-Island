package com.island.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.Label;

import com.island.model.Player;
import com.island.controller.GameController;

import java.util.Objects;

public class MenuView {

    private Stage primaryStage;
    private Player player;
    private Button createRoomButton;
    private Button joinRoomButton;
    private Button exitButton;
    
    // Image resources
    private Image menuBackgroundImage;
    private Image gameLogo;
    
    // GameController reference
    private GameController gameController;

    // Constructor without Player
    public MenuView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.gameController = null;
        loadImages();
    }
    
    // Constructor with GameController
    public MenuView(Stage primaryStage, GameController gameController) {
        this.primaryStage = primaryStage;
        this.gameController = gameController;
        loadImages();
    }

    // Constructor with Player
    public MenuView(Stage primaryStage, Player player) {
        this.primaryStage = primaryStage;
        this.player = player;
        loadImages();
    }
    
    /**
     * Load menu interface images
     */
    private void loadImages() {
        try {
            // Load menu background
            menuBackgroundImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/image/UI/menu_background.jpg")));
        } catch (Exception e) {
            System.err.println("Menu image resources loading failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates the menu scene
     * @return Scene object for menu
     */
    public Scene createScene() {
        StackPane root = new StackPane();
        
        // Add background image
        if (menuBackgroundImage != null) {
            ImageView backgroundImageView = new ImageView(menuBackgroundImage);
            backgroundImageView.setFitWidth(600);
            backgroundImageView.setFitHeight(400);
            backgroundImageView.setPreserveRatio(true);
            root.getChildren().add(backgroundImageView);
        }
        
        // Add menu controls
        VBox menuBox = createMenuLayout();
        root.getChildren().add(menuBox);
        
        return new Scene(root, 600, 400);
    }

    /**
     * Creates the menu layout with buttons
     * @return VBox containing menu buttons
     */
    private VBox createMenuLayout() {
        VBox root = new VBox(15); // Spacing between buttons
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(25));
        
        // Add game logo
        if (gameLogo != null) {
            ImageView logoImageView = new ImageView(gameLogo);
            logoImageView.setFitWidth(300);
            logoImageView.setFitHeight(100);
            logoImageView.setPreserveRatio(true);
            root.getChildren().add(logoImageView);
        } else {
            // If no logo image, display text title
            Label titleLabel = new Label("Forbidden Island");
            titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
            titleLabel.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.7), 3, 0, 0, 0);");
            root.getChildren().add(titleLabel);
        }

        createRoomButton = new Button("Create Room");
        createRoomButton.setPrefWidth(150);
        createRoomButton.setStyle("-fx-font-size: 14px; -fx-background-radius: 10;");
        createRoomButton.setOnAction(event -> {
            System.out.println("Create Room button clicked.");
            // Transition to CreateRoomView
            if (gameController != null) {
                // Note: This method is not yet implemented in GameController
                // Future implementation will handle this
                System.out.println("Transitioning to Create Room view via GameController");
                // Future implementation: gameController.showCreateRoomView();
                
                // For now, use direct transition with gameController
                CreateRoomView createRoomView = new CreateRoomView(primaryStage, gameController);
                gameController.getRoom().addPlayer(gameController.getCurrentPlayer());
                primaryStage.setScene(createRoomView.createScene());
                primaryStage.setTitle("Forbidden Island - Create Room");
                createRoomView.updatePlayerList(gameController.getRoom().getPlayers());
                System.out.println("当前房间ID： " + gameController.getRoom().getRoomId());
            } else {
                System.out.println("Transitioning to Create Room view...");
                CreateRoomView createRoomView = new CreateRoomView(primaryStage);
                primaryStage.setScene(createRoomView.createScene());
                primaryStage.setTitle("Forbidden Island - Create Room");
            }
        });

        joinRoomButton = new Button("Join Room");
        joinRoomButton.setPrefWidth(150);
        joinRoomButton.setStyle("-fx-font-size: 14px; -fx-background-radius: 10;");
        joinRoomButton.setOnAction(event -> {
            System.out.println("Join Room button clicked.");
            // Transition to JoinRoomView
            if (gameController != null) {
                // Note: This method is not yet implemented in GameController
                // Future implementation will handle this
                System.out.println("Transitioning to Join Room view via GameController");
                // Future implementation: gameController.showJoinRoomView();
                
                // For now, use direct transition
                JoinRoomView joinRoomView = new JoinRoomView(primaryStage, gameController);
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
        exitButton.setStyle("-fx-font-size: 14px; -fx-background-radius: 10;");
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
