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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

// Assuming Card class exists in a model package
// import model.Card;
// import model.TreasureCard;
// import model.FloodCard;
// Assuming GameController exists
// import controller.GameController;

public class CardView {

    private HBox viewPane; // Root pane for this view
    private VBox treasureDeckArea;
    private VBox floodDeckArea;

    private Label treasureDeckCountLabel;
    private StackPane treasureDiscardPilePane;
    private Label floodDeckCountLabel;
    private StackPane floodDiscardPilePane;

    // Placeholder for GameController
    // private GameController gameController;

    // Placeholder for card back images
    private Image treasureCardBack;
    private Image floodCardBack;

    // Constructor
    public CardView(/* GameController gameController */) {
        // this.gameController = gameController;
        // Load card back images (replace with actual paths)
        try {
            // treasureCardBack = new Image(getClass().getResourceAsStream("/images/treasure_card_back.png"));
            // floodCardBack = new Image(getClass().getResourceAsStream("/images/flood_card_back.png"));
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
        javafx.application.Platform.runLater(() -> {
            treasureDeckCountLabel.setText("Count: " + count);
        });
    }

    public void updateFloodDeckCount(int count) {
        javafx.application.Platform.runLater(() -> {
            floodDeckCountLabel.setText("Count: " + count);
        });
    }

    public void updateTreasureDiscardPile(Object topCard) {
        javafx.application.Platform.runLater(() -> {
            updateDiscardPilePane(treasureDiscardPilePane, topCard, "Discard");
        });
    }

    public void updateFloodDiscardPile(Object topCard) {
        javafx.application.Platform.runLater(() -> {
            updateDiscardPilePane(floodDiscardPilePane, topCard, "Discard");
        });
    }

    private void updateDiscardPilePane(StackPane pilePane, Object card, String defaultText) {
        pilePane.getChildren().removeIf(node -> node instanceof ImageView || node instanceof Label);

        if (card != null) {
            // Assume card has a method to get its image
            // Image cardImage = card.getImage();
            // Placeholder image generation
            Image cardImage = createPlaceholderCardImage(card);

            if (cardImage != null) {
                ImageView imageView = new ImageView(cardImage);
                imageView.setFitWidth(65);
                imageView.setFitHeight(95);
                imageView.setPreserveRatio(true);
                pilePane.getChildren().add(imageView);
            } else {
                // Fallback if image loading fails or card has no image
                // Label cardNameLabel = new Label(card.getName());
                Label cardNameLabel = new Label("Card " + card.hashCode()%100);
                pilePane.getChildren().add(cardNameLabel);
            }
        } else {
            Label textLabel = new Label(defaultText);
            pilePane.getChildren().add(textLabel);
        }
    }

    // Placeholder for generating a card image (replace with actual image loading)
    private Image createPlaceholderCardImage(Object card) {
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(65, 95);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, 65, 95);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0, 0, 65, 95);
        gc.setFill(Color.BLACK);
        // gc.fillText(card.getName(), 5, 20);
        gc.fillText("Card "+ card.hashCode()%100, 5, 20);
        return canvas.snapshot(null, null);
    }

    public HBox getView() {
        return viewPane;
    }
}