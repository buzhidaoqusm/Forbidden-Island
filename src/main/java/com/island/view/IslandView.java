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
import com.island.controller.*;
import com.island.controller.PlayerController;
import com.island.controller.IslandController;
import com.island.model.*;
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
            // 定义可能的路径
            String[] mapBgPaths = {
                "/Map/Arena.jpg",
                "/images/Map/Arena.jpg",
                "/Map/background.jpg",
                "/images/background.jpg",
                "/background.jpg",
                "/map.jpg"
            };
            
            // 尝试加载背景地图
            for (String path : mapBgPaths) {
                try {
                    mapBackgroundImage = new Image(getClass().getResourceAsStream(path));
                    if (mapBackgroundImage != null && !mapBackgroundImage.isError()) {
                        System.out.println("成功加载地图背景: " + path);
                        break;
                    }
                } catch (Exception e) {
                    // 继续尝试下一个路径
                }
            }
            
            // 如果背景加载失败，创建默认背景
            if (mapBackgroundImage == null || mapBackgroundImage.isError()) {
                // 创建一个渐变背景作为备用
                javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(500, 500);
                javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
                
                // 绘制渐变背景
                javafx.scene.paint.LinearGradient gradient = 
                    new javafx.scene.paint.LinearGradient(0, 0, 0, 500, false, 
                    javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new javafx.scene.paint.Stop(0, Color.LIGHTBLUE),
                    new javafx.scene.paint.Stop(1, Color.BLUE));
                
                gc.setFill(gradient);
                gc.fillRect(0, 0, 500, 500);
                
                // 将Canvas转换为Image
                javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                params.setFill(Color.TRANSPARENT);
                mapBackgroundImage = canvas.snapshot(params, null);
                
                System.out.println("使用默认地图背景");
            }
            
            // 加载普通地形图片
            for (int i = 1; i <= 32; i++) {
                String tileId = String.valueOf(i);
                String[] paths = {
                    "/Tiles/" + i + ".png",
                    "/images/Tiles/" + i + ".png", 
                    "/tiles/" + i + ".png",
                    "/Tiles/tile" + i + ".png",
                    "/images/tile" + i + ".png",
                    "/" + i + ".png"
                };
                
                boolean loaded = false;
                for (String path : paths) {
                    try {
                        Image image = new Image(getClass().getResourceAsStream(path));
                        if (image != null && !image.isError()) {
                            normalTileImages.put(tileId, image);
                            loaded = true;
                            System.out.println("成功加载普通地形图: " + path);
                            break;
                        }
                    } catch (Exception e) {
                        // 继续尝试下一个路径
                    }
                }
                
                // 如果无法加载，创建默认图形
                if (!loaded) {
                    javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(TILE_SIZE, TILE_SIZE);
                    javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
                    
                    gc.setFill(NORMAL_TILE_COLOR);
                    gc.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
                    gc.setStroke(Color.BLACK);
                    gc.strokeRect(0, 0, TILE_SIZE, TILE_SIZE);
                    gc.setFill(Color.BLACK);
                    gc.fillText(tileId, TILE_SIZE/2 - 5, TILE_SIZE/2 + 5);
                    
                    javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                    params.setFill(Color.TRANSPARENT);
                    normalTileImages.put(tileId, canvas.snapshot(params, null));
                    System.out.println("使用默认普通地形图 " + tileId);
                }
            }
            
            // 加载被淹的地形图片
            for (int i = 1; i <= 32; i++) {
                String tileId = String.valueOf(i);
                String[] paths = {
                    "/Flood/" + i + ".png",
                    "/images/Flood/" + i + ".png",
                    "/flood/" + i + ".png"
                };
                
                boolean loaded = false;
                for (String path : paths) {
                    try {
                        Image image = new Image(getClass().getResourceAsStream(path));
                        if (image != null && !image.isError()) {
                            floodedTileImages.put(tileId, image);
                            loaded = true;
                            break;
                        }
                    } catch (Exception e) {
                        // 继续尝试下一个路径
                    }
                }
                
                // 如果无法加载，创建默认图形
                if (!loaded) {
                    // 使用普通地形图作为基础，添加蓝色半透明覆盖层
                    javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(TILE_SIZE, TILE_SIZE);
                    javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

                    // 如果有普通地形图，绘制它
                    if (normalTileImages.containsKey(tileId)) {
                        gc.drawImage(normalTileImages.get(tileId), 0, 0, TILE_SIZE, TILE_SIZE);
                    } else {
                        gc.setFill(NORMAL_TILE_COLOR);
                        gc.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
                        gc.setStroke(Color.BLACK);
                        gc.strokeRect(0, 0, TILE_SIZE, TILE_SIZE);
                        gc.setFill(Color.BLACK);
                        gc.fillText(tileId, TILE_SIZE/2 - 5, TILE_SIZE/2 + 5);
                    }

                    // 添加蓝色半透明覆盖层表示被淹
                    gc.setFill(Color.rgb(0, 100, 255, 0.5)); // 半透明蓝色
                    gc.fillRect(0, 0, TILE_SIZE, TILE_SIZE);

                    javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                    params.setFill(Color.TRANSPARENT);
                    floodedTileImages.put(tileId, canvas.snapshot(params, null));
                    System.out.println("使用默认被淹地形图 " + tileId);
                }
            }
            
            // 加载沉没的地形图片
            for (int i = 1; i <= 32; i++) {
                String tileId = String.valueOf(i);
                String[] paths = {
                    "/SubmersedTiles/" + i + ".png",
                    "/images/SubmersedTiles/" + i + ".png",
                    "/submersed/" + i + ".png"
                };
                
                boolean loaded = false;
                for (String path : paths) {
                    try {
                        Image image = new Image(getClass().getResourceAsStream(path));
                        if (image != null && !image.isError()) {
                            sunkTileImages.put(tileId, image);
                            loaded = true;
                            break;
                        }
                    } catch (Exception e) {
                        // 继续尝试下一个路径
                    }
                }
                
                // 如果无法加载，创建默认图形
                if (!loaded) {
                    javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(TILE_SIZE, TILE_SIZE);
                    javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

                    // 深蓝色表示沉没
                    gc.setFill(SUNK_TILE_COLOR);
                    gc.fillRect(0, 0, TILE_SIZE, TILE_SIZE);

                    // 添加一些波纹效果
                    gc.setStroke(Color.LIGHTBLUE);
                    gc.setLineWidth(1.0);
                    for (int j = 0; j < 5; j++) {
                        gc.strokeOval(10 + j*5, 10 + j*5, TILE_SIZE - 20 - j*10, TILE_SIZE - 20 - j*10);
                    }

                    javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                    params.setFill(Color.TRANSPARENT);
                    sunkTileImages.put(tileId, canvas.snapshot(params, null));
                    System.out.println("使用默认沉没地形图 " + tileId);
                }
            }

            // 加载角色棋子图片
            try {
                PlayerRole[] roles = {
                    PlayerRole.ENGINEER, 
                    PlayerRole.PILOT, 
                    PlayerRole.NAVIGATOR, 
                    PlayerRole.EXPLORER, 
                    PlayerRole.MESSENGER, 
                    PlayerRole.DIVER
                };
                
                Color[] roleColors = {
                    Color.RED,      // 工程师
                    Color.BLUE,     // 飞行员
                    Color.YELLOW,   // 领航员
                    Color.GREEN,    // 探险家
                    Color.GRAY,     // 信使
                    Color.BLACK     // 潜水员
                };
                
                for (int i = 0; i < roles.length; i++) {
                    PlayerRole role = roles[i];
                    String roleName = role.toString().toLowerCase();
                    roleName = roleName.substring(0, 1).toUpperCase() + roleName.substring(1);
                    
                    String[] paths = {
                        "/Pawns/" + roleName + ".png",
                        "/images/Pawns/" + roleName + ".png",
                        "/pawns/" + roleName.toLowerCase() + ".png",
                        "/Figurines/" + roleName + ".png"
                    };
                    
                    boolean loaded = false;
                    for (String path : paths) {
                        try {
                            Image image = new Image(getClass().getResourceAsStream(path));
                            if (image != null && !image.isError()) {
                                playerPawnImages.put(role, image);
                                loaded = true;
                                break;
                            }
                        } catch (Exception e) {
                            // 继续尝试下一个路径
                        }
                    }
                    
//                    // 如果无法加载，创建默认棋子图形
//                    if (!loaded) {
//                        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(40, 40);
//                        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
//
//                        // 绘制一个简单的棋子形状
//                        gc.setFill(roleColors[i]);
//                        gc.fillOval(5, 5, 30, 30);
//                        gc.setStroke(Color.BLACK);
//                        gc.strokeOval(5, 5, 30, 30);
//                        gc.setFill(Color.WHITE);
//                        gc.fillText(roleName.substring(0, 1), 17, 25);
//
//                        javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
//                        params.setFill(Color.TRANSPARENT);
//                        playerPawnImages.put(role, canvas.snapshot(params, null));
//                        System.out.println("使用默认棋子图 " + roleName);
//                    }
                }
                
                System.out.println("角色棋子加载完成: " + playerPawnImages.size() + " 个角色");
                
            } catch (Exception e) {
                System.err.println("加载角色棋子图片时出错: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 输出加载结果统计
            System.out.println("地形图加载完成: " + normalTileImages.size() + " 个普通地形图, " + 
                              floodedTileImages.size() + " 个被淹地形图, " + 
                              sunkTileImages.size() + " 个沉没地形图");
            
        } catch (Exception e) {
            System.err.println("加载图片时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initialize() {
        gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        
        // 定义禁闭岛的标准布局
        int[][] islandLayout = {
            {0, 0, 1, 1, 0, 0},
            {0, 1, 1, 1, 1, 0},
            {1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1},
            {0, 1, 1, 1, 1, 0},
            {0, 0, 1, 1, 0, 0}
        };
        
        System.out.println("正在初始化禁闭岛游戏视图...");
        
        // 创建一个计数器，用于跟踪已放置的瓦片
        int tileCount = 0;
        
        // 遍历布局并创建瓦片视图
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                if (islandLayout[row][col] == 1) {
                    // 如果这个位置应该有瓦片
                    tileCount++;
                    String tileId = String.valueOf(tileCount);
                    
                    // 创建瓦片表示
                    Pane tilePane = createTileRepresentation(null, row, col);
                    
                    // 如果我们有图片，添加图片
                    if (normalTileImages.containsKey(tileId)) {
                        ImageView tileImageView = new ImageView(normalTileImages.get(tileId));
                        tileImageView.setFitWidth(TILE_SIZE);
                        tileImageView.setFitHeight(TILE_SIZE);
                        tileImageView.setPreserveRatio(true);
                        tilePane.getChildren().add(tileImageView);
                        
                        // 添加瓦片编号标签
                        Label nameLabel = new Label(tileId);
                        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                        nameLabel.setLayoutX(5);
                        nameLabel.setLayoutY(5);
                        tilePane.getChildren().add(nameLabel);
                    } else {
                        // 如果没有图片，添加一个占位符
                        Rectangle placeholder = new Rectangle(TILE_SIZE, TILE_SIZE);
                        placeholder.setFill(NORMAL_TILE_COLOR);
                        placeholder.setStroke(Color.BLACK);
                        tilePane.getChildren().add(placeholder);
                        
                        // 添加瓦片编号标签
                        Label nameLabel = new Label(tileId);
                        nameLabel.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
                        nameLabel.setLayoutX(TILE_SIZE/2 - 5);
                        nameLabel.setLayoutY(TILE_SIZE/2 - 5);
                        tilePane.getChildren().add(nameLabel);
                    }
                    
                    // 添加到网格
                    gridPane.add(tilePane, col, row);
                } else {
                    // 如果这个位置没有瓦片，添加一个空白的Pane
                    Pane emptyPane = new Pane();
                    emptyPane.setPrefSize(TILE_SIZE, TILE_SIZE);
                    gridPane.add(emptyPane, col, row);
                }
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
        
        System.out.println("IslandView初始化完成，禁闭岛标准布局");
        System.out.println("已加载瓦片图片数量: " + normalTileImages.size());
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
            
            // 确保所有必要的组件都存在，防止NullPointerException
            if (gameController != null && gameController.getIslandController() != null) {
                Island island = gameController.getIslandController().getIsland();
                if (island != null) {
                    Position position = new Position(row, col);
                    Tile clickedTile = island.getTile(position);
                    
                    if (clickedTile != null) {
                        // 有效的瓦片点击，传递给控制器处理
                        gameController.getIslandController().handleTileClick(clickedTile);
                    } else {
                        // 这个位置没有有效的瓦片，可能是空的格子或者地图外的区域
                        System.out.println("无效的瓦片位置: (" + row + ", " + col + ")");
                        // 可以考虑播放一个无效点击的提示音或显示提示
                    }
                } else {
                    System.err.println("岛屿对象为空");
                }
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
                TileState state = tile.getState();
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
                if (isShoredUp && state != TileState.SUNK) {
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
            
            int row = newPosition.getX();
            int col = newPosition.getY();
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
                Node node = getNodeByRowColumnIndex(pos.getX(), pos.getY(), gridPane);
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
                System.out.println("开始更新IslandView...");
                IslandController islandController = gameController.getIslandController();
                
                // Get island object
                Island island = islandController.getIsland();
                if (island == null) {
                    System.err.println("岛屿对象为空，无法更新视图");
                    return;
                }
                
                // 获取所有瓦片位置
                Map<Position, Tile> gameMap = island.getGameMap();
                if (gameMap.isEmpty()) {
                    System.err.println("游戏地图为空，无法更新视图");
                    return;
                }
                
                System.out.println("地图瓦片数量: " + gameMap.size());
                
                // 计算最大行和列
                int maxRow = 0;
                int maxCol = 0;
                for (Position pos : gameMap.keySet()) {
                    maxRow = Math.max(maxRow, pos.getX());
                    maxCol = Math.max(maxCol, pos.getY());
                }
                
                System.out.println("地图最大行列: " + maxRow + "x" + maxCol);
                
                // Update all tile states
                for (Map.Entry<Position, Tile> entry : gameMap.entrySet()) {
                    Position pos = entry.getKey();
                    Tile tile = entry.getValue();
                    if (tile != null) {
                        updateTileView(pos.getX(), pos.getY(), tile);
                    }
                }
                
                // Update player position markers
                PlayerController playerController = gameController.getPlayerController();
                if (playerController != null) {
                    List<Player> players = playerController.getRoom().getPlayers();
                    for (Player player : players) {
                        // Ensure player position is updated correctly
                        if (player.getPosition() != null) {
                            System.out.println("更新玩家位置: " + player.getName() + " 在 " + player.getPosition());
                            updatePlayerMarker(player, player.getPosition());
                        }
                    }
                }
                
                System.out.println("IslandView更新完成");
            } catch (Exception e) {
                System.err.println("更新岛屿视图时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("GameController为空，无法更新IslandView");
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
