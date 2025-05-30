package com.forbiddenisland.views.ui;

import com.forbiddenisland.controllers.island.IslandController;
import com.forbiddenisland.models.*;
import com.forbiddenisland.models.adventurers.Player;
import com.forbiddenisland.models.adventurers.PlayerRole;
import com.forbiddenisland.models.island.Island;
import com.forbiddenisland.models.island.Position;
import com.forbiddenisland.models.island.Tile;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IslandView {
    private IslandController islandController;
    private Island island;
    private GridPane boardGrid;
    private VBox waterLevelBox;
    private ImageView waterLevelView;  // 水位图片视图
    private Rectangle waterLevelIndicator; // 水位指示器
    private StackPane selectedTilePane; // 当前选中的板块
    private List<Tile> highlightedTiles; // 保存当前高亮的板块列表
    static final double SCALE = 0.55;  // 缩放比例
    private static final double TILE_SIZE = 147 * SCALE;
    private static final String SELECTED_STYLE = "-fx-border-color: red; -fx-border-width: 2; -fx-border-style: solid;";

    public IslandView(GridPane boardGrid, VBox waterLevelBox) {
        this.boardGrid = boardGrid;
        this.waterLevelBox = waterLevelBox;

        boardGrid.setHgap(5);
        boardGrid.setVgap(5);
        boardGrid.setAlignment(Pos.CENTER);

        waterLevelBox.setAlignment(Pos.TOP_CENTER);
        waterLevelBox.setPadding(new Insets(10));
    }

    public void setIslandController(IslandController islandController) {
        this.islandController = islandController;
        island = islandController.getIsland();
    }

    public void initializeBoard() {
        Map<Position, Tile> tiles = island.getTiles();

        // 保存当前高亮的板块
        List<Tile> tilesToHighlight = null;
        if (highlightedTiles != null && !highlightedTiles.isEmpty()) {
            tilesToHighlight = new ArrayList<>(highlightedTiles);
        }

        // Clear existing tiles first
        boardGrid.getChildren().clear();
        selectedTilePane = null;

        Room room = islandController.getRoom();
        // 遍历所有位置和板块
        for (Map.Entry<Position, Tile> entry : tiles.entrySet()) {
            Position pos = entry.getKey();
            Tile tile = entry.getValue();

            if (!tile.isSunk()) {
                try {
                    // 创建一个StackPane用于叠放板块和玩家棋子
                    StackPane tileStack = new StackPane();

                    // 加载板块图片
                    String imagePath = tile.isNormal() ? ("/islands/" + tile.getName() + ".png") : ("/islands/" + tile.getName() + "_flood.png");
                    Image tileImage = new Image(getClass().getResourceAsStream(imagePath));
                    ImageView tileView = new ImageView(tileImage);

                    // 设置图片大小
                    tileView.setFitWidth(TILE_SIZE);
                    tileView.setFitHeight(TILE_SIZE);

                    // 将板块图片添加到StackPane
                    tileStack.getChildren().add(tileView);

                    // 检查是否有玩家在这个位置
                    checkPlayersOnTile(room, pos, tileStack);

                    // 添加点击事件处理
                    tileStack.setOnMouseClicked(event -> {
                        // 清除之前的所有边框
                        clearAllBoarders();
                        // 添加新的边框
                        addBoarder(tile);
                        islandController.handleTileClick(tile);
                    });

                    // 将StackPane添加到网格
                    boardGrid.add(tileStack, pos.getX(), pos.getY());

                } catch (Exception e) {
                    System.err.println("无法加载图片 " + tile.getName() + ": " + e.getMessage());
                }
            } else {
                // 创建一个StackPane用于叠放板块和玩家棋子
                StackPane tileStack = new StackPane();

                // 检查是否有玩家在这个位置
                checkPlayersOnTile(room, pos, tileStack);
                // 将StackPane添加到网格
                boardGrid.add(tileStack, pos.getX(), pos.getY());
            }
        }

        // 放置Treasure
        String[] treasureNames = islandController.getTreasures();
        Position[] treasurePositions = new Position[] {
                new Position(0, 0), new Position(0, 5),
                new Position(5, 0), new Position(5, 5)
        };
        for (int i = 0; i < treasureNames.length; i++) {
            try {
                String imagePath = "/treasures/" + treasureNames[i] + ".png";
                Image treasureImage = new Image(getClass().getResourceAsStream(imagePath));
                ImageView treasureView = new ImageView(treasureImage);
                treasureView.setFitWidth(TILE_SIZE);
                treasureView.setFitHeight(TILE_SIZE);
                boardGrid.add(treasureView, treasurePositions[i].getX(), treasurePositions[i].getY());
            } catch (Exception e) {
                System.err.println("无法加载图片 " + treasureNames[i] + ": " + e.getMessage());
            }
        }

        // 恢复之前高亮的板块
        if (tilesToHighlight != null) {
            addBoarders(tilesToHighlight);
        }
    }

    private void checkPlayersOnTile(Room room, Position pos, StackPane tileStack) {
        for (Player player : room.getPlayers()) {
            if (player.getPosition() != null && player.getPosition().equals(pos)) {
                try {
                    // 加载玩家棋子图片
                    String playerImagePath = "/players/" + PlayerRole.getColor(player.getRole()) + ".png";
                    Image playerImage = new Image(getClass().getResourceAsStream(playerImagePath));
                    ImageView playerView = new ImageView(playerImage);

                    // 设置棋子大小（比板块小一些）
                    playerView.setFitWidth(73 * 0.4);
                    playerView.setFitHeight(131 * 0.4);

                    // 将玩家棋子添加到StackPane
                    tileStack.getChildren().add(playerView);
                } catch (Exception e) {
                    System.err.println("无法加载玩家棋子图片: " + e.getMessage());
                }
            }
        }
    }

    public void addBoarders(List<Tile> tiles) {
        // 保存高亮的板块列表
        if (highlightedTiles == null) {
            highlightedTiles = new ArrayList<>();
        } else {
            highlightedTiles.clear();
        }
        highlightedTiles.addAll(tiles);
        
        for (Tile tile : tiles) {
            addBoarder(tile);
        }
    }

    private void addBoarder(Tile tile) {
        // 在网格中找到对应的StackPane并添加边框
        for (javafx.scene.Node node : boardGrid.getChildren()) {
            if (node instanceof StackPane tilePane) {
                Integer columnIndex = GridPane.getColumnIndex(tilePane);
                Integer rowIndex = GridPane.getRowIndex(tilePane);

                if (columnIndex != null && rowIndex != null &&
                        columnIndex == tile.getPosition().getX() &&
                        rowIndex == tile.getPosition().getY()) {
                    tilePane.setStyle(SELECTED_STYLE);
                    break;
                }
            }
        }
    }

    // 清除所有边框
    public void clearAllBoarders() {
        if (highlightedTiles != null) {
            highlightedTiles.clear();
        }
        
        // 清除所有板块的边框样式
        for (javafx.scene.Node node : boardGrid.getChildren()) {
            if (node instanceof StackPane tilePane) {
                tilePane.setStyle("");
            }
        }
    }

    public void initWaterLevel() {
        waterLevelBox.getChildren().clear();
        if (islandController != null) {
            // 加载水位条图片
            Image waterLevelImage = new Image(getClass().getResourceAsStream("/islands/flood_meter.png"));
            waterLevelView = new ImageView(waterLevelImage);
            waterLevelView.setFitHeight(250);  // 设置高度
            waterLevelView.setPreserveRatio(true);  // 保持宽高比

            // 添加水位指示器
            StackPane waterLevelStack = new StackPane();
            waterLevelStack.getChildren().add(waterLevelView);

            // 创建水位指示器（半透明红色矩形）
            waterLevelIndicator = new Rectangle(40, 15);
            waterLevelIndicator.setFill(Color.rgb(255, 0, 0, 0.5)); // 半透明红色
            waterLevelIndicator.setStroke(Color.BLACK);
            waterLevelIndicator.setStrokeWidth(0);

            // 与顶部有160px的间距
            VBox.setMargin(waterLevelStack, new Insets(140, 0, 0, 0));

            // 根据当前水位设置指示器位置
            updateWaterLevelIndicator();

            waterLevelStack.getChildren().add(waterLevelIndicator);

            waterLevelBox.getChildren().addAll(waterLevelStack);
        }
    }
    // 更新水位指示器位置
    public void updateWaterLevelIndicator() {
        int waterLevel = islandController.getWaterLevel();
        // 水位从1到10，需要映射到水位条上的位置
        double yOffset = (waterLevel - 1) * 21 + 28; // 根据实际水位条图片调整

        // 设置指示器位置（相对于水位条）
        StackPane.setAlignment(waterLevelIndicator, Pos.BOTTOM_LEFT);
        StackPane.setMargin(waterLevelIndicator, new Insets(0, 0, yOffset, 0));
    }

    public Background getBackground() {
        String imagePath;
        int waterLevel = islandController.getWaterLevel();
        if (waterLevel <= 2) {
            imagePath = "/islands/bg_2.png";
        } else if (waterLevel <= 5) {
            imagePath = "/islands/bg_3.png";
        } else if (waterLevel <= 7) {
            imagePath = "/islands/bg_4.png";
        } else {
            imagePath = "/islands/bg_5.png";
        }
        // Load background image
        Image backgroundImage = new Image(getClass().getResourceAsStream(imagePath));

        // Create background
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO,
                        false, false, true, true)
        );
        return new Background(background);
    }

    /**
     * 关闭岛屿视图，清理资源
     */
    public void shutdown() {
        // 清理网格资源
        if (boardGrid != null) {
            boardGrid.getChildren().clear();
        }
        // 清理水位显示资源
        if (waterLevelBox != null) {
            waterLevelBox.getChildren().clear();
        }
        // 清理控制器引用
        islandController = null;
    }
}
