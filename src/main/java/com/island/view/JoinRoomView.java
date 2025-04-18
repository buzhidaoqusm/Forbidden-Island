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

public class JoinRoomView {

    private Stage primaryStage;
    // Placeholder for Player object (the joining player)
    // private Player joiningPlayer;
    // Placeholder for GameController
    // private GameController gameController;

    private ListView<String> playerListView;
    private TextField roomIdInput;
    private Label feedbackLabel; // To display join status/errors

    // Constructor (potentially needs Player and GameController)
    public JoinRoomView(Stage primaryStage /*, Player joiningPlayer, GameController gameController */) {
        this.primaryStage = primaryStage;
        // this.joiningPlayer = joiningPlayer;
        // this.gameController = gameController;
        this.playerListView = new ListView<>();
        this.roomIdInput = new TextField();
        this.feedbackLabel = new Label();
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // Center: Room ID Input, Player List, Feedback
        VBox centerBox = new VBox(10);
        centerBox.setAlignment(Pos.CENTER_LEFT);

        Label roomIdLabel = new Label("Enter Room ID:");
        roomIdInput.setPromptText("Room ID");
        roomIdInput.setMaxWidth(150);

        Label playerListLabel = new Label("Players in Room:");
        playerListView.setPrefHeight(150);
        // Player list will be updated by the controller after successfully joining

        feedbackLabel.setStyle("-fx-text-fill: red;"); // Style for error messages

        centerBox.getChildren().addAll(roomIdLabel, roomIdInput, playerListLabel, playerListView, feedbackLabel);
        root.setCenter(centerBox);

        // Bottom: Buttons
        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);

        Button returnButton = new Button("Return");
        returnButton.setOnAction(event -> {
            System.out.println("Return button clicked.");
            // Transition back to MenuView
            // MenuView menuView = new MenuView(primaryStage, joiningPlayer);
            // primaryStage.setScene(menuView.createScene());
            // primaryStage.setTitle("Forbidden Island - Main Menu");
            // Placeholder action:
            System.out.println("Returning to Main Menu...");
            // Replace with actual transition logic, potentially involving a controller
            // e.g., gameController.showMainMenu();
        });

        Button joinButton = new Button("Join");
        joinButton.setOnAction(event -> {
            String roomId = roomIdInput.getText().trim();
            feedbackLabel.setText(""); // Clear previous feedback
            if (isValidRoomId(roomId)) {
                System.out.println("Join button clicked. Attempting to join Room ID: " + roomId);
                // Send join request to the controller
                // if (gameController != null) {
                //     gameController.joinRoom(roomId, joiningPlayer);
                // }
                // Placeholder action:
                System.out.println("Sending join request...");
                // The controller would handle the response and update the view
            } else {
                setFeedback("Invalid Room ID format.");
            }
        });

        bottomBox.getChildren().addAll(returnButton, joinButton);
        root.setBottom(bottomBox);
        BorderPane.setMargin(bottomBox, new Insets(15, 0, 0, 0));

        return new Scene(root, 400, 350);
    }
    
    private boolean isValidRoomId(String roomId) {
        // Add actual validation logic here (e.g., check length, characters)
        return roomId != null && !roomId.isEmpty();
    }

    public void updatePlayerList(java.util.List<String> playerNames) {
        javafx.application.Platform.runLater(() -> {
            playerListView.getItems().setAll(playerNames);
        });
    }

    public void setFeedback(String message) {
        javafx.application.Platform.runLater(() -> {
            feedbackLabel.setText(message);
        });
    }

    public void clearFeedback() {
        setFeedback("");
    }

    // Getters for UI elements if needed by controller
    public ListView<String> getPlayerListView() {
        return playerListView;
    }

    public TextField getRoomIdInput() {
        return roomIdInput;
    }
}