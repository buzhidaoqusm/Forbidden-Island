package com.island.views.game;

import com.island.controller.GameController;
import com.island.models.adventurers.Player;
import com.island.models.game.GameState;
import com.island.models.island.Position;
import com.island.util.observer.GameObserver;
import com.island.views.ui.ActionBarView;
import com.island.views.ui.ActionLogView;
import com.island.views.ui.CardView;
import com.island.views.ui.IslandView;
import com.island.views.ui.MenuView;
import com.island.views.ui.PlayerView;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class GameView implements GameObserver {
    private Scene scene;
    private Stage primaryStage;
    private GameController gameController;

    private IslandView islandView;
    private GridPane boardGrid;
    private VBox waterLevelBox;

    private PlayerView playerView;
    private VBox playersInfoBox; // Player information area

    private CardView cardView;
    private VBox cardsInfoBox; // Card information area

    private ActionLogView actionLogView; // Action log view

    private ActionBarView actionBarView;
    private HBox actionBar; // Action bar

    public GameView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        boardGrid = new GridPane();
        waterLevelBox = new VBox(10);
        playersInfoBox = new VBox(10);
        cardsInfoBox = new VBox(20);
        actionBar = new HBox(10);

        islandView = new IslandView(boardGrid, waterLevelBox);
        playerView = new PlayerView(playersInfoBox);
        cardView = new CardView(cardsInfoBox);
        actionLogView = new ActionLogView(); // Create action log view
        actionBarView = new ActionBarView(actionBar); // Create action bar view
    }

    public void initGame() {
        gameController.getMessageHandler().setActionLogView(actionLogView); // Set action log view

        islandView.initializeBoard();
        islandView.initWaterLevel();
        playerView.initPlayersInfo();
        cardView.initializeFloodCardsInfo();
        cardView.initializeTreasureCardsInfo();
        actionBarView.initActionButtons();

        // Create main layout (using BorderPane)
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Set background to root
        root.setBackground(islandView.getBackground());

        // Create horizontal layout container (for board and water level bar)
        HBox topLayout = new HBox(20);
        topLayout.setAlignment(Pos.CENTER);
        actionLogView.setAlignment(Pos.CENTER_RIGHT);
        waterLevelBox.setAlignment(Pos.CENTER);
        boardGrid.setStyle("-fx-background-color: transparent;");
        topLayout.getChildren().addAll(boardGrid, waterLevelBox, actionLogView); // Add action log view

        VBox contentLayout = new VBox(20);
        contentLayout.setAlignment(Pos.TOP_CENTER);
        // Make player info area and card info area semi-transparent
        playersInfoBox.setStyle("-fx-background-color: rgba(240, 240, 240, 0.8); -fx-border-color: #cccccc; -fx-border-width: 1px;");
        cardsInfoBox.setStyle("-fx-background-color: rgba(240, 240, 240, 0.8); -fx-border-color: #cccccc; -fx-border-width: 1px;");
        contentLayout.getChildren().addAll(topLayout, playersInfoBox, cardsInfoBox);

        ScrollPane scrollPane = new ScrollPane(contentLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background: transparent;" +
                        "-fx-border-color: transparent;"
        );
        // This is the important part - making the viewport transparent
        scrollPane.getStyleClass().add("transparent-viewport");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        root.setCenter(scrollPane);

        // Add action bar to top
        root.setTop(actionBar);
        scene = new Scene(root, 1000, 800);
    }
    
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
        islandView.setIslandController(gameController.getIslandController());
        playerView.setPlayerController(gameController.getPlayerController());
        cardView.setCardController(gameController.getCardController());
        actionBarView.setActionBarController(gameController.getActionBarController());
        
        // Register as observer
        gameController.getGameSubject().addObserver(this);
    }

    public Scene getScene() {
        return scene;
    }

    public IslandView getIslandView() {
        return islandView;
    }

    public void setPrimaryStage() {
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Get the primary Stage window
     * @return The primary Stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void addLog(String message) {
        actionLogView.addLog(message);
    }

    // The following are GameObserver interface implementation methods
    @Override
    public void onGameStateChanged(GameState state) {
        Platform.runLater(() -> {
            // Handle game state changes
            if (state == GameState.GAME_OVER) {
                // Game over handling logic
                returnToMainMenu();
            } else if (state == GameState.TURN_START) {
                // Turn start handling logic
                updateActionBar();
            }
        });
    }

    @Override
    public void onBoardChanged() {
        Platform.runLater(() -> {
            islandView.initializeBoard();
        });
    }

    @Override
    public void onPlayerMoved(Player player, Position newPosition) {
        Platform.runLater(() -> {
            // Player movement update
            islandView.initializeBoard();
        });
    }

    @Override
    public void onWaterLevelChanged(int newLevel) {
        Platform.runLater(() -> {
            islandView.initWaterLevel();
        });
    }

    @Override
    public void onCardChanged() {
        Platform.runLater(() -> {
            cardView.initializeFloodCardsInfo();
            cardView.initializeTreasureCardsInfo();
        });
    }

    @Override
    public void onPlayerInfoChanged() {
        Platform.runLater(() -> {
            playerView.initPlayersInfo();
        });
    }

    @Override
    public void onActionBarChanged() {
        Platform.runLater(() -> {
            actionBarView.updateActionBar();
        });
    }

    public void updateActionBar() {
        actionBarView.updateActionBar();
    }

    public void returnToMainMenu() {
        Platform.runLater(() -> {
            try {
                MenuView mainMenuView = new MenuView();
                Scene menuScene = mainMenuView.getMenuScene(primaryStage, gameController.getRoom().getCurrentProgramPlayer());

                if (menuScene != null) {
                    primaryStage.setScene(menuScene);
                    primaryStage.show();
                } else {
                    System.err.println("Menu scene is null");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error returning to main menu: " + e.getMessage());
            }
        });
    }
}
