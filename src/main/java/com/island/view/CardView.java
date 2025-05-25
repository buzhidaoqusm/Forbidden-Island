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
            System.out.println("开始加载卡牌图片资源...");
            
            // 尝试加载卡背图片，使用多个可能的路径
            String[][] cardBackPaths = {
                // 宝藏卡背面
                {
                    "/Back/Treasure Deck.png", 
                    "/TreasureCards/Treasure Deck.png",
                    "/image/Back/Treasure Deck.png", 
                    "/Design/Back/Treasure.png", 
                    "/Design/Cards/TreasureBack.png"
                },
                // 洪水卡背面
                {
                    "/Back/Flood Deck.png", 
                    "/Flood/Flood Deck.png",
                    "/image/Back/Flood Deck.png", 
                    "/Design/Back/Flood.png", 
                    "/Design/Cards/FloodBack.png"
                },
                // 宝藏弃牌堆
                {
                    "/Back/Treasure Discard.png", 
                    "/image/Back/Treasure Discard.png", 
                    "/Design/Back/TreasureDiscard.png", 
                    "/Design/Cards/TreasureDiscard.png"
                },
                // 洪水弃牌堆
                {
                    "/Back/Flood Discard.png", 
                    "/image/Back/Flood Discard.png", 
                    "/Design/Back/FloodDiscard.png", 
                    "/Design/Cards/FloodDiscard.png"
                }
            };
            
            Image[] cardImages = {treasureCardBack, floodCardBack, treasureDiscardImage, floodDiscardImage};
            String[] cardNames = {"宝藏卡背", "洪水卡背", "宝藏弃牌堆", "洪水弃牌堆"};
            
            // 尝试加载每种卡背图片
            for (int i = 0; i < cardBackPaths.length; i++) {
                for (String path : cardBackPaths[i]) {
                    try {
                        Image img = new Image(getClass().getResourceAsStream(path));
                        if (img != null && !img.isError()) {
                            switch (i) {
                                case 0: treasureCardBack = img; break;
                                case 1: floodCardBack = img; break;
                                case 2: treasureDiscardImage = img; break;
                                case 3: floodDiscardImage = img; break;
                            }
                            System.out.println("成功加载" + cardNames[i] + "图片: " + path);
                            break;
                        }
                    } catch (Exception e) {
                        // 继续尝试下一个路径
                        System.out.println("尝试加载" + cardNames[i] + "图片失败: " + path);
                    }
                }
                
                // 如果图片仍未加载成功，创建默认图片
                if ((i == 0 && treasureCardBack == null) || 
                    (i == 1 && floodCardBack == null) ||
                    (i == 2 && treasureDiscardImage == null) ||
                    (i == 3 && floodDiscardImage == null)) {
                    
                    Canvas canvas = new Canvas(80, 120);
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    
                    // 绘制卡片背景
                    Color bgColor = i % 2 == 0 ? Color.GOLDENROD : Color.LIGHTBLUE;
                    gc.setFill(bgColor);
                    gc.fillRoundRect(0, 0, 80, 120, 10, 10);
                    gc.setStroke(Color.BLACK);
                    gc.setLineWidth(2);
                    gc.strokeRoundRect(0, 0, 80, 120, 10, 10);
                    
                    // 添加文本
                    gc.setFill(Color.BLACK);
                    gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    gc.fillText(cardNames[i], 10, 60);
                    
                    // 如果是弃牌堆，添加额外标记
                    if (i >= 2) {
                        gc.setStroke(Color.RED);
                        gc.setLineWidth(3);
                        gc.strokeLine(10, 10, 70, 110);
                        gc.strokeLine(10, 110, 70, 10);
                    }
                    
                    javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                    params.setFill(Color.TRANSPARENT);
                    Image defaultImage = canvas.snapshot(params, null);
                    
                    switch (i) {
                        case 0: treasureCardBack = defaultImage; break;
                        case 1: floodCardBack = defaultImage; break;
                        case 2: treasureDiscardImage = defaultImage; break;
                        case 3: floodDiscardImage = defaultImage; break;
                    }
                    System.out.println("使用默认" + cardNames[i] + "图片");
                }
            }
            
            // 尝试加载宝藏卡正面图片
            String[] treasureFolderPaths = {
                "/TreasureCards/",
                "/image/TreasureCards/",
                "/Design/Cards/Treasure/",
                "/Design/TreasureCards/"
            };
            
            // 宝藏卡的类型和对应的ID范围
            int[][] treasureCardRanges = {
                {0, 4},    // 地之宝石卡 (0-4)
                {5, 9},    // 风之雕像卡 (5-9)
                {10, 14},  // 火之水晶卡 (10-14)
                {15, 19},  // 海洋圣杯卡 (15-19)
                {20, 22},  // 直升机卡 (20-22)
                {23, 24},  // 沙袋卡 (23-24)
                {25, 27}   // 水位上升卡 (25-27)
            };
            
            String[] treasureTypes = {
                "地之宝石", "风之雕像", "火之水晶", "海洋圣杯", "直升机", "沙袋", "水位上升"
            };
            
            for (int i = 0; i <= 27; i++) {
                boolean loaded = false;
                for (String folderPath : treasureFolderPaths) {
                    String path = folderPath + i + ".png";
                    try {
                        Image img = new Image(getClass().getResourceAsStream(path));
                        if (img != null && !img.isError()) {
                            treasureCardImages.put(String.valueOf(i), img);
                            
                            // 确定卡片类型
                            String cardType = "未知";
                            for (int j = 0; j < treasureCardRanges.length; j++) {
                                if (i >= treasureCardRanges[j][0] && i <= treasureCardRanges[j][1]) {
                                    cardType = treasureTypes[j];
                                    break;
                                }
                            }
                            
                            System.out.println("成功加载宝藏卡图片: " + path + " (类型: " + cardType + ")");
                            loaded = true;
                            break;
                        }
                    } catch (Exception e) {
                        // 继续尝试下一个路径
                    }
                }
                
                // 如果没有加载成功，创建默认图片
                if (!loaded) {
                    Canvas canvas = new Canvas(80, 120);
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    
                    // 确定卡片类型和颜色
                    String cardType = "未知";
                    Color cardColor = Color.GOLD;
                    for (int j = 0; j < treasureCardRanges.length; j++) {
                        if (i >= treasureCardRanges[j][0] && i <= treasureCardRanges[j][1]) {
                            cardType = treasureTypes[j];
                            switch (j) {
                                case 0: cardColor = Color.BROWN; break;      // 地之宝石
                                case 1: cardColor = Color.LIGHTGRAY; break;  // 风之雕像
                                case 2: cardColor = Color.ORANGERED; break;  // 火之水晶
                                case 3: cardColor = Color.DEEPSKYBLUE; break;// 海洋圣杯
                                case 4: cardColor = Color.LIGHTGREEN; break; // 直升机
                                case 5: cardColor = Color.TAN; break;        // 沙袋
                                case 6: cardColor = Color.DARKBLUE; break;   // 水位上升
                            }
                            break;
                        }
                    }
                    
                    // 绘制卡片背景
                    gc.setFill(cardColor);
                    gc.fillRoundRect(0, 0, 80, 120, 10, 10);
                    gc.setStroke(Color.BLACK);
                    gc.setLineWidth(2);
                    gc.strokeRoundRect(0, 0, 80, 120, 10, 10);
                    
                    // 添加文本
                    gc.setFill(Color.BLACK);
                    gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    gc.fillText(cardType, 10, 30);
                    gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
                    gc.fillText("#" + i, 35, 60);
                    
                    // 添加图标
                    if (cardType.equals("直升机")) {
                        gc.setFill(Color.BLACK);
                        gc.fillOval(25, 70, 30, 15);
                        gc.fillRect(35, 65, 10, 5);
                    } else if (cardType.equals("沙袋")) {
                        gc.setFill(Color.SADDLEBROWN);
                        gc.fillRoundRect(25, 70, 30, 20, 5, 5);
                        gc.setStroke(Color.BLACK);
                        gc.strokeRoundRect(25, 70, 30, 20, 5, 5);
                    } else if (cardType.equals("水位上升")) {
                        gc.setFill(Color.BLUE);
                        gc.fillPolygon(
                            new double[]{40, 25, 55},
                            new double[]{70, 90, 90},
                            3
                        );
                    }
                    
                    javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                    params.setFill(Color.TRANSPARENT);
                    treasureCardImages.put(String.valueOf(i), canvas.snapshot(params, null));
                    System.out.println("使用默认宝藏卡图片 #" + i + " (类型: " + cardType + ")");
                }
            }
            
            // 尝试加载洪水卡正面图片
            String[] floodFolderPaths = {
                "/Flood/",
                "/image/Flood/",
                "/Design/Cards/Flood/",
                "/Design/FloodCards/"
            };
            
            for (int i = 1; i <= 24; i++) {
                boolean loaded = false;
                for (String folderPath : floodFolderPaths) {
                    String path = folderPath + i + ".png";
                    try {
                        Image img = new Image(getClass().getResourceAsStream(path));
                        if (img != null && !img.isError()) {
                            floodCardImages.put(String.valueOf(i), img);
                            System.out.println("成功加载洪水卡图片: " + path + " (瓦片ID: " + i + ")");
                            loaded = true;
                            break;
                        }
                    } catch (Exception e) {
                        // 继续尝试下一个路径
                    }
                }
                
                // 如果没有加载成功，创建默认图片
                if (!loaded) {
                    Canvas canvas = new Canvas(80, 120);
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    
                    // 绘制卡片背景
                    gc.setFill(Color.LIGHTBLUE);
                    gc.fillRoundRect(0, 0, 80, 120, 10, 10);
                    gc.setStroke(Color.BLACK);
                    gc.setLineWidth(2);
                    gc.strokeRoundRect(0, 0, 80, 120, 10, 10);
                    
                    // 添加水纹图案
                    gc.setStroke(Color.BLUE);
                    gc.setLineWidth(1);
                    for (int y = 20; y < 120; y += 20) {
                        for (int x = 5; x < 80; x += 15) {
                            gc.strokeLine(x, y, x + 10, y + 5);
                        }
                    }
                    
                    // 添加文本
                    gc.setFill(Color.BLACK);
                    gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
                    gc.fillText("洪水卡", 15, 40);
                    gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
                    gc.fillText("#" + i, 30, 80);
                    
                    javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                    params.setFill(Color.TRANSPARENT);
                    floodCardImages.put(String.valueOf(i), canvas.snapshot(params, null));
                    System.out.println("使用默认洪水卡图片 #" + i);
                }
            }
            
            System.out.println("卡牌图片资源加载完成: " +
                          treasureCardImages.size() + " 张宝藏卡, " +
                          floodCardImages.size() + " 张洪水卡");
            
        } catch (Exception e) {
            System.err.println("加载卡牌图片时出错: " + e.getMessage());
            e.printStackTrace();
            createDefaultCardImages();
        }
    }
    
    /**
     * 创建所有默认卡片图片（在图片加载完全失败时使用）
     */
    private void createDefaultCardImages() {
        System.out.println("创建所有默认卡片图片...");
        
        // 创建默认的牌堆背面图片
        Canvas treasureBackCanvas = new Canvas(80, 120);
        GraphicsContext treasureGc = treasureBackCanvas.getGraphicsContext2D();
        treasureGc.setFill(Color.GOLD);
        treasureGc.fillRoundRect(0, 0, 80, 120, 10, 10);
        treasureGc.setStroke(Color.BLACK);
        treasureGc.setLineWidth(2);
        treasureGc.strokeRoundRect(0, 0, 80, 120, 10, 10);
        treasureGc.setFill(Color.BLACK);
        treasureGc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        treasureGc.fillText("宝藏", 25, 50);
        treasureGc.fillText("卡牌", 25, 70);
        
        javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        treasureCardBack = treasureBackCanvas.snapshot(params, null);
        
        // 创建默认的洪水牌背面
        Canvas floodBackCanvas = new Canvas(80, 120);
        GraphicsContext floodGc = floodBackCanvas.getGraphicsContext2D();
        floodGc.setFill(Color.LIGHTBLUE);
        floodGc.fillRoundRect(0, 0, 80, 120, 10, 10);
        floodGc.setStroke(Color.BLACK);
        floodGc.setLineWidth(2);
        floodGc.strokeRoundRect(0, 0, 80, 120, 10, 10);
        floodGc.setFill(Color.BLACK);
        floodGc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        floodGc.fillText("洪水", 25, 50);
        floodGc.fillText("卡牌", 25, 70);
        
        floodCardBack = floodBackCanvas.snapshot(params, null);
        
        // 创建默认的弃牌堆图片
        Canvas treasureDiscardCanvas = new Canvas(80, 120);
        GraphicsContext treasureDiscardGc = treasureDiscardCanvas.getGraphicsContext2D();
        treasureDiscardGc.setFill(Color.GOLD.brighter());
        treasureDiscardGc.fillRoundRect(0, 0, 80, 120, 10, 10);
        treasureDiscardGc.setStroke(Color.BLACK);
        treasureDiscardGc.setLineWidth(2);
        treasureDiscardGc.strokeRoundRect(0, 0, 80, 120, 10, 10);
        treasureDiscardGc.setStroke(Color.RED);
        treasureDiscardGc.setLineWidth(3);
        treasureDiscardGc.strokeLine(10, 10, 70, 110);
        treasureDiscardGc.strokeLine(10, 110, 70, 10);
        
        treasureDiscardImage = treasureDiscardCanvas.snapshot(params, null);
        
        Canvas floodDiscardCanvas = new Canvas(80, 120);
        GraphicsContext floodDiscardGc = floodDiscardCanvas.getGraphicsContext2D();
        floodDiscardGc.setFill(Color.LIGHTBLUE.brighter());
        floodDiscardGc.fillRoundRect(0, 0, 80, 120, 10, 10);
        floodDiscardGc.setStroke(Color.BLACK);
        floodDiscardGc.setLineWidth(2);
        floodDiscardGc.strokeRoundRect(0, 0, 80, 120, 10, 10);
        floodDiscardGc.setStroke(Color.RED);
        floodDiscardGc.setLineWidth(3);
        floodDiscardGc.strokeLine(10, 10, 70, 110);
        floodDiscardGc.strokeLine(10, 110, 70, 10);
        
        floodDiscardImage = floodDiscardCanvas.snapshot(params, null);
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
                System.out.println("开始更新卡牌视图...");
                CardController cardController = gameController.getCardController();
                if (cardController != null) {
                    // Update card counts
                    try {
                        int treasureDeckSize = cardController.getTreasureDeck().size();
                        int floodDeckSize = cardController.getFloodDeck().size();
                        
                        updateTreasureDeckCount(treasureDeckSize);
                        updateFloodDeckCount(floodDeckSize);
                        
                        System.out.println("更新卡牌数量 - 宝藏卡: " + treasureDeckSize + ", 洪水卡: " + floodDeckSize);
                    } catch (Exception e) {
                        System.err.println("获取卡牌数量时出错: " + e.getMessage());
                        e.printStackTrace();
                    }
                    
                    // Update discard pile top cards
                    try {
                        List<Card> treasureDiscardPile = cardController.getTreasureDiscardPile();
                        if (treasureDiscardPile != null && !treasureDiscardPile.isEmpty()) {
                            Card topCard = treasureDiscardPile.get(treasureDiscardPile.size() - 1);
                            updateTreasureDiscardPile(topCard);
                            System.out.println("更新宝藏弃牌堆顶部卡片: " + topCard.getName() + ", 类型: " + topCard.getType());
                        } else {
                            updateTreasureDiscardPile(null);
                            System.out.println("宝藏弃牌堆为空");
                        }
                        
                        List<Card> floodDiscardPile = cardController.getFloodDiscardPile();
                        if (floodDiscardPile != null && !floodDiscardPile.isEmpty()) {
                            Card topCard = floodDiscardPile.get(floodDiscardPile.size() - 1);
                            updateFloodDiscardPile(topCard);
                            System.out.println("更新洪水弃牌堆顶部卡片: " + topCard.getName());
                        } else {
                            updateFloodDiscardPile(null);
                            System.out.println("洪水弃牌堆为空");
                        }
                    } catch (Exception e) {
                        System.err.println("更新弃牌堆时出错: " + e.getMessage());
                        e.printStackTrace();
                    }
                    
                    System.out.println("卡牌视图更新完成");
                } else {
                    System.err.println("卡牌控制器为空，无法更新卡牌视图");
                }
            } catch (Exception e) {
                System.err.println("更新卡牌视图时出错: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("游戏控制器为空，无法更新卡牌视图");
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
            String cardId = null; // 默认为空
            
            try {
                // 尝试获取卡片ID
                if (cardType == CardType.TREASURE) {
                    // 对于宝藏卡，我们需要根据卡片类型来确定ID
                    if (card.getType() == CardType.TREASURE) {
                        // 宝藏卡ID（0-19）
                        cardId = card.getName();
                    } else if (card.getType() == CardType.HELICOPTER) {
                        // 直升机卡 (20-22)
                        cardId = "20";
                    } else if (card.getType() == CardType.SANDBAGS) {
                        // 沙袋卡 (23-24)
                        cardId = "23";
                    } else if (card.getType() == CardType.WATER_RISE) {
                        // 水位上升卡 (25-27)
                        cardId = "25";
                    }
                } else if (cardType == CardType.FLOOD) {
                    // 对于洪水卡，使用卡片名称作为ID，该名称应与对应瓦片ID匹配
                    cardId = card.getName();
                }
                
                System.out.println("更新" + (cardType == CardType.TREASURE ? "宝藏" : "洪水") + 
                                  "弃牌堆，显示卡片: ID=" + cardId + ", 类型=" + card.getType());
            } catch (Exception e) {
                System.err.println("无法获取卡片ID: " + e.getMessage());
            }
            
            // 根据卡片类型选择对应的图片集合
            if (cardType == CardType.TREASURE) {
                if (cardId != null && treasureCardImages.containsKey(cardId)) {
                    cardImage = treasureCardImages.get(cardId);
                } else {
                    // 尝试根据类型创建默认图片
                    cardImage = createDefaultTreasureCardImage(card.getType().toString());
                }
            } else if (cardType == CardType.FLOOD) {
                if (cardId != null && floodCardImages.containsKey(cardId)) {
                    cardImage = floodCardImages.get(cardId);
                } else {
                    // 尝试根据名称创建默认图片
                    cardImage = createDefaultFloodCardImage(card.getName());
                }
            }

            if (cardImage != null) {
                // 如果找到卡片图片，使用它
                ImageView imageView = new ImageView(cardImage);
                imageView.setFitWidth(75);
                imageView.setFitHeight(115);
                imageView.setPreserveRatio(true);
                pilePane.getChildren().add(imageView);
                
                // 添加标签显示卡片类型或名称
                String cardName = "";
                if (cardType == CardType.TREASURE) {
                    try {
                        if (card.getType() == CardType.TREASURE && card.getTreasureType() != null) {
                            cardName = card.getTreasureType().getDisplayName();
                        } else {
                            cardName = formatCardTypeName(card.getType().toString());
                        }
                    } catch (Exception e) {
                        cardName = "宝藏卡";
                    }
                } else {
                    cardName = "洪水: " + card.getName();
                }
                
                // 创建一个半透明的信息面板显示在卡片底部
                Pane infoPane = new Pane();
                Rectangle infoBg = new Rectangle(75, 20);
                infoBg.setFill(Color.rgb(0, 0, 0, 0.7));
                
                Label nameLabel = new Label(cardName);
                nameLabel.setTextFill(Color.WHITE);
                nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                nameLabel.setLayoutX(5);
                nameLabel.setLayoutY(2);
                
                infoPane.getChildren().addAll(infoBg, nameLabel);
                infoPane.setLayoutY(95); // 放在卡片底部
                
                pilePane.getChildren().add(infoPane);
            } else {
                // 如果找不到卡片图片，使用默认样式
                Rectangle background = new Rectangle(75, 115);
                if (cardType == CardType.TREASURE) {
                    background.setFill(Color.GOLD);
                } else {
                    background.setFill(Color.LIGHTBLUE);
                }
                background.setStroke(Color.BLACK);
                
                Label textLabel = new Label(card.getName());
                textLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                
                pilePane.getChildren().addAll(background, textLabel);
            }
        } else {
//            // 如果没有卡片，显示弃牌堆图片
//            Image discardImage = (cardType == CardType.TREASURE) ? treasureDiscardImage : floodDiscardImage;
//
//            if (discardImage != null) {
//                ImageView imageView = new ImageView(discardImage);
//                imageView.setFitWidth(75);
//                imageView.setFitHeight(115);
//                imageView.setPreserveRatio(true);
//                pilePane.getChildren().add(imageView);
//            } else {
//                // 如果没有弃牌堆图片，使用默认样式
//                Rectangle background = new Rectangle(75, 115);
//                background.setFill((cardType == CardType.TREASURE) ? Color.GOLD.brighter() : Color.LIGHTBLUE.brighter());
//                background.setStroke(Color.BLACK);
//                background.setStrokeWidth(2);
//
//                Label textLabel = new Label(defaultText);
//                textLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
//
//                // 添加X形标记表示弃牌堆
//                Line line1 = new Line(10, 10, 65, 105);
//                Line line2 = new Line(10, 105, 65, 10);
//                line1.setStroke(Color.RED);
//                line2.setStroke(Color.RED);
//                line1.setStrokeWidth(3);
//                line2.setStrokeWidth(3);
//
//                pilePane.getChildren().addAll(background, line1, line2, textLabel);
//            }
        }
    }
    
    /**
     * 创建默认的宝藏卡图片
     * @param cardType 卡片类型
     * @return 生成的默认图片
     */
    private Image createDefaultTreasureCardImage(String cardType) {
        Canvas canvas = new Canvas(80, 120);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // 设置卡片颜色
        Color cardColor = Color.GOLD;
        String cardName = "未知";
        
        // 根据卡片类型设置样式
        switch (cardType) {
            case "TREASURE":
                cardColor = Color.GOLD;
                cardName = "宝藏";
                break;
            case "HELICOPTER":
                cardColor = Color.LIGHTGREEN;
                cardName = "直升机";
                break;
            case "SANDBAGS":
                cardColor = Color.TAN;
                cardName = "沙袋";
                break;
            case "WATER_RISE":
                cardColor = Color.DARKBLUE;
                cardName = "水位上升";
                break;
            default:
                break;
        }
        
        // 绘制卡片背景
        gc.setFill(cardColor);
        gc.fillRoundRect(0, 0, 80, 120, 10, 10);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRoundRect(0, 0, 80, 120, 10, 10);
        
        // 添加文本
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.fillText(cardName, 15, 40);
        
        // 添加图标
        if (cardName.equals("直升机")) {
            gc.setFill(Color.BLACK);
            gc.fillOval(25, 70, 30, 15);
            gc.fillRect(35, 65, 10, 5);
        } else if (cardName.equals("沙袋")) {
            gc.setFill(Color.SADDLEBROWN);
            gc.fillRoundRect(25, 70, 30, 20, 5, 5);
            gc.setStroke(Color.BLACK);
            gc.strokeRoundRect(25, 70, 30, 20, 5, 5);
        } else if (cardName.equals("水位上升")) {
            gc.setFill(Color.BLUE);
            gc.fillPolygon(
                new double[]{40, 25, 55},
                new double[]{70, 90, 90},
                3
            );
        }
        
        javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return canvas.snapshot(params, null);
    }
    
    /**
     * 创建默认的洪水卡图片
     * @param tileId 瓦片ID
     * @return 生成的默认图片
     */
    private Image createDefaultFloodCardImage(String tileId) {
        Canvas canvas = new Canvas(80, 120);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // 绘制卡片背景
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRoundRect(0, 0, 80, 120, 10, 10);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRoundRect(0, 0, 80, 120, 10, 10);
        
        // 添加水纹图案
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(1);
        for (int y = 20; y < 120; y += 20) {
            for (int x = 5; x < 80; x += 15) {
                gc.strokeLine(x, y, x + 10, y + 5);
            }
        }
        
        // 添加文本
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.fillText("洪水卡", 15, 40);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gc.fillText(tileId, 30, 80);
        
        javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return canvas.snapshot(params, null);
    }
    
    /**
     * 格式化卡片类型名称为更易读的形式
     * @param cardType 卡片类型
     * @return 格式化后的名称
     */
    private String formatCardTypeName(String cardType) {
        switch (cardType) {
            case "TREASURE": return "宝藏";
            case "HELICOPTER": return "直升机";
            case "SANDBAGS": return "沙袋";
            case "WATER_RISE": return "水位上升";
            case "FLOOD": return "洪水";
            default: return cardType;
        }
    }

    public Pane getView() {
        return viewPane;
    }
}
