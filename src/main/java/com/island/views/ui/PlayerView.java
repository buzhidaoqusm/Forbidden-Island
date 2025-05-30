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
    private ImageView selectedCardView; // 当前选中的卡牌
    private Room room;
    private static final String SELECTED_STYLE = "-fx-border-color: red; -fx-border-width: 2; -fx-border-style: solid;";


    public PlayerView(VBox playersInfoBox) {
        this.playersInfoBox = playersInfoBox;
        playersInfoBox.setPadding(new Insets(10));
        playersInfoBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1px;");
    }

    public void initPlayersInfo() {
        playersInfoBox.getChildren().clear();

        // 获取房间中的玩家
        List<Player> players = room.getPlayers();

        // 为每个玩家创建一行信息
        for (Player player : players) {
            HBox playerRow = new HBox(15);
            playerRow.setAlignment(Pos.CENTER_LEFT);

            // 玩家名称
            Label nameLabel = new Label(player.getName());
            nameLabel.setMinWidth(100);
            nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

            // 玩家角色
            HBox roleBox = new HBox(25);
            roleBox.setAlignment(Pos.CENTER_LEFT);

            PlayerRole role = player.getRole();
//            String roleName = role != null ? role.getDisplayName() : "未选择";

            try {
                // 加载角色图片
                String imagePath = "/adventurers/" + (role != null ? role.name() : "unknown") + ".png";
                Image roleImage = new Image(getClass().getResourceAsStream(imagePath));
                ImageView roleView = new ImageView(roleImage);
                roleView.setFitHeight(204 * SCALE);
                roleView.setFitWidth(147 * SCALE);

                roleBox.getChildren().addAll(roleView);
//                Label roleLabel = new Label(roleName);
//
//                roleBox.getChildren().addAll(roleView, roleLabel);
            } catch (Exception e) {
                // 如果图片加载失败，只显示文本
//                roleBox.getChildren().add(new Label(roleName));
            }

            // 玩家卡牌
            FlowPane cardsPane = new FlowPane(5, 5);
            cardsPane.setPrefWrapLength(400);

            List<Card> cards = player.getCards();
            if (cards != null && !cards.isEmpty()) {
                for (Card card : cards) {
                    try {
                        // 加载卡牌图片
                        String imagePath = "/treasure cards/" + card.getName() + ".png";
                        Image cardImage = new Image(getClass().getResourceAsStream(imagePath));
                        ImageView cardView = new ImageView(cardImage);
                        cardView.setFitHeight(204 * SCALE);
                        cardView.setFitWidth(147 * SCALE);

                        // 创建一个StackPane来包装卡牌图片，以便添加边框
                        StackPane cardPane = new StackPane(cardView);

                        // 为卡牌添加点击事件
                        cardView.setOnMouseClicked(event -> {
                            // 只有card的owner才能点击
                            if (card.getBelongingPlayer().equals(room.getCurrentProgramPlayer().getName())) {
                                // 移除之前选中卡牌的边框
                                if (selectedCardView != null) {
                                    selectedCardView.setStyle("");
                                    selectedCardView.getParent().setStyle("");
                                }
                                // 设置新选中的卡牌
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
                        // 如果图片加载失败，显示卡牌名称
                        Label cardLabel = new Label(card.getName());
                        cardLabel.setPadding(new Insets(5));
                        cardLabel.setStyle("-fx-border-color: #cccccc; -fx-background-color: #ffffff;");
                        cardsPane.getChildren().add(cardLabel);
                    }
                }
            } else {
//                cardsPane.getChildren().add(new Label("无卡牌"));
            }

            // 将所有元素添加到玩家行
            playerRow.getChildren().addAll(nameLabel, roleBox, cardsPane);

            if (!player.getCapturedTreasures().isEmpty()) {
                for (TreasureType treasureType : player.getCapturedTreasures()) {
                    // 加载卡牌图片
                    String imagePath = "/treasures/" + treasureType.getDisplayName() + ".png";
                    Image treasureImage = new Image(getClass().getResourceAsStream(imagePath));
                    ImageView treasureView = new ImageView(treasureImage);
                    treasureView.setFitHeight(120 * 0.7);
                    treasureView.setFitWidth(88 * 0.7);

                    // 将宝藏图片添加到整个playerrow的右上角
                    StackPane treasurePane = new StackPane(treasureView);
                    treasurePane.setAlignment(Pos.TOP_RIGHT);
                    playerRow.getChildren().add(treasurePane);
                }

            }
            // 添加到玩家信息区域
            playersInfoBox.getChildren().add(playerRow);
        }
    }
    public void setPlayerController(PlayerController playerController) {
        this.playerController = playerController;
        room = playerController.getRoom();
    }
}
