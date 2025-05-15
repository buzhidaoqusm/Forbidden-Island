package com.island.view;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

import com.island.view.IslandView;
import com.island.view.PlayerView;
import com.island.view.CardView;
import com.island.view.ActionBarView;
import com.island.controller.GameController;
import com.island.model.Player;
import com.island.model.Position;

public class GameView {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private Scene gameScene;

    // Sub-views (actual instances)
    private IslandView islandView;
    private PlayerView playerView;
    private CardView cardView;
    private ActionBarView actionBarView;
    
    // Sub-view panes
    private Pane islandViewPane;
    private Pane playerViewPane;
    private Pane cardViewPane;
    private Pane actionBarViewPane;
    
    // Game interface image resources
    private Image gameBackgroundImage;
    private Image victoryIcon;
    private Image defeatIcon;
    private Map<Integer, Image> treasureIcons = new HashMap<>();

    // GameController reference
    private GameController gameController;

    // Constructor
    public GameView(Stage primaryStage, GameController gameController) {
        this.primaryStage = primaryStage;
        this.gameController = gameController;
        loadImages();
        initialize();
    }
    
    /**
     * Load game interface images
     */
    private void loadImages() {
        try {
            // Load game background image
            gameBackgroundImage = new Image(getClass().getResourceAsStream("/image/UI/game_background.jpg"));
            
            // Load game status icons
            victoryIcon = new Image(getClass().getResourceAsStream("/image/UI/victory.png"));
            defeatIcon = new Image(getClass().getResourceAsStream("/image/UI/defeat.png"));
            
            // Load treasure icons
            for (int i = 1; i <= 4; i++) {
                String path = "/image/UI/treasure_" + i + ".png";
                try {
                    treasureIcons.put(i, new Image(getClass().getResourceAsStream(path)));
                } catch (Exception e) {
                    System.err.println("Unable to load treasure icon: " + path);
                }
            }
        } catch (Exception e) {
            System.err.println("Game interface image resources loading failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initialize() {
        rootLayout = new BorderPane();
        
        // If background image exists, add it to StackPane
        if (gameBackgroundImage != null) {
            StackPane rootContainer = new StackPane();
            
            // Add background image
            ImageView backgroundImageView = new ImageView(gameBackgroundImage);
            backgroundImageView.setFitWidth(1200);
            backgroundImageView.setFitHeight(800);
            backgroundImageView.setPreserveRatio(false); // Stretch to fill entire area
            
            // Add semi-transparent overlay for better readability
            javafx.scene.shape.Rectangle overlay = new javafx.scene.shape.Rectangle(1200, 800);
            overlay.setFill(Color.rgb(173, 216, 230, 0.5)); // Semi-transparent light blue
            
            rootContainer.getChildren().addAll(backgroundImageView, overlay, rootLayout);
            
            // Create scene
            gameScene = new Scene(rootContainer, 1200, 800);
        } else {
            // If no background image, use solid color background
            rootLayout.setStyle("-fx-background-color: #add8e6;"); // Light blue background
            gameScene = new Scene(rootLayout, 1200, 800);
        }

        // Initialize view components
        islandView = new IslandView(gameController);
        playerView = new PlayerView(gameController);
        cardView = new CardView(gameController);
        actionBarView = new ActionBarView(gameController);

        // Register views as observers to be implemented later
        // Currently the observer pattern is not fully implemented in GameController

        // Get panes from view components
        islandViewPane = islandView.getView();
        playerViewPane = playerView.getView();
        cardViewPane = cardView.getView();
        actionBarViewPane = actionBarView.getView();

        // Set layout
        // Center: Island view (main game board)
        rootLayout.setCenter(islandViewPane);

        // Left: Player information view
        rootLayout.setLeft(playerViewPane);

        // Right: Card view
        VBox rightPanel = new VBox(10);
        rightPanel.getChildren().add(cardViewPane);
        rootLayout.setRight(rightPanel);

        // Bottom: Action bar view
        rootLayout.setBottom(actionBarViewPane);

        // Set margins
        BorderPane.setMargin(playerViewPane, new Insets(10));
        BorderPane.setMargin(rightPanel, new Insets(10));
        BorderPane.setMargin(actionBarViewPane, new Insets(10));
    }

    public Scene getScene() {
        return gameScene;
    }

    public void showGameOverInterface() {
        Platform.runLater(() -> {
            // Create game over overlay
            StackPane gameOverPane = new StackPane();
            gameOverPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);"); // Semi-transparent black background
            
            VBox gameOverContent = new VBox(20);
            gameOverContent.setAlignment(javafx.geometry.Pos.CENTER);
            
            // Add game over icon (defeat)
            if (defeatIcon != null) {
                ImageView iconView = new ImageView(defeatIcon);
                iconView.setFitWidth(200);
                iconView.setFitHeight(200);
                iconView.setPreserveRatio(true);
                gameOverContent.getChildren().add(iconView);
            }
            
            // Add game over text
            Label gameOverLabel = new Label("Game Over");
            gameOverLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
            gameOverLabel.setStyle("-fx-text-fill: red;");
            gameOverContent.getChildren().add(gameOverLabel);
            
            gameOverPane.getChildren().add(gameOverContent);
            rootLayout.setCenter(gameOverPane); // Replace center content
            
            System.out.println("Showing game over interface");
        });
    }
    
    public void showVictoryInterface() {
        Platform.runLater(() -> {
            // Create victory overlay
            StackPane victoryPane = new StackPane();
            victoryPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);"); // Semi-transparent black background
            
            VBox victoryContent = new VBox(20);
            victoryContent.setAlignment(javafx.geometry.Pos.CENTER);
            
            // Add victory icon
            if (victoryIcon != null) {
                ImageView iconView = new ImageView(victoryIcon);
                iconView.setFitWidth(200);
                iconView.setFitHeight(200);
                iconView.setPreserveRatio(true);
                victoryContent.getChildren().add(iconView);
            }
            
            // Add victory text
            Label victoryLabel = new Label("Victory!");
            victoryLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
            victoryLabel.setStyle("-fx-text-fill: gold;");
            victoryContent.getChildren().add(victoryLabel);
            
            victoryPane.getChildren().add(victoryContent);
            rootLayout.setCenter(victoryPane); // Replace center content
            
            System.out.println("Showing victory interface");
        });
    }
    
    /**
     * Initialize the game, update all view components
     */
    public void initGame() {
        // Notify all view components to update
        updateAllViews();
    }
    
    /**
     * Update all view components
     */
    public void updateAllViews() {
        if (gameController == null) return;
        
        if (islandView != null) {
            islandView.update();
            System.out.println("Update island view");
        }
        
        if (playerView != null) {
            playerView.update();
            System.out.println("Update player view");
        }
        
        if (cardView != null) {
            cardView.update();
            System.out.println("Update card view");
        }
        
        if (actionBarView != null) {
            actionBarView.update();
            System.out.println("Update actionbar view");
        }
    }

    /**
     * Set the primary stage scene and show it
     */
    public void setPrimaryStage() {
        primaryStage.setTitle("Forbidden Island");
        primaryStage.setScene(gameScene);
        primaryStage.show();
    }
    
    /**
     * Called when returning to main menu
     */
    public void returnToMainMenu() {
        // This would connect to GameController if the method was implemented
        System.out.println("Returning to main menu");
        // Future implementation will call: gameController.showMainMenu();
    }

    public IslandView getIslandView() { return islandView; }
    public PlayerView getPlayerView() { return playerView; }
    public CardView getCardView() { return cardView; }
    public ActionBarView getActionBarView() { return actionBarView; }
    
    /**
     * Update player position on the board
     * @param player Player to update
     * @param newPosition New position
     */
    public void updatePlayerPosition(Player player, Position newPosition) {
        if (islandView != null) {
            islandView.updatePlayerMarker(player, newPosition);
        }
        if (playerView != null) {
            playerView.updatePlayerInfo(player);
        }
    }
    
    /**
     * Update method called by observed subjects
     * Will be used when Observer pattern is implemented
     */
    public void update() {
        updateAllViews();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
