import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import view.IslandView;
import view.PlayerView;
import view.CardView;
import view.ActionBarView;
// Assuming GameController exists
// import controller.GameController;

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

    // Placeholder for GameController
    // private GameController gameController;

    // Constructor
    public GameView(Stage primaryStage /*, GameController gameController */) {
        this.primaryStage = primaryStage;
        // this.gameController = gameController;
        initialize();
    }


    private void initialize() {
        rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: #add8e6;"); // Light blue background

        IslandView islandView = new IslandView(gameController);
        PlayerView playerView = new PlayerView(gameController);
        CardView cardView = new CardView(gameController);
        ActionBarView actionBarView = new ActionBarView(/* gameController */); // Pass controller if needed
        // ActionLogView is removed

        islandViewPane = islandView.getView();
        playerViewPane = playerView.getView();
        cardViewPane = cardView.getView();
        actionBarViewPane = actionBarView.getView();
        // actionLogViewPane is removed

        // --- Example Layout --- 
        // Center: Island View (Main Game Board)
        // rootLayout.setCenter(islandViewPane);
        rootLayout.setCenter(new Pane(new javafx.scene.control.Label("Island View Area"))); // Placeholder

        // Left: Player Info View
        // rootLayout.setLeft(playerViewPane);
        rootLayout.setLeft(new Pane(new javafx.scene.control.Label("Player View Area"))); // Placeholder

        // Right: Card View
        // If Action Log was here, it's now part of ActionBarView at the bottom
        javafx.scene.layout.VBox rightPanel = new javafx.scene.layout.VBox(10);
        // rightPanel.getChildren().add(cardViewPane); // Only CardView here now
        // rootLayout.setRight(rightPanel);
        rootLayout.setRight(new Pane(new javafx.scene.control.Label("Card View Area"))); // Placeholder updated

        // Bottom: Action Bar View
        // rootLayout.setBottom(actionBarViewPane);
        rootLayout.setBottom(new Pane(new javafx.scene.control.Label("Action Bar Area"))); // Placeholder

        // Set preferred sizes or constraints if necessary
        // BorderPane.setMargin(playerViewPane, new javafx.geometry.Insets(10));
        // BorderPane.setMargin(rightPanel, new javafx.geometry.Insets(10));
        // BorderPane.setMargin(actionBarViewPane, new javafx.geometry.Insets(10));

        gameScene = new Scene(rootLayout, 1200, 800); // Example size
    }

    public Scene getScene() {
        return gameScene;
    }

    public void showGameOverInterface() {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Label gameOverLabel = new javafx.scene.control.Label("GAME OVER");
            gameOverLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: red;");
            rootLayout.setCenter(new javafx.scene.layout.StackPane(gameOverLabel)); // Replace center content
            System.out.println("Displaying Game Over Interface.");
        });
    }

    // --- Methods to access or update sub-views (called by Controller) ---

    // Example: Getters for sub-views if the controller needs direct access
    /*
    public IslandView getIslandView() { return islandView; }
    public PlayerView getPlayerView() { return playerView; }
    public CardView getCardView() { return cardView; }
    public ActionBarView getActionBarView() { return actionBarView; }
    // ActionLogView getter removed
    */

    // Example: Method to update a specific part, delegated to the sub-view
    /*
    public void updatePlayerPosition(Player player, Position newPosition) {
        if (islandView != null) {
            islandView.updatePlayerMarker(player, newPosition);
        }
        if (playerView != null) {
            playerView.updatePlayerInfo(player); // Assuming PlayerView also shows position
        }
    }
    */
}
