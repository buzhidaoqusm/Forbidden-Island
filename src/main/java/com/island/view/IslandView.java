package com.island.view;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
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
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

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
                "Fire1", "Green", "Red", "Normal8", "Blue", "Wind1", "Earth2", "Normal1", 
                "Normal10", "Normal2", "White", "Yellow", "Normal3", "Normal5", "Normal9", "Earth1", 
                "Ocean2", "Fire2", "Black", "Ocean1", "Normal4", "Normal6", "Wind2", "Normal7"
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
                floodCardBackImage = new Image(getClass().getResourceAsStream("/flood cards/FloodCardSymbol.png"));
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
                    "/treasure cards/TreasureCardSymbol.png"
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
            String[] floodCardTypes = {
                "Black", "Blue", "Earth1", "Earth2", "Fire1", "Fire2", "Green",
                "Normal1", "Normal2", "Normal3", "Normal4", "Normal5", "Normal6",
                "Normal7", "Normal8", "Normal9", "Normal10", "Ocean1", "Ocean2",
                "Red", "White", "Wind1", "Wind2", "Yellow"
            };
            
            // 只尝试从 flood cards 目录加载
            String floodCardPath = "/flood cards/";
            
            for (int i = 0; i < floodCardTypes.length; i++) {
                boolean loaded = false;
                String path = floodCardPath + floodCardTypes[i] + ".png";
                try {
                    Image img = new Image(getClass().getResourceAsStream(path));
                    if (img != null && !img.isError()) {
                        floodCardImages.add(img);
                        System.out.println("成功加载洪水卡图片: " + path);
                        loaded = true;
                    }
                } catch (Exception e) {
                    System.err.println("加载洪水卡图片失败: " + path + " - " + e.getMessage());
                }
                
                // 如果没有找到图片，创建默认图片
                if (!loaded) {
                    System.out.println("无法加载洪水卡图片 " + floodCardTypes[i] + "，创建默认图片");
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
                    gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    gc.fillText(floodCardTypes[i], 15, 80);
                    
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
        // 创建主视图容器 - 使用ScrollPane来支持滚动
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefSize(800, 600); // 3:4比例的横屏
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // 禁用水平滚动条
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // 根据需要显示垂直滚动条
        
        // 创建可滚动的内容区域，比例为1:2的竖屏
        viewPane = new Pane();
        viewPane.setPrefSize(800, 1600); // 1:2比例的竖屏
        
        // 添加背景图片
        if (mapBackgroundImage != null) {
            ImageView backgroundImageView = new ImageView(mapBackgroundImage);
            backgroundImageView.setFitWidth(800);
            backgroundImageView.setFitHeight(1600);
            backgroundImageView.setPreserveRatio(false);
            viewPane.getChildren().add(backgroundImageView);
        }
        
        // 创建行动选项区域（放在最上方）
        Pane actionPane = new Pane();
        actionPane.setPrefSize(800, 80);
        actionPane.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-padding: 10px;");
        actionPane.setLayoutY(10);
        
        // 添加行动选项标签
        Label actionLabel = new Label("行动选项");
        actionLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
        actionLabel.setLayoutX(10);
        actionLabel.setLayoutY(10);
        actionPane.getChildren().add(actionLabel);
        
        // 创建网格布局（放在行动选项下方）
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
        
        // 创建一个列表来存储所有有效的位置
        List<int[]> validPositions = new ArrayList<>();
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                if (islandLayout[row][col] == 1) {
                    validPositions.add(new int[]{row, col});
                }
            }
        }
        
        // Check if the IslandController has an initialized Island
        Island island = null;
        if (gameController != null && gameController.getIslandController() != null) {
            island = gameController.getIslandController().getIsland();
        }
        
        // Create tiles for all valid positions
        for (int[] pos : validPositions) {
            int row = pos[0];
            int col = pos[1];
            
            // Create a tile representation
            Tile modelTile = null;
            if (island != null) {
                modelTile = island.getTile(new Position(row, col));
            }
            
            Pane tilePane = createTileRepresentation(modelTile, row, col);
            
            // If we don't have a model tile yet, create a visual placeholder
            if (modelTile == null) {
                // Create a visual placeholder for the tile
                        Rectangle placeholder = new Rectangle(TILE_SIZE, TILE_SIZE);
                        placeholder.setFill(NORMAL_TILE_COLOR);
                        placeholder.setStroke(Color.BLACK);
                tilePane.getChildren().add(placeholder);
                        
                // Add a label for debugging
                Label posLabel = new Label("(" + row + "," + col + ")");
                posLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
                posLabel.setLayoutX(5);
                posLabel.setLayoutY(5);
                tilePane.getChildren().add(posLabel);
                    }
            
            // Add to grid
            gridPane.add(tilePane, col, row);
        }
        
        // Add empty spaces for invalid positions
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                if (islandLayout[row][col] == 0) {
                    // If this position has no tile, add an empty Pane
                    Pane emptyPane = new Pane();
                    emptyPane.setPrefSize(TILE_SIZE, TILE_SIZE);
                    gridPane.add(emptyPane, col, row);
                }
            }
        }

        // 调整网格位置，使其居中且位于行动选项下方
        gridPane.setLayoutX((800 - 6 * TILE_SIZE - 5 * 5) / 2);
        gridPane.setLayoutY(100); // 放在行动选项下方
        
        // 创建卡牌和游戏信息区域（放在岛屿下方）
        Pane cardInfoPane = new Pane();
        cardInfoPane.setPrefSize(800, 900);
        cardInfoPane.setLayoutY(600); // 放在岛屿下方
        
        // 添加水位线指示器
        if (floodMeterImage != null) {
            floodMeterView = new ImageView(floodMeterImage);
            floodMeterView.setFitWidth(80);
            floodMeterView.setFitHeight(350);
            floodMeterView.setPreserveRatio(true);
            floodMeterView.setLayoutX(680);
            floodMeterView.setLayoutY(650); // 调整Y坐标到岛屿下方
            viewPane.getChildren().add(floodMeterView);
        
            // 添加水位线标签
            Label waterLevelLabel = new Label("水位线");
            waterLevelLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: rgba(0,0,0,0.7); -fx-padding: 3px; -fx-background-radius: 3;");
            waterLevelLabel.setLayoutX(680);
            waterLevelLabel.setLayoutY(1010); // 放在水位线指示器下方
            viewPane.getChildren().add(waterLevelLabel);
            
            // 初始化水位线
            updateWaterLevelIndicator(1);
        }
        
        // 添加到主视图
        viewPane.getChildren().addAll(actionPane, gridPane, cardInfoPane);
        
        // 添加洪水卡和宝藏卡
        addFloodCardStack();
        addTreasureCardSymbol();
        
        // 添加游戏日志区域
        addGameLogArea();
        
        // 设置滚动面板的内容
        scrollPane.setContent(viewPane);
        
        // 保存对滚动面板的引用，以便在需要时更新视图
        this.scrollPane = scrollPane;
        
        System.out.println("IslandView initialization complete");
    }

    private Pane createTileRepresentation(Tile tile, int row, int col) {
        Pane tilePane = new Pane();
        tilePane.setPrefSize(TILE_SIZE, TILE_SIZE);
        
        // Store the position as user data for easy access in click handler
        tilePane.setUserData(new Position(row, col));
        
        // Default use a rectangle as placeholder
        Rectangle background = new Rectangle(TILE_SIZE, TILE_SIZE);
        background.setFill(Color.TRANSPARENT);
        background.setStroke(Color.BLACK);
        
        // Add to pane
        tilePane.getChildren().add(background);

        // Add click listener
        tilePane.setOnMouseClicked(event -> {
            System.out.println("Clicked on tile at (" + row + ", " + col + ")");
            
            // Make sure all necessary components exist to prevent NullPointerException
            if (gameController != null && gameController.getIslandController() != null) {
                Island island = gameController.getIslandController().getIsland();
                if (island != null) {
                    Position position = new Position(row, col);
                    Tile clickedTile = island.getTile(position);
                    
                    if (clickedTile != null) {
                        // Valid tile click, pass to controller for handling
                        gameController.getIslandController().handleTileClick(clickedTile);
                    } else {
                        // This position has no valid tile, might be empty space or off-map area
                        System.out.println("Invalid tile position: (" + row + ", " + col + ")");
                    }
                } else {
                    System.err.println("Island object is null");
                }
            } else {
                System.err.println("GameController or IslandController is null");
            }
        });

        return tilePane;
    }

    public void updateTileView(int row, int col, Tile tile) {
        // Find the corresponding Pane in the gridPane
        Platform.runLater(() -> {
            // Example: Get node by row/col (might need adjustment based on gridPane structure)
            Node node = getNodeByRowColumnIndex(row, col, gridPane);
            if (node instanceof Pane) {
                Pane tilePane = (Pane) node;
                
                // Store the original click handler before clearing
                @SuppressWarnings("unchecked")
                javafx.event.EventHandler<javafx.scene.input.MouseEvent> clickHandler = 
                    (javafx.event.EventHandler<javafx.scene.input.MouseEvent>) tilePane.getOnMouseClicked();
                
                // Clear existing content
                tilePane.getChildren().clear();
                
                // Restore the click handler
                tilePane.setOnMouseClicked(clickHandler);
                
                // Get tile name and state
                String tileName = tile.getName();
                TileState state = tile.getState();
                boolean isShoredUp = tile.isShoredUp();
                
                // Map tile name to image type
                String tileType = mapTileNameToImageType(tileName);
                
                ImageView tileImageView = new ImageView();
                tileImageView.setFitWidth(TILE_SIZE);
                tileImageView.setFitHeight(TILE_SIZE);
                tileImageView.setPreserveRatio(true);

                // Choose image path based on tile state
                boolean imageLoaded = false;
                
                try {
                    Image tileImage = null;
                    switch (state) {
                        case NORMAL:
                            tileImage = normalTileImages.getOrDefault(tileType, null);
                            break;
                        case FLOODED:
                            tileImage = floodedTileImages.getOrDefault(tileType, null);
                            break;
                        case SUNK:
                            // For sunken tiles, show flooded image with reduced opacity
                            tileImage = floodedTileImages.getOrDefault(tileType, null);
                            tileImageView.setOpacity(0.5); // Reduce opacity to indicate sunken
                            break;
                    }
                    
                    if (tileImage != null && !tileImage.isError()) {
                        tileImageView.setImage(tileImage);
                        tilePane.getChildren().add(tileImageView);
                        imageLoaded = true;
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load tile image for " + tileType + ": " + e.getMessage());
                }
                
                // If image loading failed, use default rectangle
                if (!imageLoaded) {
                            Rectangle rect = new Rectangle(TILE_SIZE, TILE_SIZE);
                    switch (state) {
                        case NORMAL:
                            rect.setFill(NORMAL_TILE_COLOR);
                        break;
                    case FLOODED:
                            rect.setFill(FLOODED_TILE_COLOR);
                        break;
                    case SUNK:
                            rect.setFill(SUNK_TILE_COLOR);
                        break;
                }
                    tilePane.getChildren().add(rect);
                }
                
                // If tile is shored up and not sunk, add border
                if (isShoredUp && state != TileState.SUNK) {
                    Rectangle shoreUpIndicator = new Rectangle(TILE_SIZE, TILE_SIZE);
                    shoreUpIndicator.setFill(Color.TRANSPARENT);
                    shoreUpIndicator.setStroke(SHORED_UP_BORDER_COLOR);
                    shoreUpIndicator.setStrokeWidth(SHORED_UP_BORDER_WIDTH);
                    tilePane.getChildren().add(shoreUpIndicator);
                }
                
                // Display tile name
                Label nameLabel = new Label(tileName);
                // 根据瓦片名称长度设置不同的字体大小
                if (tileName.length() > 10) {
                    nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 8px;");
                } else {
                    nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
                }
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

    public ScrollPane getView() {
        return scrollPane;
    }
    
    /**
     * Update the island view, get the latest island status information from GameController
     */
    public void update() {
        if (gameController != null) {
            try {
                System.out.println("Starting IslandView update...");
                IslandController islandController = gameController.getIslandController();
                
                // Get island object
                Island island = islandController.getIsland();
                if (island == null) {
                    System.err.println("Island object is null, cannot update view");
                    return;
                }
                
                // Get all tile positions
                Map<Position, Tile> gameMap = island.getGameMap();
                if (gameMap.isEmpty()) {
                    System.err.println("Game map is empty, cannot update view");
                    return;
                }
                
                System.out.println("Map tile count: " + gameMap.size());
                
                // Update water level
                int waterLevel = island.getWaterLevel();
                if (waterLevel != currentWaterLevel) {
                    updateWaterLevelIndicator(waterLevel);
                }
                
                // Update all tile states
                for (Map.Entry<Position, Tile> entry : gameMap.entrySet()) {
                    Position pos = entry.getKey();
                    Tile tile = entry.getValue();
                        updateTileView(pos.getX(), pos.getY(), tile);
                }
                
                // Update player positions
                PlayerController playerController = gameController.getPlayerController();
                if (playerController != null) {
                    List<Player> players = playerController.getRoom().getPlayers();
                    if (players != null) {
                    for (Player player : players) {
                        // Ensure player position is updated correctly
                        if (player.getPosition() != null) {
                                System.out.println("Updating player position: " + player.getName() + " at " + player.getPosition());
                            updatePlayerMarker(player, player.getPosition());
                        }
                        }
                    }
                }
                
                // Highlight valid positions if any
                Player currentPlayer = gameController.getCurrentPlayer();
                if (currentPlayer != null) {
                    List<Position> validPositions = islandController.getValidPositions(currentPlayer);
                    if (validPositions != null && !validPositions.isEmpty()) {
                        highlightTiles(validPositions, "valid_move");
                    }
                }
                
                System.out.println("IslandView update complete");
            } catch (Exception e) {
                System.err.println("Error updating island view: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("GameController is null, cannot update IslandView");
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
        // 创建一个映射表来存储瓦片名称到图片类型的对应关系
        Map<String, String> tileMap = new HashMap<>();
        tileMap.put("FOOL'S LANDING", "Blue");
        tileMap.put("BRONZE GATE", "Wind1");
        tileMap.put("CAVE OF EMBERS", "Fire1");
        tileMap.put("CAVE OF SHADOWS", "Fire2");
        tileMap.put("COPPER GATE", "Green");
        tileMap.put("CORAL PALACE", "Ocean1");
        tileMap.put("CRIMSON FOREST", "Red");
        tileMap.put("DUNES OF DECEPTION", "Normal1");
        tileMap.put("GOLD GATE", "Yellow");
        tileMap.put("IRON GATE", "Black");
        tileMap.put("LOST LAGOON", "Ocean2");
        tileMap.put("MISTY MARSH", "Normal2");
        tileMap.put("OBSERVATORY", "Normal3");
        tileMap.put("PHANTOM ROCK", "Normal4");
        tileMap.put("SILVER GATE", "White");
        tileMap.put("TEMPLE OF THE MOON", "Earth1");
        tileMap.put("TEMPLE OF THE SUN", "Earth2");
        tileMap.put("TIDAL PALACE", "Normal5");
        tileMap.put("TWILIGHT HOLLOW", "Normal6");
        tileMap.put("WATCHTOWER", "Normal7");
        tileMap.put("WHISPERING GARDEN", "Wind2");
        tileMap.put("HOWLING GARDEN", "Normal8");
        tileMap.put("BREAKERS BRIDGE", "Normal9");
        tileMap.put("CLIFFS OF ABANDON", "Normal10");

        // 如果找到映射关系，返回对应的图片类型
        if (tileMap.containsKey(tileName)) {
            System.out.println("映射瓦片 " + tileName + " 到图片类型: " + tileMap.get(tileName));
            return tileMap.get(tileName);
        }

        // 如果没有找到映射关系，输出警告并返回默认类型
        System.err.println("警告：未找到瓦片 " + tileName + " 的图片类型映射，使用默认类型 Normal1");
        return "Normal1";
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
        // 洪水卡堆的位置 - 调整到岛屿下方
        double cardWidth = 80;  // 卡片宽度
        double cardHeight = 120; // 卡片高度
        double stackX = 650;  // 洪水卡堆的X坐标
        double stackY = 650;   // 洪水卡堆的Y坐标 - 调整到岛屿下方
        double discardX = 100; // 弃牌堆的X坐标
        double discardY = 650;  // 弃牌堆的Y坐标 - 调整到岛屿下方
        double offsetX = 30;   // 卡片横向偏移量
        
        // 创建洪水卡背面堆
        ImageView floodCardBack = new ImageView(floodCardBackImage);
        floodCardBack.setFitWidth(cardWidth);
        floodCardBack.setFitHeight(cardHeight);
        floodCardBack.setPreserveRatio(true);
        floodCardBack.setLayoutX(stackX);
        floodCardBack.setLayoutY(stackY);
        
        // 添加洪水卡背面标签
        Label floodDeckLabel = new Label("洪水卡牌堆");
        floodDeckLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: rgba(0,0,0,0.7); -fx-padding: 3px; -fx-background-radius: 3;");
        floodDeckLabel.setLayoutX(stackX);
        floodDeckLabel.setLayoutY(stackY + cardHeight + 5);
        
        // 创建已沉没的洪水卡（弃牌堆）
        Pane discardPane = new Pane();
        discardPane.setLayoutX(discardX);
        discardPane.setLayoutY(discardY);
        
        // 添加6张沉没的洪水卡，从左至右叠放
        for (int i = 0; i < 6; i++) {
            // 使用瓦片图片作为沉没的洪水卡
            String tileType = "Normal" + ((i % 6) + 1); // 使用不同的普通瓦片图片
            String imagePath = "/islands/" + tileType + ".png";
            ImageView cardView;
            
            try {
                Image cardImage = new Image(getClass().getResourceAsStream(imagePath));
                cardView = new ImageView(cardImage);
            } catch (Exception e) {
                // 如果加载失败，使用默认背面
                cardView = new ImageView(floodCardBackImage);
            }
            
            cardView.setFitWidth(cardWidth);
            cardView.setFitHeight(cardHeight);
            cardView.setPreserveRatio(true);
            cardView.setLayoutX(i * offsetX);
            cardView.setOpacity(0.7); // 降低透明度表示已沉没
            
            // 添加蓝色半透明覆盖层表示已沉没
            Rectangle overlay = new Rectangle(cardWidth, cardHeight);
            overlay.setFill(Color.BLUE.deriveColor(0, 1, 1, 0.3));
            overlay.setLayoutX(i * offsetX);
            
            discardPane.getChildren().addAll(cardView, overlay);
        }
        
        // 添加弃牌堆标签
        Label discardLabel = new Label("已沉没的洪水卡");
        discardLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: rgba(0,0,0,0.7); -fx-padding: 3px; -fx-background-radius: 3;");
        discardLabel.setLayoutX(discardX);
        discardLabel.setLayoutY(discardY + cardHeight + 5);
        
        // 添加到视图
        viewPane.getChildren().addAll(floodCardBack, floodDeckLabel, discardPane, discardLabel);
    }
    
    /**
     * 添加宝藏卡符号到视图中
     */
    private void addTreasureCardSymbol() {
        // 宝藏卡符号的位置 - 调整到岛屿下方
        double cardWidth = 80;  // 卡片宽度
        double cardHeight = 120; // 卡片高度
        double symbolX = 650;  // 宝藏卡堆的X坐标
        double symbolY = 850;  // 宝藏卡堆的Y坐标 - 调整到洪水卡下方
        
        // 创建宝藏卡符号视图
        ImageView treasureSymbolView = new ImageView(treasureCardSymbolImage);
        treasureSymbolView.setFitWidth(cardWidth);
        treasureSymbolView.setFitHeight(cardHeight);
        treasureSymbolView.setPreserveRatio(true);
        treasureSymbolView.setLayoutX(symbolX);
        treasureSymbolView.setLayoutY(symbolY);
        
        // 添加标签
        Label treasureLabel = new Label("宝藏卡牌堆");
        treasureLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: rgba(0,0,0,0.7); -fx-padding: 3px; -fx-background-radius: 3;");
        treasureLabel.setLayoutX(symbolX);
        treasureLabel.setLayoutY(symbolY + cardHeight + 5);
        
        // 添加到视图
        viewPane.getChildren().addAll(treasureSymbolView, treasureLabel);
    }
    
    /**
     * 添加游戏日志区域
     */
    private void addGameLogArea() {
        // 创建游戏日志区域
        VBox logArea = new VBox(5);
        logArea.setPrefSize(600, 300);
        logArea.setLayoutX(100);
        logArea.setLayoutY(1050); // 放在卡牌区域下方
        logArea.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-padding: 10px; -fx-border-color: white; -fx-border-width: 1px;");
        
        // 添加标题
        Label logTitle = new Label("游戏日志");
        logTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        
        // 创建日志内容区域（使用滚动面板）
        ScrollPane logScrollPane = new ScrollPane();
        logScrollPane.setPrefSize(580, 260);
        logScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        // 创建日志内容
        VBox logContent = new VBox(3);
        logContent.setPrefWidth(560);
        logContent.setStyle("-fx-background-color: transparent;");
        
        // 添加一些示例日志条目
        for (int i = 1; i <= 5; i++) {
            Label logEntry = new Label("游戏日志示例 #" + i);
            logEntry.setStyle("-fx-text-fill: white;");
            logContent.getChildren().add(logEntry);
        }
        
        // 设置滚动面板内容
        logScrollPane.setContent(logContent);
        
        // 添加到日志区域
        logArea.getChildren().addAll(logTitle, logScrollPane);
        
        // 添加到主视图
        viewPane.getChildren().add(logArea);
        
        // 保存对日志内容的引用，以便稍后添加新的日志条目
        this.logContent = logContent;
    }
    
    // 新增成员变量
    private ScrollPane scrollPane;
    private VBox logContent;

    /**
     * 根据洪水卡编号获取对应的瓦片名称
     * @param cardNumber 洪水卡编号
     * @return 瓦片名称
     */
    private String getTileNameForFloodCard(int cardNumber) {
        String[] tileNames = {
            "Fool's Landing", "Bronze Gate", "Cave of Embers", "Cave of Shadows",
            "Copper Gate", "Coral Palace", "Crimson Forest", "Dunes of Deception",
            "Gold Gate", "Iron Gate", "Lost Lagoon", "Misty Marsh",
            "Observatory", "Phantom Rock", "Silver Gate", "Temple of the Moon",
            "Temple of the Sun", "Tidal Palace", "Twilight Hollow", "Watchtower",
            "Whispering Garden", "Howling Garden", "Breakers Bridge", "Cliffs of Abandon"
        };
        
        if (cardNumber >= 1 && cardNumber <= tileNames.length) {
            return tileNames[cardNumber - 1];
        }
        return "Tile " + cardNumber;
    }
}

