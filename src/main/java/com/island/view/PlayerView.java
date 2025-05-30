package com.island.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.island.models.Player;
import com.island.models.PlayerRole;
import com.island.controller.GameController;
import com.island.controller.PlayerController;

public class PlayerView {

    private VBox viewPane; // The root pane for this view component
    private Map<String, BorderPane> playerInfoBoxes; // Map playerName to their display BorderPane
    private GameController gameController;
    
    // 角色图片资源
    private Map<PlayerRole, Image> adventurerImages = new HashMap<>();

    // Constructor
    public PlayerView(GameController gameController) {
        this.gameController = gameController;
        loadImages();
        initialize();
    }
    
    /**
     * 加载角色图片资源
     */
    private void loadImages() {
        try {
            // 加载冒险家图片
            adventurerImages.put(PlayerRole.ENGINEER, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/players/Red.png"))));
            adventurerImages.put(PlayerRole.PILOT, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/players/Blue.png"))));
            adventurerImages.put(PlayerRole.NAVIGATOR, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/players/Yellow.png"))));
            adventurerImages.put(PlayerRole.EXPLORER, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/players/Green.png"))));
            adventurerImages.put(PlayerRole.MESSENGER, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/players/White.png"))));
            adventurerImages.put(PlayerRole.DIVER, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/players/Black.png"))));
        } catch (Exception e) {
            System.err.println("Error loading adventurer images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initialize() {
        viewPane = new VBox(10); // Spacing between player sections
        viewPane.setPadding(new Insets(10));
        viewPane.setAlignment(Pos.TOP_LEFT);
        viewPane.setStyle("-fx-background-color: #f0f0f0;"); // Light grey background
        playerInfoBoxes = new HashMap<>();

        Label title = new Label("玩家信息");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        viewPane.getChildren().add(title);
    }

    public void updatePlayerInfo(Player player) {
        updatePlayerInfo(player, false);
    }

    public void updatePlayerInfo(Player player, boolean isCurrentPlayer) {
        final String playerName = player.getName();
        final PlayerRole playerRole = player.getRole();
        final int handSize = player.getCards().size();
        final String position = (player.getPosition() != null) ? 
            player.getPosition().toString() : "未知位置";
        
        final Color playerColor = getRoleColor(playerRole);

        Platform.runLater(() -> {
            BorderPane playerBox = playerInfoBoxes.get(playerName);
            if (playerBox == null) {
                // Create new box if player not seen before
                playerBox = new BorderPane();
                playerBox.setPadding(new Insets(5));
                playerBox.setStyle("-fx-border-color: black; -fx-border-width: 1;");
                playerInfoBoxes.put(playerName, playerBox);
                
                // 添加玩家信息VBox到BorderPane的右侧
                VBox infoBox = new VBox(3);
                playerBox.setRight(infoBox);
                
                // 创建图片显示区域
                ImageView playerImage = new ImageView();
                playerImage.setFitHeight(100);
                playerImage.setFitWidth(70);
                playerImage.setPreserveRatio(true);
                
                // 设置玩家角色图片
                if (playerRole != null && adventurerImages.containsKey(playerRole)) {
                    playerImage.setImage(adventurerImages.get(playerRole));
                }
                
                playerBox.setLeft(playerImage);
                
                // 添加到主视图
                viewPane.getChildren().add(playerBox);
            } else {
                // Clear existing content before updating
                Node imageView = playerBox.getLeft();
                Node infoBox = playerBox.getRight();
                playerBox.getChildren().clear();
                
                // 如果已有图片和信息盒子，重新添加
                if (imageView != null) {
                    playerBox.setLeft(imageView);
                }
                
                if (infoBox instanceof VBox) {
                    ((VBox) infoBox).getChildren().clear();
                } else {
                    infoBox = new VBox(3);
                    playerBox.setRight(infoBox);
                }
            }
            
            // 获取或创建信息VBox
            VBox infoBox = (VBox) playerBox.getRight();
            if (infoBox == null) {
                infoBox = new VBox(3);
                playerBox.setRight(infoBox);
            }

            // Player Name and Color Indicator
            HBox nameBox = new HBox(5);
            nameBox.setAlignment(Pos.CENTER_LEFT);
            Rectangle colorRect = new Rectangle(15, 15, playerColor);
            Label nameLabel = new Label(playerName);
            nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            nameBox.getChildren().addAll(colorRect, nameLabel);

            // Other Info
            Label roleLabel = new Label("角色: " + getChineseRoleName(playerRole));
            Label handLabel = new Label("卡牌数: " + handSize);
            Label posLabel = new Label("位置: " + position);

            infoBox.getChildren().addAll(nameBox, roleLabel, handLabel, posLabel);
            
            // 更新图片显示
            ImageView playerImage = (ImageView) playerBox.getLeft();
            if (playerImage == null) {
                playerImage = new ImageView();
                playerImage.setFitHeight(100);
                playerImage.setFitWidth(70);
                playerImage.setPreserveRatio(true);
                playerBox.setLeft(playerImage);
            }
            
            if (playerRole != null && adventurerImages.containsKey(playerRole)) {
                playerImage.setImage(adventurerImages.get(playerRole));
            }

            // Highlight current player
            if (isCurrentPlayer) {
                playerBox.setStyle("-fx-border-color: red; -fx-border-width: 3;");
            } else {
                playerBox.setStyle("-fx-border-color: black; -fx-border-width: 1;");
            }
        });
    }
    
    /**
     * 获取角色的中文名称
     */
    private String getChineseRoleName(PlayerRole role) {
        if (role == null) return "未知";
        
        switch (role) {
            case ENGINEER: return "工程师";
            case EXPLORER: return "探险家";
            case PILOT: return "飞行员";
            case MESSENGER: return "信使";
            case NAVIGATOR: return "领航员";
            case DIVER: return "潜水员";
            default: return role.toString();
        }
    }

    public void updateAllPlayers(List<Player> players, Player currentPlayer) {
        Platform.runLater(() -> {
            // Remove all player boxes but keep the title
            if (viewPane.getChildren().size() > 0) {
                viewPane.getChildren().retainAll(viewPane.getChildren().get(0));
            }
            playerInfoBoxes.clear();

            String currentPlayerName = (currentPlayer != null) ? currentPlayer.getName() : null;

            for (Player player : players) {
                String playerName = player.getName();
                updatePlayerInfo(player, playerName.equals(currentPlayerName));
            }
        });
    }

    public void removePlayer(String playerName) {
        Platform.runLater(() -> {
            BorderPane playerBox = playerInfoBoxes.remove(playerName);
            if (playerBox != null) {
                viewPane.getChildren().remove(playerBox);
            }
        });
    }

    public Pane getView() {
        return viewPane;
    }
    
    /**
     * Returns the color corresponding to the player's role
     * @param role Player role
     * @return Corresponding color
     */
    private Color getRoleColor(PlayerRole role) {
        if (role == null) return Color.GRAY;
        
        switch (role) {
            case ENGINEER: return Color.RED;
            case EXPLORER: return Color.GREEN;
            case PILOT: return Color.BLUE;
            case MESSENGER: return Color.GRAY;
            case NAVIGATOR: return Color.YELLOW;
            case DIVER: return Color.BLACK;
            default: return Color.PURPLE;
        }
    }
    
    /**
     * Update the view, get the latest player information from GameController
     */
    public void update() {
        if (gameController != null) {
            // Use GameController methods to get player information
            try {
                PlayerController playerController = gameController.getPlayerController();
                List<Player> players = playerController.getRoom().getPlayers();
                Player currentPlayer = gameController.getCurrentPlayer();
                
                updateAllPlayers(players, currentPlayer);
            } catch (Exception e) {
                System.err.println("Error updating player view: " + e.getMessage());
            }
        }
    }
}
