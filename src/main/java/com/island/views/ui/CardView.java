package com.island.views.ui;

import com.island.controller.CardController;
import com.island.models.card.Card;
import com.island.models.card.CardType;

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

import static com.island.views.ui.IslandView.SCALE;

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

    // 初始化卡牌信息区域
    public void initializeFloodCardsInfo() {
        cardsInfoBox.getChildren().clear();

        // 创建洪水卡牌区域
        HBox floodCardsBox = new HBox(5);
        floodCardsBox.setAlignment(Pos.TOP_CENTER);
        floodCardsBox.setStyle("-fx-padding: 10; -fx-border-color: #cccccc; -fx-border-width: 1;");

        // 洪水卡牌图标，显示洪水卡牌中没有被抽取的数量
        StackPane floodSymbol = createCardSymbol("FloodCardSymbol", floodDeck.size(), CardType.FLOOD);

        // 洪水卡牌展示
        HBox floodCards = new HBox(-30); // 负值使卡牌重叠
        floodCards.setAlignment(Pos.CENTER);

        for (int i = 0; i < floodDiscardPile.size(); i++) {
            Card card = floodDiscardPile.get(i);
            try {
                // 加载板块图片
                String imagePath = "/flood cards/" + card.getName() + ".png";
                Image tileImage = new Image(getClass().getResourceAsStream(imagePath));
                ImageView tileView = new ImageView(tileImage);
                tileView.setFitHeight(204 * SCALE);
                tileView.setFitWidth(147 * SCALE);

                // 最后一张卡片要完全显示
                if (i == floodDiscardPile.size() - 1) {
                    floodCards.getChildren().add(new StackPane(tileView));
                } else {
                    // 创建一个裁剪区域
                    Rectangle clip = new Rectangle(
                            60, // 只显示左边一小部分
                            204 * SCALE
                    );
                    StackPane cardPane = new StackPane(tileView);
                    cardPane.setClip(clip);
                    floodCards.getChildren().add(cardPane);
                }
            } catch (Exception e) {
                System.err.println("无法加载图片 " + card.getName() + ": " + e.getMessage());
            }
        }

        floodCardsBox.getChildren().addAll(floodSymbol, floodCards);
        VBox.setMargin(floodCardsBox, new Insets(0, 0, 10, 0));
        // 添加到卡牌信息区域
        cardsInfoBox.getChildren().add(floodCardsBox);
    }

    public void initializeTreasureCardsInfo() {
        // 创建宝藏卡牌区域
        HBox treasureCardsBox = new HBox(5);
        treasureCardsBox.setAlignment(Pos.TOP_CENTER);
        treasureCardsBox.setStyle("-fx-padding: 10; -fx-border-color: #cccccc; -fx-border-width: 1;");

        // 宝藏卡牌符号
        StackPane treasureSymbol = createCardSymbol("TreasureCardSymbol", treasureDeck.size(), CardType.TREASURE);

        // 弃牌堆卡牌展示
        HBox treasureCards = new HBox(-30); // 负值使卡牌重叠
        treasureCards.setAlignment(Pos.CENTER);

        // 显示位于宝藏弃牌堆的卡牌
        for (int i = 0; i < treasureDiscardPile.size(); i++) {
            Card card = treasureDiscardPile.get(i);
            try {
                // 加载板块图片
                String imagePath = "/treasure cards/" + card.getName() + ".png";
                Image cardImage = new Image(getClass().getResourceAsStream(imagePath));
                ImageView cardView = new ImageView(cardImage);
                cardView.setFitHeight(204 * SCALE);
                cardView.setFitWidth(147 * SCALE);

                // 最后一张卡片要完全显示
                if (i == treasureDiscardPile.size() - 1) {
                    treasureCards.getChildren().add(new StackPane(cardView));
                } else {
                    // 创建一个裁剪区域
                    Rectangle clip = new Rectangle(
                            60, // 只显示左边一小部分
                            204 * SCALE
                    );
                    StackPane cardPane = new StackPane(cardView);
                    cardPane.setClip(clip);
                    treasureCards.getChildren().add(cardPane);
                }
            } catch (Exception e) {
                System.err.println("无法加载图片 " + card.getName() + ": " + e.getMessage());
            }
        }

        treasureCardsBox.getChildren().addAll(treasureSymbol, treasureCards);
        // 添加到卡牌信息区域
        cardsInfoBox.getChildren().add(treasureCardsBox);
    }


    // 创建卡牌符号（带数字）
    private StackPane createCardSymbol(String type, int count, CardType cardType) {
        StackPane cardPane = new StackPane();

        try {
            // 根据卡牌类型选择不同的文件夹路径
            String folderPath = (cardType == CardType.TREASURE) ? "/treasure cards/" : "/flood cards/";
            String imagePath = folderPath + type + ".png";
            Image cardImage = new Image(getClass().getResourceAsStream(imagePath));
            ImageView cardView = new ImageView(cardImage);
            cardView.setFitHeight(204 * SCALE);
            cardView.setFitWidth(147 * SCALE);

            cardPane.getChildren().add(cardView);

            // 如果有数量，添加数字标签
            if (count > 0) {
                Label countLabel = new Label(String.valueOf(count));
                countLabel.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-padding: 2px; -fx-text-fill: black;");
                StackPane.setAlignment(countLabel, Pos.BOTTOM_RIGHT);
                StackPane.setMargin(countLabel, new Insets(0, 5, 5, 0));

                cardPane.getChildren().add(countLabel);
            }
        } catch (Exception e) {
            // 如果图片加载失败，显示文本
            Rectangle cardRect = new Rectangle(60, 80);
            cardRect.setFill(Color.LIGHTGRAY);
            cardRect.setStroke(Color.BLACK);

            Label typeLabel = new Label(type);

            cardPane.getChildren().addAll(cardRect, typeLabel);

            // 如果有数量，添加数字标签
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
     * 关闭卡牌视图，清理资源
     */
    public void shutdown() {
        // 清理卡牌信息区域资源
        if (cardsInfoBox != null) {
            cardsInfoBox.getChildren().clear();
        }
        // 清理控制器引用
        cardController = null;
    }
}
