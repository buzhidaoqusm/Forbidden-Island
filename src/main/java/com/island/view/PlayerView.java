import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Assuming Player class exists in a model package
// import model.Player;
// import model.Role;
// Assuming GameController exists
// import controller.GameController;

public class PlayerView {

    private VBox viewPane; // The root pane for this view component
    private Map<String, VBox> playerInfoBoxes; // Map playerName to their display VBox
    // Placeholder for GameController
    // private GameController gameController;

    // Constructor
    public PlayerView(/* GameController gameController */) {
        // this.gameController = gameController;
        initialize();
    }

    private void initialize() {
        viewPane = new VBox(10); // Spacing between player sections
        viewPane.setPadding(new Insets(10));
        viewPane.setAlignment(Pos.TOP_LEFT);
        viewPane.setStyle("-fx-background-color: #f0f0f0;"); // Light grey background
        playerInfoBoxes = new HashMap<>();

        Label title = new Label("Players");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        viewPane.getChildren().add(title);
    }

    public void updatePlayerInfo(Object player, boolean isCurrentPlayer) {
        // String playerName = player.getName();
        // String playerRole = player.getRole().getName();
        // int handSize = player.getHand().size();
        // String position = player.getCurrentTile().getName(); // Or getPosition().toString()
        // Color playerColor = player.getColor(); // Assuming Player has a color

        // Placeholder data:
        String playerName = "Player " + player.hashCode() % 100; // Example name
        String playerRole = "Pilot"; // Example role
        int handSize = (int)(Math.random() * 5) + 1;
        String position = "Tile (" + (int)(Math.random()*6) + "," + (int)(Math.random()*6) + ")";
        Color playerColor = Color.rgb((int)(Math.random()*200), (int)(Math.random()*200), (int)(Math.random()*200));

        javafx.application.Platform.runLater(() -> {
            VBox playerBox = playerInfoBoxes.get(playerName);
            if (playerBox == null) {
                // Create new box if player not seen before
                playerBox = new VBox(3);
                playerBox.setPadding(new Insets(5));
                playerBox.setStyle("-fx-border-color: black; -fx-border-width: 1;");
                playerInfoBoxes.put(playerName, playerBox);
                viewPane.getChildren().add(playerBox);
            } else {
                // Clear existing content before updating
                playerBox.getChildren().clear();
            }

            // Player Name and Color Indicator
            HBox nameBox = new HBox(5);
            nameBox.setAlignment(Pos.CENTER_LEFT);
            Rectangle colorRect = new Rectangle(15, 15, playerColor);
            Label nameLabel = new Label(playerName);
            nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            nameBox.getChildren().addAll(colorRect, nameLabel);

            // Other Info
            Label roleLabel = new Label("Role: " + playerRole);
            Label handLabel = new Label("Cards: " + handSize);
            Label posLabel = new Label("Position: " + position);

            playerBox.getChildren().addAll(nameBox, roleLabel, handLabel, posLabel);

            // Highlight current player
            if (isCurrentPlayer) {
                playerBox.setStyle("-fx-border-color: red; -fx-border-width: 3;");
            } else {
                playerBox.setStyle("-fx-border-color: black; -fx-border-width: 1;");
            }
        });
    }

    public void updateAllPlayers(List<Object> players, Object currentPlayer) {
        javafx.application.Platform.runLater(() -> {
            // Optional: Clear existing boxes if players can leave
            // viewPane.getChildren().retainAll(viewPane.getChildren().get(0)); // Keep title
            // playerInfoBoxes.clear();

            // String currentPlayerName = (currentPlayer != null) ? currentPlayer.getName() : null;
            String currentPlayerName = (currentPlayer != null) ? "Player " + currentPlayer.hashCode() % 100 : null;

            for (Object player : players) {
                // String playerName = player.getName();
                String playerName = "Player " + player.hashCode() % 100;
                updatePlayerInfo(player, playerName.equals(currentPlayerName));
            }
        });
    }

    public void removePlayer(String playerName) {
        javafx.application.Platform.runLater(() -> {
            VBox playerBox = playerInfoBoxes.remove(playerName);
            if (playerBox != null) {
                viewPane.getChildren().remove(playerBox);
            }
        });
    }

    public VBox getView() {
        return viewPane;
    }
}