package com.island.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.application.Platform;
import java.util.List;

import com.island.controller.GameController;
import com.island.controller.CardController;
import com.island.model.Card;
// Note: Actual card class is needed here, if not exists, use Object instead

public class CardView {

    private HBox viewPane; // Root pane for this view
    private VBox treasureDeckArea;
    private VBox floodDeckArea;

    private Label treasureDeckCountLabel;
    private StackPane treasureDiscardPilePane;
    private Label floodDeckCountLabel;
    private StackPane floodDiscardPilePane;

    // GameController reference
    private GameController gameController;

    // Placeholder for card back images
    private Image treasureCardBack;
    private Image floodCardBack;
    
    // Note: Actual card image resources are needed here, currently using placeholders

    // Constructor
    public CardView(GameController gameController) {
        this.gameController = gameController;
        // Load card back images (replace with actual paths)
        try {
            // Note: Actual image resources are needed here
            // treasureCardBack = new Image(getClass().getResourceAsStream("/images/treasure_card_back.png"));
            // floodCardBack = new Image(getClass().getResourceAsStream("/images/flood_card_back.png"));
            System.out.println("Note: Need to add actual card back image resources");
        } catch (Exception e) {
            System.err.println("Error loading card back images: " + e.getMessage());
            treasureCardBack = null; // Handle missing image
            floodCardBack = null;
        }
        initialize();
    }

    private void initialize() {
        viewPane = new HBox(20); // Spacing between deck areas
        viewPane.setPadding(new Insets(10));
        viewPane.setAlignment(Pos.CENTER);
        viewPane.setStyle("-fx-background-color: #e0eee0;"); // Light green background

        // --- Treasure Card Area ---
        treasureDeckArea = new VBox(5);
        treasureDeckArea.setAlignment(Pos.CENTER);
        Label treasureTitle = new Label("Treasure Cards");
        treasureTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        HBox treasurePiles = new HBox(10);
        treasurePiles.setAlignment(Pos.CENTER);

        // Treasure Deck
        VBox treasureDeck = new VBox(2);
        treasureDeck.setAlignment(Pos.CENTER);
        StackPane treasureDeckPane = createCardPileDisplay(treasureCardBack, "Treasure Deck");
        treasureDeckCountLabel = new Label("Count: ?");
        treasureDeck.getChildren().addAll(treasureDeckPane, treasureDeckCountLabel);

        // Treasure Discard Pile
        VBox treasureDiscard = new VBox(2);
        treasureDiscard.setAlignment(Pos.CENTER);
        treasureDiscardPilePane = createCardPileDisplay(null, "Discard"); // Initially empty
        Label treasureDiscardLabel = new Label("Discard Pile");
        treasureDiscard.getChildren().addAll(treasureDiscardPilePane, treasureDiscardLabel);

        treasurePiles.getChildren().addAll(treasureDeck, treasureDiscard);
        treasureDeckArea.getChildren().addAll(treasureTitle, treasurePiles);

        // --- Flood Card Area ---
        floodDeckArea = new VBox(5);
        floodDeckArea.setAlignment(Pos.CENTER);
        Label floodTitle = new Label("Flood Cards");
        floodTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        HBox floodPiles = new HBox(10);
        floodPiles.setAlignment(Pos.CENTER);

        // Flood Deck
        VBox floodDeck = new VBox(2);
        floodDeck.setAlignment(Pos.CENTER);
        StackPane floodDeckPane = createCardPileDisplay(floodCardBack, "Flood Deck");
        floodDeckCountLabel = new Label("Count: ?");
        floodDeck.getChildren().addAll(floodDeckPane, floodDeckCountLabel);

        // Flood Discard Pile
        VBox floodDiscard = new VBox(2);
        floodDiscard.setAlignment(Pos.CENTER);
        floodDiscardPilePane = createCardPileDisplay(null, "Discard"); // Initially empty
        Label floodDiscardLabel = new Label("Discard Pile");
        floodDiscard.getChildren().addAll(floodDiscardPilePane, floodDiscardLabel);

        floodPiles.getChildren().addAll(floodDeck, floodDiscard);
        floodDeckArea.getChildren().addAll(floodTitle, floodPiles);

        viewPane.getChildren().addAll(treasureDeckArea, floodDeckArea);
    }

    private StackPane createCardPileDisplay(Image cardImage, String defaultText) {
        StackPane pilePane = new StackPane();
        pilePane.setPrefSize(70, 100); // Example card size

        Rectangle background = new Rectangle(70, 100);
        background.setFill(Color.LIGHTGRAY);
        background.setStroke(Color.BLACK);
        pilePane.getChildren().add(background);

        if (cardImage != null) {
            ImageView imageView = new ImageView(cardImage);
            imageView.setFitWidth(65);
            imageView.setFitHeight(95);
            imageView.setPreserveRatio(true);
            pilePane.getChildren().add(imageView);
        } else {
            Label textLabel = new Label(defaultText);
            pilePane.getChildren().add(textLabel);
        }
        return pilePane;
    }

    public void updateTreasureDeckCount(int count) {
        Platform.runLater(() -> {
            treasureDeckCountLabel.setText("Count: " + count);
        });
    }

    public void updateFloodDeckCount(int count) {
        Platform.runLater(() -> {
            floodDeckCountLabel.setText("Count: " + count);
        });
    }
    
    /**
     * Update the card view, get the latest card information from GameController
     */
    public void update() {
        if (gameController != null) {
            try {
                CardController cardController = gameController.getCardController();
                if (cardController != null) {
                    // Update card counts
                    try {
                        updateTreasureDeckCount(cardController.getTreasureDeck().size());
                        updateFloodDeckCount(cardController.getFloodDeck().size());
                    } catch (Exception e) {
                        System.err.println("Error getting card counts: " + e.getMessage());
                    }
                    
                    // Update discard pile top cards
                    try {
                        List<Card> treasureDiscardPile = cardController.getTreasureDiscardPile();
                        if (treasureDiscardPile != null && !treasureDiscardPile.isEmpty()) {
                            updateTreasureDiscardPile(treasureDiscardPile.get(treasureDiscardPile.size() - 1));
                        } else {
                            updateTreasureDiscardPile(null);
                        }
                        
                        List<Card> floodDiscardPile = cardController.getFloodDiscardPile();
                        if (floodDiscardPile != null && !floodDiscardPile.isEmpty()) {
                            updateFloodDiscardPile(floodDiscardPile.get(floodDiscardPile.size() - 1));
                        } else {
                            updateFloodDiscardPile(null);
                        }
                    } catch (Exception e) {
                        System.err.println("Error updating discard piles: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error updating card view: " + e.getMessage());
            }
        }
    }

    public void updateTreasureDiscardPile(Card topCard) {
        Platform.runLater(() -> {
            updateDiscardPilePane(treasureDiscardPilePane, topCard, "Discard");
        });
    }

    public void updateFloodDiscardPile(Card topCard) {
        Platform.runLater(() -> {
            updateDiscardPilePane(floodDiscardPilePane, topCard, "Discard");
        });
    }

    private void updateDiscardPilePane(StackPane pilePane, Card card, String defaultText) {
        pilePane.getChildren().removeIf(node -> node instanceof ImageView || node instanceof Label);

        if (card != null) {
            // Try to get card image if the method exists
            Image cardImage = null;
            try {
                // Attempt to use card.getImage() if it exists
                // cardImage = card.getImage();
                // For now, use placeholder image
                cardImage = createPlaceholderCardImage(card);
            } catch (Exception e) {
                // Fallback to placeholder
                cardImage = createPlaceholderCardImage(card);
            }

            if (cardImage != null) {
                ImageView imageView = new ImageView(cardImage);
                imageView.setFitWidth(65);
                imageView.setFitHeight(95);
                imageView.setPreserveRatio(true);
                pilePane.getChildren().add(imageView);
            } else {
                // Fallback if image loading fails
                String cardName = "Unknown Card";
                try {
                    // Try to get card name if method exists
                    cardName = card.getName();
                } catch (Exception e) {
                    cardName = "Card " + card.hashCode()%100;
                }
                Label cardNameLabel = new Label(cardName);
                pilePane.getChildren().add(cardNameLabel);
            }
        } else {
            Label textLabel = new Label(defaultText);
            pilePane.getChildren().add(textLabel);
        }
    }

    // Placeholder for generating a card image (replace with actual image loading)
    private Image createPlaceholderCardImage(Card card) {
        Canvas canvas = new Canvas(65, 95);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, 65, 95);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0, 0, 65, 95);
        
        try {
            String cardName = card.getName();
            gc.setFill(Color.BLACK);
            gc.fillText(cardName, 5, 15);
        } catch (Exception e) {
            gc.setFill(Color.BLACK);
            gc.fillText("Card " + card.hashCode()%100, 5, 15);
        }
        
        return canvas.snapshot(null, null);
    }

    public Pane getView() {
        return viewPane;
    }
}
