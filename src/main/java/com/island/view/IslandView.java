package com.island.view;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import com.island.controller.GameController;
import com.island.controller.PlayerController;
import com.island.controller.IslandController;
import com.island.model.Tile;
import com.island.model.Player;
import com.island.model.Position;
import com.island.model.Island;
import com.island.model.PlayerRole;

public class IslandView {

    private GridPane gridPane; // The main layout for the island tiles
    private Pane viewPane; // The root pane for this view component
    // GameController reference
    private GameController gameController;

    // Constants for tile display (example)
    private static final double TILE_SIZE = 80.0;
    private static final Color NORMAL_TILE_COLOR = Color.SANDYBROWN;
    private static final Color FLOODED_TILE_COLOR = Color.LIGHTBLUE;
    private static final Color SUNK_TILE_COLOR = Color.DARKBLUE;
    private static final Color SHORED_UP_BORDER_COLOR = Color.YELLOWGREEN;
    private static final double SHORED_UP_BORDER_WIDTH = 3.0;
    
    // 图片资源缓存
    private Map<String, Image> normalTileImages = new HashMap<>();
    private Map<String, Image> floodedTileImages = new HashMap<>();
    private Map<String, Image> sunkTileImages = new HashMap<>();
    private Map<PlayerRole, Image> playerPawnImages = new HashMap<>();
    private Image mapBackgroundImage;

    // Constructor
    public IslandView(GameController gameController) {
        this.gameController = gameController;
        loadImages();
        initialize();
    }
    
    /**
     * 加载所有图片资源
     */
    private void loadImages() {
        try {
            // 加载背景地图
            mapBackgroundImage = new Image(getClass().getResourceAsStream("/image/Map/Arena.jpg"));
            
            // 加载普通地形图片
            for (int i = 1; i <= 32; i++) {
                String path = "/image/Tiles/" + i + ".png";
                normalTileImages.put(String.valueOf(i), new Image(getClass().getResourceAsStream(path)));
            }
            
            // 加载被淹的地形图片
            for (int i = 1; i <= 32; i++) {
                String path = "/image/Flood/" + i + ".png";
                floodedTileImages.put(String.valueOf(i), new Image(getClass().getResourceAsStream(path)));
            }
            
            // 加载沉没的地形图片
            for (int i = 1; i <= 32; i++) {
                String path = "/image/SubmersedTiles/" + i + ".png";
                sunkTileImages.put(String.valueOf(i), new Image(getClass().getResourceAsStream(path)));
            }
            
            // 加载角色棋子图片
            playerPawnImages.put(PlayerRole.ENGINEER, new Image(getClass().getResourceAsStream("/image/Pawns/Engineer.png")));
            playerPawnImages.put(PlayerRole.PILOT, new Image(getClass().getResourceAsStream("/image/Pawns/Pilot.png")));
            playerPawnImages.put(PlayerRole.NAVIGATOR, new Image(getClass().getResourceAsStream("/image/Pawns/Navigator.png")));
            playerPawnImages.put(PlayerRole.EXPLORER, new Image(getClass().getResourceAsStream("/image/Pawns/Explorer.png")));
            playerPawnImages.put(PlayerRole.MESSENGER, new Image(getClass().getResourceAsStream("/image/Pawns/Messenger.png")));
            playerPawnImages.put(PlayerRole.DIVER, new Image(getClass().getResourceAsStream("/image/Pawns/Diver.png")));
            
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initialize() {
        gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        // 设置背景
        // gridPane.setStyle("-fx-background-color: grey;");

        // Populate the grid with initial placeholder tiles (replace with actual game data)
        // This needs the actual board layout (e.g., 6x6)
        int boardSize = 6; // Example size
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                // Create a placeholder representation for each tile
                Pane tilePane = createTileRepresentation(null, row, col); // Pass null for initial empty state
                gridPane.add(tilePane, col, row);
            }
        }

        // 将背景图片和岛屿网格包装在一个Pane中
        viewPane = new Pane();
        
        // 添加背景图片
        if (mapBackgroundImage != null) {
            ImageView backgroundImageView = new ImageView(mapBackgroundImage);
            backgroundImageView.setFitWidth(500);  // 根据实际情况调整大小
            backgroundImageView.setFitHeight(500);
            backgroundImageView.setPreserveRatio(true);
            viewPane.getChildren().add(backgroundImageView);
        }
        
        // 在背景上添加网格
        viewPane.getChildren().add(gridPane);
        
        // 调整网格位置，使其位于地图上的合适位置
        gridPane.setLayoutX(50);  // 根据实际背景调整
        gridPane.setLayoutY(50);
    }

    private Pane createTileRepresentation(Object tile, int row, int col) {
        Pane tilePane = new Pane();
        tilePane.setPrefSize(TILE_SIZE, TILE_SIZE);
        
        // 默认使用一个空矩形作为占位符
        Rectangle background = new Rectangle(TILE_SIZE, TILE_SIZE);
        background.setFill(Color.TRANSPARENT);
        background.setStroke(Color.BLACK);
        
        // 添加到pane中
        tilePane.getChildren().add(background);

        // Add click listener
        tilePane.setOnMouseClicked(event -> {
            System.out.println("Clicked on tile at (" + row + ", " + col + ")");
            // Pass interaction to the controller
            if (gameController != null) {
                gameController.getIslandController().handleTileClick(new Position(row, col));
            }
        });

        return tilePane;
    }

    public void updateTileView(int row, int col, Tile tile) {
        // Find the corresponding Pane in the gridPane
        // This requires a way to map row/col back to the Node in the grid
        Platform.runLater(() -> {
            // Example: Get node by row/col (might need adjustment based on gridPane structure)
            Node node = getNodeByRowColumnIndex(row, col, gridPane);
            if (node instanceof Pane) {
                Pane tilePane = (Pane) node;
                // 清除现有内容
                tilePane.getChildren().clear();
                
                String tileId = tile.getName();
                Tile.State state = tile.getState();
                boolean isShoredUp = tile.isShoredUp();
                
                ImageView tileImageView = new ImageView();
                tileImageView.setFitWidth(TILE_SIZE);
                tileImageView.setFitHeight(TILE_SIZE);
                tileImageView.setPreserveRatio(true);

                // 根据瓦片状态选择图片
                switch (state) {
                    case NORMAL:
                        if (normalTileImages.containsKey(tileId)) {
                            tileImageView.setImage(normalTileImages.get(tileId));
                        } else {
                            // 如果找不到图片，使用默认矩形
                            Rectangle rect = new Rectangle(TILE_SIZE, TILE_SIZE);
                            rect.setFill(NORMAL_TILE_COLOR);
                            tilePane.getChildren().add(rect);
                        }
                        break;
                    case FLOODED:
                        if (floodedTileImages.containsKey(tileId)) {
                            tileImageView.setImage(floodedTileImages.get(tileId));
                        } else {
                            Rectangle rect = new Rectangle(TILE_SIZE, TILE_SIZE);
                            rect.setFill(FLOODED_TILE_COLOR);
                            tilePane.getChildren().add(rect);
                        }
                        break;
                    case SUNK:
                        if (sunkTileImages.containsKey(tileId)) {
                            tileImageView.setImage(sunkTileImages.get(tileId));
                        } else {
                            Rectangle rect = new Rectangle(TILE_SIZE, TILE_SIZE);
                            rect.setFill(SUNK_TILE_COLOR);
                            tilePane.getChildren().add(rect);
                        }
                        break;
                }
                
                // 添加图片到面板
                if (tileImageView.getImage() != null) {
                    tilePane.getChildren().add(tileImageView);
                }
                
                // 如果是加固的瓷砖，添加边框
                if (isShoredUp && state != Tile.State.SUNK) {
                    Rectangle shoreUpIndicator = new Rectangle(TILE_SIZE, TILE_SIZE);
                    shoreUpIndicator.setFill(Color.TRANSPARENT);
                    shoreUpIndicator.setStroke(SHORED_UP_BORDER_COLOR);
                    shoreUpIndicator.setStrokeWidth(SHORED_UP_BORDER_WIDTH);
                    tilePane.getChildren().add(shoreUpIndicator);
                }
                
                // 显示瓦片名称（可以根据需要删除或保留）
                Label nameLabel = new Label(tileId);
                nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                nameLabel.setLayoutX(5);
                nameLabel.setLayoutY(5);
                tilePane.getChildren().add(nameLabel);

            } else {
                System.err.println("Could not find Pane at row " + row + ", col " + col);
            }
        });
    }

    public void updatePlayerMarker(Player player, Position newPosition) {
        Platform.runLater(() -> {
            System.out.println("Updating player marker for " + player.getName() + " at " + newPosition);
            
            int row = newPosition.getRow();
            int col = newPosition.getCol();
            Node node = getNodeByRowColumnIndex(row, col, gridPane);
            if (node instanceof Pane) {
                Pane tilePane = (Pane) node;
                // Remove previous markers of this player before adding
                for (int i = tilePane.getChildren().size() - 1; i >= 0; i--) {
                    if (tilePane.getChildren().get(i) instanceof Circle ||
                        (tilePane.getChildren().get(i) instanceof ImageView && 
                         ((ImageView)tilePane.getChildren().get(i)).getUserData() != null && 
                         ((ImageView)tilePane.getChildren().get(i)).getUserData().equals("playerPawn"))) {
                        tilePane.getChildren().remove(i);
                    }
                }
                
                // 获取玩家角色
                PlayerRole role = player.getRole();
                
                // 使用对应的棋子图片
                if (role != null && playerPawnImages.containsKey(role)) {
                    ImageView pawnImageView = new ImageView(playerPawnImages.get(role));
                    pawnImageView.setFitWidth(TILE_SIZE / 2);
                    pawnImageView.setFitHeight(TILE_SIZE / 2);
                    pawnImageView.setPreserveRatio(true);
                    pawnImageView.setUserData("playerPawn"); // 标记为玩家棋子
                    tilePane.getChildren().add(pawnImageView);
                    
                    // 放置在瓦片中心
                    pawnImageView.setLayoutX(TILE_SIZE / 4);
                    pawnImageView.setLayoutY(TILE_SIZE / 4);
                } else {
                    // 如果没有图片，使用默认圆形标记
                    Color playerColor = Color.RED; // 根据角色设置颜色
                    if (role != null) {
                        switch (role) {
                            case ENGINEER: playerColor = Color.RED; break;
                            case EXPLORER: playerColor = Color.GREEN; break;
                            case PILOT: playerColor = Color.BLUE; break;
                            case MESSENGER: playerColor = Color.GRAY; break;
                            case NAVIGATOR: playerColor = Color.YELLOW; break;
                            case DIVER: playerColor = Color.BLACK; break;
                            default: playerColor = Color.PURPLE; break;
                        }
                    }
                    
                    Circle playerMarker = new Circle(TILE_SIZE / 4, playerColor);
                    tilePane.getChildren().add(playerMarker);
                    
                    // 放置在瓦片中心
                    playerMarker.setCenterX(TILE_SIZE / 2);
                    playerMarker.setCenterY(TILE_SIZE / 2);
                }
            }
        });
    }

    public void highlightTiles(List<Position> positions, String highlightType) {
        clearHighlights(); // Clear previous highlights first
        Platform.runLater(() -> {
            Color highlightColor = Color.YELLOW; // Default highlight
            if ("shore_up".equals(highlightType)) {
                highlightColor = Color.LIGHTGREEN;
            }
            for (Position pos : positions) {
                Node node = getNodeByRowColumnIndex(pos.getRow(), pos.getCol(), gridPane);
                if (node instanceof Pane) {
                    Pane tilePane = (Pane) node;
                    
                    // 创建一个高亮矩形框
                    Rectangle highlight = new Rectangle(TILE_SIZE, TILE_SIZE);
                    highlight.setFill(Color.TRANSPARENT);
                    highlight.setStroke(highlightColor);
                    highlight.setStrokeWidth(3.0);
                    highlight.setUserData("highlight"); // 使用userData标记为高亮元素
                    
                    // 将高亮框添加到图块顶部
                    tilePane.getChildren().add(highlight);
                    
                    System.out.println("Highlighting tile at " + pos + " for " + highlightType);
                }
            }
            System.out.println("Highlighting tiles. Count: " + positions.size());
        });
    }

    public void clearHighlights() {
        Platform.runLater(() -> {
            for (Node node : gridPane.getChildren()) {
                if (node instanceof Pane) {
                    Pane tilePane = (Pane) node;
                    // 移除所有被标记为高亮的元素
                    tilePane.getChildren().removeIf(child -> 
                        child.getUserData() != null && "highlight".equals(child.getUserData()));
                }
            }
            System.out.println("Cleared highlights.");
        });
    }

    public Pane getView() {
        return viewPane;
    }
    
    /**
     * Update the island view, get the latest island status information from GameController
     */
    public void update() {
        if (gameController != null) {
            try {
                IslandController islandController = gameController.getIslandController();
                
                // Get island object
                Island island = islandController.getIsland();
                if (island == null) return;
                
                // Update all tile states
                for (int row = 0; row < island.getSize(); row++) {
                    for (int col = 0; col < island.getSize(); col++) {
                        Tile tile = island.getTile(new Position(row, col));
                        if (tile != null) {
                            updateTileView(row, col, tile);
                        }
                    }
                }
                
                // Update player position markers
                PlayerController playerController = gameController.getPlayerController();
                if (playerController != null) {
                    List<Player> players = playerController.getRoom().getPlayers();
                    for (Player player : players) {
                        // Ensure player position is updated correctly
                        if (player.getPosition() != null) {
                            System.out.println("Updating position for player: " + player.getName());
                            updatePlayerMarker(player, player.getPosition());
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error updating island view: " + e.getMessage());
            }
        }
    }

    private Node getNodeByRowColumnIndex(final int row, final int column, GridPane gridPane) {
        for (Node node : gridPane.getChildren()) {
            Integer r = GridPane.getRowIndex(node);
            Integer c = GridPane.getColumnIndex(node);
            int nodeRow = (r == null) ? 0 : r;
            int nodeCol = (c == null) ? 0 : c;
            if (nodeRow == row && nodeCol == column) {
                return node;
            }
        }
        return null;
    }
}
