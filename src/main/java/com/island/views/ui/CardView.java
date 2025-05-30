package com.forbiddenisland.views.ui;

import com.forbiddenisland.controllers.card.CardController;
import com.forbiddenisland.models.card.Card;
import com.forbiddenisland.models.card.CardType;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static com.forbiddenisland.views.ui.IslandView.SCALE;

import java.util.Deque;
import java.util.List;

public class CardView {
    private CardController cardController;
    private VBox cardsInfoBox;
    private Deque<Card> treasureDeck;
    private Deque<Card> floodDeck;
    private List<Card> treasureDiscardPile;
    private List<Card> floodDiscardPile;

    public CardView(VBox cardsInfoBox) {
        this.cardsInfoBox = cardsInfoBox;
        cardsInfoBox.setPadding(new Insets(10));
        cardsInfoBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1px;");

    }

    // Initialize the card information area
    public void initializeFloodCardsInfo() {
        cardsInfoBox.getChildren().clear();

        // Create flood card area
        HBox floodCardsBox = new HBox(5);
        floodCardsBox.setAlignment(Pos.TOP_CENTER);
        floodCardsBox.setStyle("-fx-padding: 10; -fx-border-color: #cccccc; -fx-border-width: 1;");

        // Flood card icon, showing the number of undrawn flood cards
        StackPane floodSymbol = createCardSymbol("FloodCardSymbol", floodDeck.size(), CardType.FLOOD);

        // Flood card display
        HBox floodCards = new HBox(-30); // Negative value to make cards overlap
        floodCards.setAlignment(Pos.CENTER);

        for (int i = 0; i < floodDiscardPile.size(); i++) {
            Card card = floodDiscardPile.get(i);
            try {
                // Load tile image
                String imagePath = "/flood cards/" + card.getName() + ".png";
                Image tileImage = new Image(getClass().getResourceAsStream(imagePath));
                ImageView tileView = new ImageView(tileImage);
                tileView.setFitHeight(204 * SCALE);
                tileView.setFitWidth(147 * SCALE);

                // Last card should be fully visible
                if (i == floodDiscardPile.size() - 1) {
                    floodCards.getChildren().add(new StackPane(tileView));
                } else {
                    // Create a clipping area
                    Rectangle clip = new Rectangle(
                            60, // Only show a small part on the left
                            204 * SCALE
                    );
                    StackPane cardPane = new StackPane(tileView);
                    cardPane.setClip(clip);
                    floodCards.getChildren().add(cardPane);
                }
            } catch (Exception e) {
                System.err.println("Cannot load image " + card.getName() + ": " + e.getMessage());
            }
        }

        floodCardsBox.getChildren().addAll(floodSymbol, floodCards);
        VBox.setMargin(floodCardsBox, new Insets(0, 0, 10, 0));
        // Add to card information area
        cardsInfoBox.getChildren().add(floodCardsBox);
    }

    public void initializeTreasureCardsInfo() {
        // Create treasure card area
        HBox treasureCardsBox = new HBox(5);
        treasureCardsBox.setAlignment(Pos.TOP_CENTER);
        treasureCardsBox.setStyle("-fx-padding: 10; -fx-border-color: #cccccc; -fx-border-width: 1;");

        // Treasure card symbol
        StackPane treasureSymbol = createCardSymbol("TreasureCardSymbol", treasureDeck.size(), CardType.TREASURE);

        // Discard pile card display
        HBox treasureCards = new HBox(-30); // Negative value to make cards overlap
        treasureCards.setAlignment(Pos.CENTER);

        // Show cards in the treasure discard pile
        for (int i = 0; i < treasureDiscardPile.size(); i++) {
            Card card = treasureDiscardPile.get(i);
            try {
                // Load card image
                String imagePath = "/treasure cards/" + card.getName() + ".png";
                Image cardImage = new Image(getClass().getResourceAsStream(imagePath));
                ImageView cardView = new ImageView(cardImage);
                cardView.setFitHeight(204 * SCALE);
                cardView.setFitWidth(147 * SCALE);

                // Last card should be fully visible
                if (i == treasureDiscardPile.size() - 1) {
                    treasureCards.getChildren().add(new StackPane(cardView));
                } else {
                    // Create a clipping area
                    Rectangle clip = new Rectangle(
                            60, // Only show a small part on the left
                            204 * SCALE
                    );
                    StackPane cardPane = new StackPane(cardView);
                    cardPane.setClip(clip);
                    treasureCards.getChildren().add(cardPane);
                }
            } catch (Exception e) {
                System.err.println("Cannot load image " + card.getName() + ": " + e.getMessage());
            }
        }

        treasureCardsBox.getChildren().addAll(treasureSymbol, treasureCards);
        // Add to card information area
        cardsInfoBox.getChildren().add(treasureCardsBox);
    }


    // Create card symbol (with number)
    private StackPane createCardSymbol(String type, int count, CardType cardType) {
        StackPane cardPane = new StackPane();

        try {
            // Choose different folder path based on card type
            String folderPath = (cardType == CardType.TREASURE) ? "/treasure cards/" : "/flood cards/";
            String imagePath = folderPath + type + ".png";
            Image cardImage = new Image(getClass().getResourceAsStream(imagePath));
            ImageView cardView = new ImageView(cardImage);
            cardView.setFitHeight(204 * SCALE);
            cardView.setFitWidth(147 * SCALE);

            cardPane.getChildren().add(cardView);

            // If there is a count, add a number label
            if (count > 0) {
                Label countLabel = new Label(String.valueOf(count));
                countLabel.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-padding: 2px; -fx-text-fill: black;");
                StackPane.setAlignment(countLabel, Pos.BOTTOM_RIGHT);
                StackPane.setMargin(countLabel, new Insets(0, 5, 5, 0));

                cardPane.getChildren().add(countLabel);
            }
        } catch (Exception e) {
            // If image loading fails, show text
            Rectangle cardRect = new Rectangle(60, 80);
            cardRect.setFill(Color.LIGHTGRAY);
            cardRect.setStroke(Color.BLACK);

            Label typeLabel = new Label(type);

            cardPane.getChildren().addAll(cardRect, typeLabel);

            // If there is a count, add a number label
            if (count > 0) {
                Label countLabel = new Label(String.valueOf(count));
                countLabel.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-padding: 2px;");
                StackPane.setAlignment(countLabel, Pos.BOTTOM_RIGHT);
                StackPane.setMargin(countLabel, new Insets(0, 5, 5, 0));

                cardPane.getChildren().add(countLabel);
            }
        }

        return cardPane;
    }


    public void setCardController(CardController cardController) {
        this.cardController = cardController;
        this.treasureDeck = cardController.getTreasureDeck();
        this.floodDeck = cardController.getFloodDeck();
        this.treasureDiscardPile = cardController.getTreasureDiscardPile();
        this.floodDiscardPile = cardController.getFloodDiscardPile();
    }

    /**
     * Close card view, clean up resources
     */
    public void shutdown() {
        // Clean up card information area resources
        if (cardsInfoBox != null) {
            cardsInfoBox.getChildren().clear();
        }
        // Clean up controller references
        cardController = null;
    }
}
