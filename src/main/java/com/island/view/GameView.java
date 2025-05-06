import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import com.island.view.IslandView;
import com.island.view.PlayerView;
import com.island.view.CardView;
import com.island.view.ActionBarView;
import com.island.controller.GameController;

public class GameView {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private Scene gameScene;

    // Sub-views (placeholders - actual instances will be created/injected)
    private Pane islandViewPane; // Placeholder for IslandView's root node
    private Pane playerViewPane; // Placeholder for PlayerView's root node
    private Pane cardViewPane;   // Placeholder for CardView's root node
    private Pane actionBarViewPane; // Placeholder for ActionBarView's root node
    // ActionLogView functionality is now part of ActionBarView

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
        IslandView islandView = new IslandView(gameController);
        PlayerView playerView = new PlayerView(gameController);
        CardView cardView = new CardView(gameController);
        ActionBarView actionBarView = new ActionBarView(gameController);

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
        javafx.scene.layout.VBox rightPanel = new javafx.scene.layout.VBox(10);
        rightPanel.getChildren().add(cardViewPane);
        rootLayout.setRight(rightPanel);

        // Bottom: Action bar view
        rootLayout.setBottom(actionBarViewPane);

        // Set margins
        BorderPane.setMargin(playerViewPane, new Insets(10));
        BorderPane.setMargin(rightPanel, new Insets(10));
        BorderPane.setMargin(actionBarViewPane, new Insets(10));

        gameScene = new Scene(rootLayout, 1200, 800); // 设置游戏窗口大小
    }

    public Scene getScene() {
        return gameScene;
    }

    public void showGameOverInterface() {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Label gameOverLabel = new javafx.scene.control.Label("Game Over");
            gameOverLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: red;");
            rootLayout.setCenter(new javafx.scene.layout.StackPane(gameOverLabel)); // Replace center content
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
        
        
        if (islandViewPane != null) {
            gameController.getIslandController().updateIslandView();
            System.out.println("update island view");
        }
        
       
        if (playerViewPane != null) {
            gameController.getPlayerController().updatePlayerView();
            System.out.println("update player view");
        }
        
        if (cardViewPane != null) {
            gameController.getCardController().updateCardView();
            System.out.println("update card view");
        }
        
        if (actionBarViewPane != null) {
            gameController.getActionBarController().updateActionBarView();
            System.out.println("update actionbar view");
        }
    }

    public void setPrimaryStage() {
        primaryStage.setTitle("island"); // 设置窗口标题为中文
        primaryStage.setScene(gameScene);
        primaryStage.show();
    }

    public IslandView getIslandView() { return islandView; }
    public PlayerView getPlayerView() { return playerView; }
    public CardView getCardView() { return cardView; }
    public ActionBarView getActionBarView() { return actionBarView; }
    
    public void updatePlayerPosition(Player player, Position newPosition) {
        if (islandView != null) {
            islandView.updatePlayerMarker(player, newPosition);
        }
        if (playerView != null) {
            playerView.updatePlayerInfo(player); // Assuming PlayerView also shows position
        }
    }
}
