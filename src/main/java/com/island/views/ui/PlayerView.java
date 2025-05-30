package com.island.views.ui;

import com.island.controller.PlayerController;
import com.island.models.Room;
import com.island.models.adventurers.Player;
import com.island.models.adventurers.PlayerRole;
import com.island.models.card.Card;
import com.island.models.treasure.TreasureType;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static com.island.views.ui.IslandView.SCALE;

import java.util.List;

public class PlayerView {
    private PlayerController playerController;
    private VBox playersInfoBox;
    private ImageView selectedCardView; // Currently selected card
    private Room room;
    private static final String SELECTED_STYLE = "-fx-border-color: red; -fx-border-width: 2; -fx-border-style: solid;";


    public PlayerView(VBox playersInfoBox) {
        this.playersInfoBox = playersInfoBox;
        playersInfoBox.setPadding(new Insets(10));
        playersInfoBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1px;");
    }

    public void initPlayersInfo() {
        playersInfoBox.getChildren().clear();

        // Get players in the room
        List<Player> players = room.getPlayers();

        // Create a row of information for each player
        for (Player player : players) {
            HBox playerRow = new HBox(15);
            playerRow.setAlignment(Pos.CENTER_LEFT);

            // Player name
            Label nameLabel = new Label(player.getName());
            nameLabel.setMinWidth(100);
            nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            nameLabel.setStyle("-fx-text-fill: black;");

            // Player role
            HBox roleBox = new HBox(25);
            roleBox.setAlignment(Pos.CENTER_LEFT);

            PlayerRole role = player.getRole();

            try {
                // Load role image
                String imagePath = "/adventurers/" + (role != null ? role.name() : "unknown") + ".png";
                Image roleImage = new Image(getClass().getResourceAsStream(imagePath));
                ImageView roleView = new ImageView(roleImage);
                roleView.setFitHeight(204 * SCALE);
                roleView.setFitWidth(147 * SCALE);

                roleBox.getChildren().addAll(roleView);
            } catch (Exception e) {

            }

            // Player cards
            FlowPane cardsPane = new FlowPane(5, 5);
            cardsPane.setPrefWrapLength(400);

            List<Card> cards = player.getCards();
            if (cards != null && !cards.isEmpty()) {
                for (Card card : cards) {
                    try {
                        // Load card image
                        String imagePath = "/treasure cards/" + card.getName() + ".png";
                        Image cardImage = new Image(getClass().getResourceAsStream(imagePath));
                        ImageView cardView = new ImageView(cardImage);
                        cardView.setFitHeight(204 * SCALE);
                        cardView.setFitWidth(147 * SCALE);

                        // Create a StackPane to wrap the card image to add border
                        StackPane cardPane = new StackPane(cardView);

                        // Add click event for cards
                        cardView.setOnMouseClicked(event -> {
                            // Only the card owner can click
                            if (card.getBelongingPlayer().equals(room.getCurrentProgramPlayer().getName())) {
                                // Remove border from previously selected card
                                if (selectedCardView != null) {
                                    selectedCardView.setStyle("");
                                    selectedCardView.getParent().setStyle("");
                                }
                                // Set new selected card
                                cardPane.setStyle(SELECTED_STYLE);
                                selectedCardView = cardView;

                                Card chosenCard = playerController.getChosenCard();
                                if (chosenCard != null && chosenCard == card) {
                                    playerController.setChosenCard(null);
                                    cardPane.setStyle("");
                                    selectedCardView = null;
                                    return;
                                }
                                playerController.setChosenCard(card);
                            }
                        });

                        cardsPane.getChildren().add(cardPane);
                    } catch (Exception e) {
                        // If image loading fails, show card name
                        Label cardLabel = new Label(card.getName());
                        cardLabel.setPadding(new Insets(5));
                        cardLabel.setStyle("-fx-border-color: #cccccc; -fx-background-color: #ffffff;");
                        cardsPane.getChildren().add(cardLabel);
                    }
                }
            } else {
                cardsPane.getChildren().add(new Label("No cards"));
            }

            // Add all elements to the player row
            playerRow.getChildren().addAll(nameLabel, roleBox, cardsPane);

            if (!player.getCapturedTreasures().isEmpty()) {
                for (TreasureType treasureType : player.getCapturedTreasures()) {
                    // Load treasure image
                    String imagePath = "/treasures/" + treasureType.getDisplayName() + ".png";
                    Image treasureImage = new Image(getClass().getResourceAsStream(imagePath));
                    ImageView treasureView = new ImageView(treasureImage);
                    treasureView.setFitHeight(120 * 0.7);
                    treasureView.setFitWidth(88 * 0.7);

                    // Add treasure image to the top right of the player row
                    StackPane treasurePane = new StackPane(treasureView);
                    treasurePane.setAlignment(Pos.TOP_RIGHT);
                    playerRow.getChildren().add(treasurePane);
                }

            }
            // Add to player info area
            playersInfoBox.getChildren().add(playerRow);
        }
    }
    public void setPlayerController(PlayerController playerController) {
        this.playerController = playerController;
        room = playerController.getRoom();
    }
}
