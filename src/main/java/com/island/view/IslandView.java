package com.island.view;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;
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
    
    // 水位线相关图片
    private Image floodMeterImage;
    private Map<Integer, Image> waterLevelBackgrounds = new HashMap<>();
    private ImageView floodMeterView;
    private int currentWaterLevel = 1;
    
    // 卡牌相关图片
    private Image floodCardBackImage;
    private Image treasureCardSymbolImage;
    private List<Image> floodCardImages = new ArrayList<>();

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
            System.out.println("开始加载 IslandView 图片资源...");
            
            // 加载水位线图片
            try {
                floodMeterImage = new Image(getClass().getResourceAsStream("/islands/flood_meter.png"));
                if (floodMeterImage == null || floodMeterImage.isError()) {
                    System.err.println("无法加载水位线图片");
                }
                System.out.println("成功加载水位线图片");
            } catch (Exception e) {
                System.err.println("加载水位线图片失败: " + e.getMessage());
            }
            
            // 加载不同水位的背景图片
            for (int i = 2; i <= 5; i++) {
                try {
                    Image bgImage = new Image(getClass().getResourceAsStream("/islands/bg_" + i + ".png"));
                    if (bgImage != null && !bgImage.isError()) {
                        waterLevelBackgrounds.put(i, bgImage);
                        System.out.println("成功加载水位 " + i + " 的背景图片");
                    }
                } catch (Exception e) {
                    System.err.println("加载水位 " + i + " 背景图片失败: " + e.getMessage());
                }
            }
            
            // 默认使用水位2的背景图片
            if (waterLevelBackgrounds.containsKey(2)) {
                mapBackgroundImage = waterLevelBackgrounds.get(2);
            } else {
            // 如果背景加载失败，创建默认背景
                javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(600, 600);
                javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
                
                // 绘制渐变背景
                javafx.scene.paint.LinearGradient gradient = 
                    new javafx.scene.paint.LinearGradient(0, 0, 0, 600, false, 
                    javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new javafx.scene.paint.Stop(0, Color.LIGHTBLUE),
                    new javafx.scene.paint.Stop(1, Color.BLUE));
                
                gc.setFill(gradient);
                gc.fillRect(0, 0, 600, 600);
                
                // 将Canvas转换为Image
                javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                params.setFill(Color.TRANSPARENT);
                mapBackgroundImage = canvas.snapshot(params, null);
                
                System.out.println("使用默认地图背景");
            }
            
            // 加载岛屿瓦片图片
            String[] tileTypes = {
                "Earth1", "Earth2", "Fire1", "Fire2", "Wind1", "Wind2", "Ocean1", "Ocean2", 
                "Normal1", "Normal2", "Normal3", "Normal4", "Normal5", "Normal6", "Normal7", "Normal8", "Normal9", "Normal10",
                "Blue", "Red", "Green", "Yellow", "Black", "White"
                };
                
            // 加载正常和被淹没的瓦片图片
            for (String tileType : tileTypes) {
                    try {
                    // 加载正常瓦片图片
                    Image normalImage = new Image(getClass().getResourceAsStream("/islands/" + tileType + ".png"));
                    if (normalImage != null && !normalImage.isError()) {
                        normalTileImages.put(tileType, normalImage);
                        System.out.println("成功加载正常瓦片图片: " + tileType);
                    }
                    
                    // 加载被淹没的瓦片图片
                    Image floodedImage = new Image(getClass().getResourceAsStream("/islands/" + tileType + "_flood.png"));
                    if (floodedImage != null && !floodedImage.isError()) {
                        floodedTileImages.put(tileType, floodedImage);
                        System.out.println("成功加载被淹没瓦片图片: " + tileType);
                        }
                    } catch (Exception e) {
                    System.err.println("加载瓦片图片失败 " + tileType + ": " + e.getMessage());
                    }
                }
                
            // 加载洪水卡背面图片
            try {
                floodCardBackImage = new Image(getClass().getResourceAsStream("/Flood/Flood Deck.png"));
                if (floodCardBackImage == null || floodCardBackImage.isError()) {
                    System.err.println("无法加载洪水卡背面图片");
                    // 创建默认洪水卡背面
                    javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(80, 120);
                    javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
                    gc.setFill(Color.LIGHTBLUE);
                    gc.fillRoundRect(0, 0, 80, 120, 10, 10);
                    gc.setStroke(Color.BLACK);
                    gc.setLineWidth(2);
                    gc.strokeRoundRect(0, 0, 80, 120, 10, 10);
                    gc.setFill(Color.BLACK);
                    gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    gc.fillText("洪水", 25, 50);
                    gc.fillText("卡牌", 25, 70);
                    
                    javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                    params.setFill(Color.TRANSPARENT);
                    floodCardBackImage = canvas.snapshot(params, null);
                }
                System.out.println("成功加载洪水卡背面图片");
            } catch (Exception e) {
                System.err.println("加载洪水卡背面图片失败: " + e.getMessage());
            }
            
            // 加载宝藏卡符号图片
            try {
                // 尝试从多个可能的路径加载宝藏卡符号图片
                String[] treasureSymbolPaths = {
                    "/TreasureCards/TreasureCardSymbol.png",
                    "/treasurecards/TreasureCardSymbol.png",
                    "/treasure/TreasureCardSymbol.png",
                    "/treasurecards/treasurecardsymbol.png",
                    "/TreasureCards/treasurecardsymbol.png",
                    "/images/TreasureCards/TreasureCardSymbol.png"
                };
                
                boolean loaded = false;
                for (String path : treasureSymbolPaths) {
                    try {
                        Image img = new Image(getClass().getResourceAsStream(path));
                        if (img != null && !img.isError()) {
                            treasureCardSymbolImage = img;
                            System.out.println("成功加载宝藏卡符号图片: " + path);
                            loaded = true;
                            break;
                        }
                    } catch (Exception e) {
                        // 继续尝试下一个路径
                    }
                }
                
                // 如果所有路径都加载失败，创建默认图片
                if (!loaded) {
                    System.err.println("无法加载宝藏卡符号图片，创建默认图片");
                    // 创建默认宝藏卡符号
                    javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(80, 120);
                    javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
                    gc.setFill(Color.GOLD);
                    gc.fillRoundRect(0, 0, 80, 120, 10, 10);
                        gc.setStroke(Color.BLACK);
                    gc.setLineWidth(2);
                    gc.strokeRoundRect(0, 0, 80, 120, 10, 10);
                        gc.setFill(Color.BLACK);
                    gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    gc.fillText("宝藏", 25, 50);
                    gc.fillText("卡牌", 25, 70);

                    javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                    params.setFill(Color.TRANSPARENT);
                    treasureCardSymbolImage = canvas.snapshot(params, null);
                }
            } catch (Exception e) {
                System.err.println("加载宝藏卡符号图片失败: " + e.getMessage());
            }
            
            // 加载洪水卡图片
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
                            floodCardImages.add(img);
                            System.out.println("成功加载洪水卡图片: " + path);
                            loaded = true;
                            break;
                        }
                    } catch (Exception e) {
                        // 继续尝试下一个路径
                    }
                }
                
                // 如果没有加载成功，创建默认图片
                if (!loaded) {
                    // 创建默认洪水卡图片
                    javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(80, 120);
                    javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
                    gc.setFill(Color.LIGHTBLUE);
                    gc.fillRoundRect(0, 0, 80, 120, 10, 10);
                    gc.setStroke(Color.BLACK);
                    gc.setLineWidth(2);
                    gc.strokeRoundRect(0, 0, 80, 120, 10, 10);
                    gc.setFill(Color.BLACK);
                    gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    gc.fillText("洪水卡 #" + i, 10, 60);

                    javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                    params.setFill(Color.TRANSPARENT);
                    floodCardImages.add(canvas.snapshot(params, null));
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
                
                for (PlayerRole role : roles) {
                    String roleName = role.toString().toLowerCase();
                    roleName = roleName.substring(0, 1).toUpperCase() + roleName.substring(1);
                    
                        try {
                        Image image = new Image(getClass().getResourceAsStream("/adventurers/" + roleName + ".png"));
                            if (image != null && !image.isError()) {
                                playerPawnImages.put(role, image);
                            System.out.println("成功加载角色棋子图片: " + roleName);
                        }
                    } catch (Exception e) {
                        System.err.println("加载角色棋子图片失败 " + roleName + ": " + e.getMessage());
                    }
                }
                
                System.out.println("角色棋子加载完成: " + playerPawnImages.size() + " 个角色");
                
            } catch (Exception e) {
                System.err.println("加载角色棋子图片时出错: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 输出加载结果统计
            System.out.println("地形图加载完成: " + normalTileImages.size() + " 个普通地形图, " + 
                              floodedTileImages.size() + " 个被淹地形图");
            
        } catch (Exception e) {
            System.err.println("加载图片时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initialize() {
        // 创建主视图容器
        viewPane = new Pane();
        viewPane.setPrefSize(800, 600);
        
        // 添加背景图片
        if (mapBackgroundImage != null) {
            ImageView backgroundImageView = new ImageView(mapBackgroundImage);
            backgroundImageView.setFitWidth(800);
            backgroundImageView.setFitHeight(600);
            backgroundImageView.setPreserveRatio(false);
            viewPane.getChildren().add(backgroundImageView);
        }
        
        // 创建网格布局
        gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        
        // 定义菱形布局 (第一行2个，第二行4个，第三/四行6个，第五行4个，第六行2个)
        int[][] islandLayout = {
            {0, 0, 1, 1, 0, 0},
            {0, 1, 1, 1, 1, 0},
            {1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1},
            {0, 1, 1, 1, 1, 0},
            {0, 0, 1, 1, 0, 0}
        };
        
        System.out.println("正在初始化禁闭岛游戏视图...");
        
        // 按照指定顺序定义瓦片类型
        String[] tileTypeMapping = {
            "Fire1", "Green", "Red", "Normal8", "Blue", "Wind1", 
            "Earth2", "Normal1", "Normal10", "Normal2", "White", "Yellow", 
            "Normal3", "Normal5", "Normal9", "Earth1", "Ocean2", "Fire2", 
            "Black", "Ocean1", "Normal4", "Normal6", "Wind2", "Normal7"
        };
        
        // 创建一个计数器，用于跟踪已放置的瓦片
        int tileCount = 0;
        
        // 遍历布局并创建瓦片视图
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                if (islandLayout[row][col] == 1) {
                    // 如果这个位置应该有瓦片
                    
                    // 使用当前计数器作为索引，不使用模运算确保按顺序显示
                    int tileTypeIndex = tileCount;
                    String tileType = "Normal1"; // 默认类型
                    if (tileTypeIndex < tileTypeMapping.length) {
                        tileType = tileTypeMapping[tileTypeIndex];
                    }
                    
                    // 创建瓦片表示
                    Pane tilePane = createTileRepresentation(null, row, col);
                    
                    // 如果我们有图片，添加图片
                    if (normalTileImages.containsKey(tileType)) {
                        ImageView tileImageView = new ImageView(normalTileImages.get(tileType));
                        tileImageView.setFitWidth(TILE_SIZE);
                        tileImageView.setFitHeight(TILE_SIZE);
                        tileImageView.setPreserveRatio(true);
                        tilePane.getChildren().add(tileImageView);
                        
                        // 添加瓦片编号标签
                        Label nameLabel = new Label(String.valueOf(tileCount));
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
                        Label nameLabel = new Label(String.valueOf(tileCount));
                        nameLabel.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
                        nameLabel.setLayoutX(TILE_SIZE/2 - 5);
                        nameLabel.setLayoutY(TILE_SIZE/2 - 5);
                        tilePane.getChildren().add(nameLabel);
                    }
                    
                    // 添加到网格
                    gridPane.add(tilePane, col, row);
                    
                    // 增加计数器
                    tileCount++;
                } else {
                    // 如果这个位置没有瓦片，添加一个空白的Pane
                    Pane emptyPane = new Pane();
                    emptyPane.setPrefSize(TILE_SIZE, TILE_SIZE);
                    gridPane.add(emptyPane, col, row);
                }
            }
        }

        // 添加网格到主视图
        viewPane.getChildren().add(gridPane);
        
        // 调整网格位置，使其位于地图中央
        gridPane.setLayoutX((800 - 6 * TILE_SIZE - 5 * 5) / 2);  // 居中显示
        gridPane.setLayoutY((600 - 6 * TILE_SIZE - 5 * 5) / 2);
        
        // 添加水位线图片
        if (floodMeterImage != null) {
            floodMeterView = new ImageView(floodMeterImage);
            floodMeterView.setFitWidth(80);  // 减小宽度以节省空间
            floodMeterView.setFitHeight(350); // 减小高度
            floodMeterView.setPreserveRatio(true);
            floodMeterView.setLayoutX(680);  // 微调位置，与卡片保持适当距离
            floodMeterView.setLayoutY(100);  // 垂直居中
            viewPane.getChildren().add(floodMeterView);
        
            // 添加水位线指示器
            updateWaterLevelIndicator(1);  // 初始水位为1
        }
        
        // 添加洪水卡堆
        addFloodCardStack();
        
        // 添加宝藏卡符号
        addTreasureCardSymbol();
        
        System.out.println("IslandView初始化完成，禁闭岛菱形布局");
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
                
                // 获取瓦片名称和状态
                String tileName = tile.getName();
                TileState state = tile.getState();
                boolean isShoredUp = tile.isShoredUp();
                
                // 将瓦片名称映射到图片类型
                String tileType = mapTileNameToImageType(tileName);
                
                ImageView tileImageView = new ImageView();
                tileImageView.setFitWidth(TILE_SIZE);
                tileImageView.setFitHeight(TILE_SIZE);
                tileImageView.setPreserveRatio(true);

                // 根据瓦片状态选择图片
                switch (state) {
                    case NORMAL:
                        if (normalTileImages.containsKey(tileType)) {
                            tileImageView.setImage(normalTileImages.get(tileType));
                        } else {
                            // 如果找不到图片，使用默认矩形
                            Rectangle rect = new Rectangle(TILE_SIZE, TILE_SIZE);
                            rect.setFill(NORMAL_TILE_COLOR);
                            tilePane.getChildren().add(rect);
                        }
                        break;
                    case FLOODED:
                        if (floodedTileImages.containsKey(tileType)) {
                            tileImageView.setImage(floodedTileImages.get(tileType));
                        } else {
                            Rectangle rect = new Rectangle(TILE_SIZE, TILE_SIZE);
                            rect.setFill(FLOODED_TILE_COLOR);
                            tilePane.getChildren().add(rect);
                        }
                        break;
                    case SUNK:
                        // 对于沉没的瓦片，我们显示被淹没的图片，但更暗一些
                        if (floodedTileImages.containsKey(tileType)) {
                            tileImageView.setImage(floodedTileImages.get(tileType));
                            tileImageView.setOpacity(0.5); // 降低不透明度表示沉没
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
                Label nameLabel = new Label(tileName);
                nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
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
                
                // 更新水位线
                int waterLevel = island.getWaterLevel();
                if (waterLevel != currentWaterLevel) {
                    updateWaterLevelIndicator(waterLevel);
                }
                
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
    
    /**
     * 将瓦片名称映射到图片类型
     * @param tileName 瓦片名称
     * @return 对应的图片类型
     */
    private String mapTileNameToImageType(String tileName) {
        // 根据瓦片名称返回对应的图片类型
        if (tileName.contains("Earth") || tileName.equals("Howling Garden") || tileName.equals("Whispering Garden")) {
            return "Earth1";
        } else if (tileName.contains("Fire") || tileName.equals("Cave of Embers") || tileName.equals("Cave of Shadows")) {
            return "Fire1";
        } else if (tileName.contains("Wind") || tileName.equals("Breakers Bridge") || tileName.equals("Bronze Gate")) {
            return "Wind1";
        } else if (tileName.contains("Ocean") || tileName.equals("Cliffs of Abandon") || tileName.equals("Coral Palace")) {
            return "Ocean1";
        } else if (tileName.equals("Fool's Landing")) {
            return "Blue";
        } else if (tileName.equals("Temple of the Moon") || tileName.equals("Temple of the Sun")) {
            return "Yellow";
        } else if (tileName.equals("Silver Gate") || tileName.equals("Gold Gate")) {
            return "White";
        } else if (tileName.equals("Iron Gate") || tileName.equals("Bronze Gate") || tileName.equals("Copper Gate")) {
            return "Green";
        } else if (tileName.equals("Crimson Forest") || tileName.equals("Dunes of Deception")) {
            return "Red";
        } else if (tileName.equals("Lost Lagoon") || tileName.equals("Misty Marsh")) {
            return "Black";
        } else {
            // 默认使用普通瓦片
            return "Normal1";
        }
    }

    /**
     * 更新水位线指示器和背景
     * @param waterLevel 当前水位 (1-10)
     */
    public void updateWaterLevelIndicator(int waterLevel) {
        int level = waterLevel;
        if (level < 1) level = 1;
        if (level > 10) level = 10;
        
        currentWaterLevel = level;
        
        // 更新背景图片
        final int finalLevel = level;  // 创建一个 final 变量给 lambda 表达式使用
        Platform.runLater(() -> {
            // 根据水位选择背景图片
            Image newBackground = null;
            if (finalLevel >= 5) {
                newBackground = waterLevelBackgrounds.get(5);
            } else if (finalLevel >= 4) {
                newBackground = waterLevelBackgrounds.get(4);
            } else if (finalLevel >= 3) {
                newBackground = waterLevelBackgrounds.get(3);
            } else if (finalLevel >= 2) {
                newBackground = waterLevelBackgrounds.get(2);
            }
            
            if (newBackground != null) {
                // 更新背景图片
                for (Node node : viewPane.getChildren()) {
                    if (node instanceof ImageView && node != floodMeterView) {
                        ((ImageView) node).setImage(newBackground);
                        break;
                    }
                }
            }
            
            // 更新水位线指示器
            if (floodMeterView != null) {
                // 这里可以添加水位线指示器的动画或其他视觉效果
                // 例如添加一个标记或颜色变化
                System.out.println("更新水位线指示器: " + finalLevel);
            }
        });
    }
    
    /**
     * 添加洪水卡堆到视图中
     */
    private void addFloodCardStack() {
        // 洪水卡堆的位置 - 适当调整位置
        double cardWidth = 70;  // 减小宽度以节省空间
        double cardHeight = 105; // 减小高度以节省空间
        double stackX = 800;  // 向左移动一点点
        double stackY = 50;   // 顶部位置
        
        // 创建一个StackPane来叠放卡片
        StackPane floodCardStack = new StackPane();
        floodCardStack.setLayoutX(stackX);
        floodCardStack.setLayoutY(stackY);
        
        // 指定的卡片类型顺序
        String[] specificCardTypes = {
            "Fire1", "Green", "Red", "Normal8", "Blue", "Wind1", 
            "Earth2", "Normal1", "Normal10", "Normal2", "White", "Yellow", 
            "Normal3", "Normal5", "Normal9", "Earth1", "Ocean2", "Fire2", 
            "Black", "Ocean1", "Normal4", "Normal6", "Wind2", "Normal7"
        };
        
        // 随机选择8张卡片进行展示
        List<String> selectedTypes = new ArrayList<>();
        Random random = new Random();
        
        // 确保我们有足够的卡片类型可供选择
        if (specificCardTypes.length >= 8) {
            // 创建一个卡片类型的列表副本，以便我们可以从中随机选择
            List<String> availableTypes = new ArrayList<>(Arrays.asList(specificCardTypes));
            
            // 随机选择8张不重复的卡片
            for (int i = 0; i < 8 && !availableTypes.isEmpty(); i++) {
                int index = random.nextInt(availableTypes.size());
                selectedTypes.add(availableTypes.remove(index));
            }
        } else {
            // 如果没有足够的卡片类型，则使用所有可用的类型
            selectedTypes.addAll(Arrays.asList(specificCardTypes));
        }
        
        // 添加选中的卡片到堆中，稍微错开以显示叠放效果
        for (int i = 0; i < selectedTypes.size(); i++) {
            String cardType = selectedTypes.get(i);
            ImageView cardView;
            
            // 尝试获取对应类型的图片
            if (normalTileImages.containsKey(cardType)) {
                cardView = new ImageView(normalTileImages.get(cardType));
            } else {
                // 如果找不到图片，使用默认背面
                cardView = new ImageView(floodCardBackImage);
            }
            
            cardView.setFitWidth(cardWidth);
            cardView.setFitHeight(cardHeight);
            cardView.setPreserveRatio(true);
            
            // 错开卡片位置
            cardView.setTranslateX(i * 3);
            cardView.setTranslateY(i * 3);
            
            floodCardStack.getChildren().add(cardView);
        }
        
        // 添加标签
        Label floodDeckLabel = new Label("洪水卡");
        floodDeckLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: rgba(0,0,0,0.7); -fx-padding: 3px; -fx-background-radius: 3;");
        floodDeckLabel.setLayoutX(stackX);
        floodDeckLabel.setLayoutY(stackY + cardHeight + 5);
        
        // 添加到视图
        viewPane.getChildren().addAll(floodCardStack, floodDeckLabel);
    }
    
    /**
     * 添加宝藏卡符号到视图中
     */
    private void addTreasureCardSymbol() {
        // 宝藏卡符号的位置 - 适当调整位置
        double cardWidth = 70;  // 减小宽度以节省空间
        double cardHeight = 105; // 减小高度以节省空间
        double symbolX = 800;  // 向左移动一点点
        double symbolY = 250;  // 中间位置
        
        // 创建宝藏卡符号视图
        ImageView treasureSymbolView = new ImageView(treasureCardSymbolImage);
        treasureSymbolView.setFitWidth(cardWidth);
        treasureSymbolView.setFitHeight(cardHeight);
        treasureSymbolView.setPreserveRatio(true);
        treasureSymbolView.setLayoutX(symbolX);
        treasureSymbolView.setLayoutY(symbolY);
        
        // 添加标签
        Label treasureLabel = new Label("宝藏卡");
        treasureLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: rgba(0,0,0,0.7); -fx-padding: 3px; -fx-background-radius: 3;");
        treasureLabel.setLayoutX(symbolX);
        treasureLabel.setLayoutY(symbolY + cardHeight + 5);
        
        // 添加到视图
        viewPane.getChildren().addAll(treasureSymbolView, treasureLabel);
    }
}

