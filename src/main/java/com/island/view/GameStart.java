package com.island.view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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

    /**
     * Constructor with Stage
     * @param primaryStage The primary stage
     */
    public GameStart(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    /**
     * Constructor with Stage and GameController
     * @param primaryStage The primary stage
     * @param gameController The game controller
     */
    public GameStart(Stage primaryStage, GameController gameController) {
        this.primaryStage = primaryStage;
        this.gameController = gameController;
    }

    /**
     * Creates the username input scene
     * @return Scene object for username input
     */
    public Scene createScene() {
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Label nameLabel = new Label("Enter your username:");
        TextField nameInput = new TextField();
        nameInput.setPromptText("Username");
        nameInput.setMaxWidth(200);

        Button submitButton = new Button("Submit");
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
                // Could add alert dialog here in the future
            }
        });

        root.getChildren().addAll(nameLabel, nameInput, submitButton);
        return new Scene(root, 300, 200);
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

