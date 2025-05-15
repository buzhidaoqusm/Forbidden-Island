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
import java.util.HashMap;
import java.util.Map;

import com.island.controller.GameController;
import com.island.controller.CardController;
import com.island.model.Card;
import com.island.model.CardType;
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

    // 卡片图片资源
    private Image treasureCardBack;
    private Image floodCardBack;
    private Image treasureDiscardImage;
    private Image floodDiscardImage;
    
    // 宝藏卡图片映射（从ID到图片）
    private Map<String, Image> treasureCardImages = new HashMap<>();
    // 洪水卡图片映射
    private Map<String, Image> floodCardImages = new HashMap<>();

    // Constructor
    public CardView(GameController gameController) {
        this.gameController = gameController;
        // 加载卡牌图片资源
        loadCardImages();
        initialize();
    }
    
    /**
     * 加载所有卡牌相关的图片资源
     */
    private void loadCardImages() {
        try {
            // 加载卡背图片
            treasureCardBack = new Image(getClass().getResourceAsStream("/image/Back/Treasure Deck.png"));
            floodCardBack = new Image(getClass().getResourceAsStream("/image/Back/Flood Deck.png"));
            treasureDiscardImage = new Image(getClass().getResourceAsStream("/image/Back/Treasure Discard.png"));
            floodDiscardImage = new Image(getClass().getResourceAsStream("/image/Back/Flood Discard.png"));
            
            // 加载宝藏卡正面图片
            for (int i = 0; i <= 27; i++) { // 根据实际的宝藏卡数量调整
                String path = "/image/TreasureCards/" + i + ".png";
                try {
                    treasureCardImages.put(String.valueOf(i), new Image(getClass().getResourceAsStream(path)));
                } catch (Exception e) {
                    System.err.println("无法加载宝藏卡图片: " + path);
                }
            }
            
            // 加载洪水卡正面图片（用Flood文件夹的图片，因为它们对应瓷砖）
            for (int i = 1; i <= 24; i++) {
                String path = "/image/Flood/" + i + ".png";
                try {
                    floodCardImages.put(String.valueOf(i), new Image(getClass().getResourceAsStream(path)));
                } catch (Exception e) {
                    System.err.println("无法加载洪水卡图片: " + path);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error loading card images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initialize() {
        viewPane = new HBox(20); // Spacing between deck areas
        viewPane.setPadding(new Insets(10));
        viewPane.setAlignment(Pos.CENTER);
        viewPane.setStyle("-fx-background-color: #e0eee0;"); // Light green background

        // --- Treasure Card Area ---
        treasureDeckArea = new VBox(5);
        treasureDeckArea.setAlignment(Pos.CENTER);
        Label treasureTitle = new Label("宝藏卡");
        treasureTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        HBox treasurePiles = new HBox(10);
        treasurePiles.setAlignment(Pos.CENTER);

        // Treasure Deck
        VBox treasureDeck = new VBox(2);
        treasureDeck.setAlignment(Pos.CENTER);
        StackPane treasureDeckPane = createCardPileDisplay(treasureCardBack, "宝藏牌堆");
        treasureDeckCountLabel = new Label("数量: ?");
        treasureDeck.getChildren().addAll(treasureDeckPane, treasureDeckCountLabel);

        // Treasure Discard Pile
        VBox treasureDiscard = new VBox(2);
        treasureDiscard.setAlignment(Pos.CENTER);
        treasureDiscardPilePane = createCardPileDisplay(treasureDiscardImage, "弃牌堆"); // 使用弃牌堆图片
        Label treasureDiscardLabel = new Label("宝藏弃牌堆");
        treasureDiscard.getChildren().addAll(treasureDiscardPilePane, treasureDiscardLabel);

        treasurePiles.getChildren().addAll(treasureDeck, treasureDiscard);
        treasureDeckArea.getChildren().addAll(treasureTitle, treasurePiles);

        // --- Flood Card Area ---
        floodDeckArea = new VBox(5);
        floodDeckArea.setAlignment(Pos.CENTER);
        Label floodTitle = new Label("洪水卡");
        floodTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        HBox floodPiles = new HBox(10);
        floodPiles.setAlignment(Pos.CENTER);

        // Flood Deck
        VBox floodDeck = new VBox(2);
        floodDeck.setAlignment(Pos.CENTER);
        StackPane floodDeckPane = createCardPileDisplay(floodCardBack, "洪水牌堆");
        floodDeckCountLabel = new Label("数量: ?");
        floodDeck.getChildren().addAll(floodDeckPane, floodDeckCountLabel);

        // Flood Discard Pile
        VBox floodDiscard = new VBox(2);
        floodDiscard.setAlignment(Pos.CENTER);
        floodDiscardPilePane = createCardPileDisplay(floodDiscardImage, "弃牌堆"); // 使用弃牌堆图片
        Label floodDiscardLabel = new Label("洪水弃牌堆");
        floodDiscard.getChildren().addAll(floodDiscardPilePane, floodDiscardLabel);

        floodPiles.getChildren().addAll(floodDeck, floodDiscard);
        floodDeckArea.getChildren().addAll(floodTitle, floodPiles);

        viewPane.getChildren().addAll(treasureDeckArea, floodDeckArea);
    }

    private StackPane createCardPileDisplay(Image cardImage, String defaultText) {
        StackPane pilePane = new StackPane();
        pilePane.setPrefSize(80, 120); // 根据卡片图像调整尺寸

        // 如果有图片，直接显示图片
        if (cardImage != null) {
            ImageView imageView = new ImageView(cardImage);
            imageView.setFitWidth(75);
            imageView.setFitHeight(115);
            imageView.setPreserveRatio(true);
            pilePane.getChildren().add(imageView);
        } else {
            // 否则使用矩形和文本代替
            Rectangle background = new Rectangle(80, 120);
            background.setFill(Color.LIGHTGRAY);
            background.setStroke(Color.BLACK);
            Label textLabel = new Label(defaultText);
            pilePane.getChildren().addAll(background, textLabel);
        }
        return pilePane;
    }

    public void updateTreasureDeckCount(int count) {
        Platform.runLater(() -> {
            treasureDeckCountLabel.setText("数量: " + count);
        });
    }

    public void updateFloodDeckCount(int count) {
        Platform.runLater(() -> {
            floodDeckCountLabel.setText("数量: " + count);
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
            updateDiscardPilePane(treasureDiscardPilePane, topCard, "弃牌堆", CardType.TREASURE);
        });
    }

    public void updateFloodDiscardPile(Card topCard) {
        Platform.runLater(() -> {
            updateDiscardPilePane(floodDiscardPilePane, topCard, "弃牌堆", CardType.FLOOD);
        });
    }

    private void updateDiscardPilePane(StackPane pilePane, Card card, String defaultText, CardType cardType) {
        pilePane.getChildren().removeIf(node -> node instanceof ImageView || node instanceof Label || node instanceof Rectangle);

        if (card != null) {
            // 尝试获取卡片图片
            Image cardImage = null;
            String cardId = "0"; // 默认ID
            
            try {
                // 尝试获取卡片名称作为图片查找的键
                cardId = card.getName();
            } catch (Exception e) {
                System.err.println("无法获取卡片名称: " + e.getMessage());
            }
            
            // 根据卡片类型选择对应的图片集合
            if (cardType == CardType.TREASURE) {
                cardImage = treasureCardImages.get(cardId);
            } else if (cardType == CardType.FLOOD) {
                cardImage = floodCardImages.get(cardId);
            }

            if (cardImage != null) {
                // 如果找到卡片图片，使用它
                ImageView imageView = new ImageView(cardImage);
                imageView.setFitWidth(75);
                imageView.setFitHeight(115);
                imageView.setPreserveRatio(true);
                pilePane.getChildren().add(imageView);
            } else {
                // 如果没有对应图片，使用默认图片或文本显示
                if (cardType == CardType.TREASURE) {
                    ImageView imageView = new ImageView(treasureDiscardImage);
                    imageView.setFitWidth(75);
                    imageView.setFitHeight(115);
                    imageView.setPreserveRatio(true);
                    pilePane.getChildren().add(imageView);
                    
                    // 添加卡片名称文本覆盖在图片上
                    try {
                        Label nameLabel = new Label(card.getName());
                        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                        pilePane.getChildren().add(nameLabel);
                    } catch (Exception e) {
                        System.err.println("无法获取卡片名称: " + e.getMessage());
                    }
                } else {
                    ImageView imageView = new ImageView(floodDiscardImage);
                    imageView.setFitWidth(75);
                    imageView.setFitHeight(115);
                    imageView.setPreserveRatio(true);
                    pilePane.getChildren().add(imageView);
                    
                    // 添加卡片名称文本覆盖在图片上
                    try {
                        Label nameLabel = new Label(card.getName());
                        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                        pilePane.getChildren().add(nameLabel);
                    } catch (Exception e) {
                        System.err.println("无法获取卡片名称: " + e.getMessage());
                    }
                }
            }
        } else {
            // 没有卡片，显示空弃牌堆
            if (cardType == CardType.TREASURE) {
                ImageView imageView = new ImageView(treasureDiscardImage);
                imageView.setFitWidth(75);
                imageView.setFitHeight(115);
                imageView.setPreserveRatio(true);
                pilePane.getChildren().add(imageView);
            } else {
                ImageView imageView = new ImageView(floodDiscardImage);
                imageView.setFitWidth(75);
                imageView.setFitHeight(115);
                imageView.setPreserveRatio(true);
                pilePane.getChildren().add(imageView);
            }
            
            // 添加空弃牌堆提示
            Label textLabel = new Label(defaultText);
            textLabel.setStyle("-fx-text-fill: white;");
            pilePane.getChildren().add(textLabel);
        }
    }

    public Pane getView() {
        return viewPane;
    }
}
