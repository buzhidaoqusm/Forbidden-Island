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

    // GameController reference
    private GameController gameController;

    // Constructor
    public GameView(Stage primaryStage, GameController gameController) {
        this.primaryStage = primaryStage;
        this.gameController = gameController;
        initialize();
    }

    private void initialize() {
        rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: #add8e6;"); // Light blue background

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

        gameScene = new Scene(rootLayout, 1200, 800);
    }

    public Scene getScene() {
        return gameScene;
    }

    public void showGameOverInterface() {
        Platform.runLater(() -> {
            Label gameOverLabel = new Label("Game Over");
            gameOverLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: red;");
            rootLayout.setCenter(new StackPane(gameOverLabel)); // Replace center content
            System.out.println("Showing game over interface");
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
            gameController.getIslandController().updateIslandView();
            System.out.println("Update island view");
        }
        
        if (playerView != null) {
            gameController.getPlayerController().updatePlayerView();
            System.out.println("Update player view");
        }
        
        if (cardView != null) {
            gameController.getCardController().updateCardView();
            System.out.println("Update card view");
        }
        
        if (actionBarView != null) {
            gameController.getActionBarController().updateActionBarView();
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
}
