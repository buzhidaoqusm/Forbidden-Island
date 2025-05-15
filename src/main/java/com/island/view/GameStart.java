package com.island.view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import com.island.model.Player;
import com.island.model.PlayerRole;
import com.island.controller.GameController;

/**
 * GameStart: Responsible for implementing the game startup interface, including user name
 * input. Handles the first step of the player's interaction when entering the game,
 * creates a Player object after the player enters the user name, and transitions to the
 * main menu interface.
 */
public class GameStart {

    private Stage primaryStage;
    private GameController gameController;
    private Player player;
    
    // Image resources
    private Image startBackgroundImage;
    private Image gameLogo;

    /**
     * Constructor with Stage
     * @param primaryStage The primary stage
     */
    public GameStart(Stage primaryStage) {
        this.primaryStage = primaryStage;
        loadImages();
    }
    
    /**
     * Constructor with Stage and GameController
     * @param primaryStage The primary stage
     * @param gameController The game controller
     */
    public GameStart(Stage primaryStage, GameController gameController) {
        this.primaryStage = primaryStage;
        this.gameController = gameController;
        loadImages();
    }
    
    /**
     * Load game start interface images
     */
    private void loadImages() {
        try {
            // Load start screen background
            startBackgroundImage = new Image(getClass().getResourceAsStream("/image/UI/start_background.jpg"));
            // Load game logo
            gameLogo = new Image(getClass().getResourceAsStream("/image/UI/game_logo.png"));
        } catch (Exception e) {
            System.err.println("Start screen image resources loading failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates the username input scene
     * @return Scene object for username input
     */
    public Scene createScene() {
        StackPane root = new StackPane();
        
        // Add background image
        if (startBackgroundImage != null) {
            ImageView backgroundImageView = new ImageView(startBackgroundImage);
            backgroundImageView.setFitWidth(600);
            backgroundImageView.setFitHeight(400);
            backgroundImageView.setPreserveRatio(true);
            root.getChildren().add(backgroundImageView);
        }
        
        // Add interface controls
        VBox contentBox = new VBox(15);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(20));
        
        // Add game logo
        if (gameLogo != null) {
            ImageView logoImageView = new ImageView(gameLogo);
            logoImageView.setFitWidth(300);
            logoImageView.setFitHeight(100);
            logoImageView.setPreserveRatio(true);
            contentBox.getChildren().add(logoImageView);
        } else {
            // If no logo image, display text title
            Label titleLabel = new Label("Forbidden Island");
            titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
            titleLabel.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.7), 3, 0, 0, 0);");
            contentBox.getChildren().add(titleLabel);
        }

        Label nameLabel = new Label("Enter your username:");
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        TextField nameInput = new TextField();
        nameInput.setPromptText("Username");
        nameInput.setMaxWidth(200);

        Button submitButton = new Button("Submit");
        submitButton.setStyle("-fx-font-size: 14px; -fx-background-radius: 10;");
        submitButton.setOnAction(event -> {
            String username = nameInput.getText().trim();
            if (!username.isEmpty()) {
                if (gameController != null) {
                    // Note: These methods are not yet implemented in GameController
                    // Future implementation will handle player creation and menu transition
                    System.out.println("Creating player via GameController: " + username);
                    // Future implementation:
                    // gameController.createPlayer(username, PlayerRole.EXPLORER);
                    // gameController.showMainMenu();
                    
                    // For now, use direct transition
                    MenuView menuView = new MenuView(primaryStage);
                    primaryStage.setScene(menuView.createScene());
                    primaryStage.setTitle("Forbidden Island - Main Menu");
                } else {
                    // For standalone usage without GameController
                    System.out.println("Player created: " + username);
                    MenuView menuView = new MenuView(primaryStage);
                    primaryStage.setScene(menuView.createScene());
                    primaryStage.setTitle("Forbidden Island - Main Menu");
                }
            } else {
                // Handle empty username case
                System.out.println("Username cannot be empty.");
                nameLabel.setText("Username cannot be empty, please try again:");
                nameLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                // Could add alert dialog here in the future
            }
        });

        contentBox.getChildren().addAll(nameLabel, nameInput, submitButton);
        root.getChildren().add(contentBox);
        
        return new Scene(root, 600, 400);
    }
    
    /**
     * Load the main menu
     * @param stage The primary stage
     * @param player The player object
     */
    public void loadMainMenu(Stage stage, Player player) {
        MenuView menuView = new MenuView(stage, player);
        stage.setScene(menuView.createScene());
        stage.setTitle("Forbidden Island - Main Menu");
    }

    /**
     * Main application class for standalone usage
     */
    public static class MainApplication extends Application {
        @Override
        public void start(Stage primaryStage) {
            GameStart gameStart = new GameStart(primaryStage);
            primaryStage.setScene(gameStart.createScene());
            primaryStage.setTitle("Forbidden Island - Welcome");
            primaryStage.show();
        }

        public static void main(String[] args) {
            launch(args);
        }

        // Method to transition to MenuView (called from GameStart)
        public static void showMenuView(Stage stage, Player player) {
             MenuView menuView = new MenuView(stage, player);
             stage.setScene(menuView.createScene());
             stage.setTitle("Forbidden Island - Main Menu");
        }
    }
}

