import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// import model.Player;
// import controller.GameController;

public class CreateRoomView {

    private Stage primaryStage;
    // Placeholder for Player object (host)
    // private Player hostPlayer;
    // Placeholder for GameController
    // private GameController gameController;

    private ListView<String> playerListView;
    private ComboBox<String> difficultyComboBox;

    // Constructor (potentially needs Player and GameController)
    public CreateRoomView(Stage primaryStage /*, Player hostPlayer, GameController gameController */) {
        this.primaryStage = primaryStage;
        // this.hostPlayer = hostPlayer;
        // this.gameController = gameController;
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
            // MenuView menuView = new MenuView(primaryStage, hostPlayer);
            // primaryStage.setScene(menuView.createScene());
            // primaryStage.setTitle("Forbidden Island - Main Menu");
            // Placeholder action:
            System.out.println("Returning to Main Menu...");
            // Replace with actual transition logic, potentially involving a controller
            // e.g., gameController.showMainMenu();
        });

        Button startGameButton = new Button("Start Game");
        startGameButton.setOnAction(event -> {
            String selectedDifficulty = difficultyComboBox.getValue();
            System.out.println("Start Game button clicked. Difficulty: " + selectedDifficulty);
            // Validate if enough players have joined (usually handled by controller)
            // Notify the controller to start the game
            // if (gameController != null) {
            //     gameController.startGame(selectedDifficulty, playerListView.getItems());
            // }
            // Placeholder action:
            System.out.println("Starting game setup...");
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
        javafx.application.Platform.runLater(() -> {
            playerListView.getItems().add(playerName);
        });
    }

    /**
     * Updates the list of players displayed in the view.
     * Called by the controller when a player leaves.
     * @param playerName The name of the player who left.
     */
    public void removePlayerFromList(String playerName) {
        javafx.application.Platform.runLater(() -> {
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
}
