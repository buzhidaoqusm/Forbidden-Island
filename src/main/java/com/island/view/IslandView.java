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

        // Add grid to main view
        viewPane.getChildren().add(gridPane);
        
        // Adjust grid position to center it on the map
        gridPane.setLayoutX((800 - 6 * TILE_SIZE - 5 * 5) / 2);
        gridPane.setLayoutY((600 - 6 * TILE_SIZE - 5 * 5) / 2);
        
        // Add water level meter image
        if (floodMeterImage != null) {
            floodMeterView = new ImageView(floodMeterImage);
            floodMeterView.setFitWidth(80);
            floodMeterView.setFitHeight(350);
            floodMeterView.setPreserveRatio(true);
            floodMeterView.setLayoutX(680);
            floodMeterView.setLayoutY(100);
            viewPane.getChildren().add(floodMeterView);
        
            // Add water level indicator
            updateWaterLevelIndicator(1);  // Initial water level is 1
        }
        
        // Add flood card stack
        addFloodCardStack();
        
        // Add treasure card symbol
        addTreasureCardSymbol();
        
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
                            System.out.println("Updating player position: " + player.getName() + " at " + player.getPosition());
                            updatePlayerMarker(player, player.getPosition());
                        }
                    }
                }
                
                // Highlight valid positions if any
                List<Position> validPositions = islandController.getValidPositions();
                if (validPositions != null && !validPositions.isEmpty()) {
                    highlightTiles(validPositions, "valid_move");
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
        // 直接映射瓦片名称到我们使用的图片类型
        // 首先检查是否是我们直接使用的类型之一
        String[] directTypes = {
            "Fire1", "Green", "Red", "Normal8", "Blue", "Wind1", "Earth2", "Normal1", 
            "Normal10", "Normal2", "White", "Yellow", "Normal3", "Normal5", "Normal9", 
            "Earth1", "Ocean2", "Fire2", "Black", "Ocean1", "Normal4", "Normal6", "Wind2", "Normal7"
        };
        
        for (String type : directTypes) {
            if (tileName.equals(type)) {
                return type;
            }
        }
        
        // 如果不是直接类型，根据名称特征匹配
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
        
        // 使用与主岛屿布局相同的瓦片类型顺序
        String[] tileTypes = {
            "Fire1", 
            "Green", 
            "Red", 
            "Normal8", 
            "Blue", 
            "Wind1", 
            "Earth2", 
            "Normal1", 
            "Normal10", 
            "Normal2", 
            "White", 
            "Yellow", 
            "Normal3", 
            "Normal5", 
            "Normal9", 
            "Earth1", 
            "Ocean2", 
            "Fire2", 
            "Black", 
            "Ocean1", 
            "Normal4", 
            "Normal6", 
            "Wind2", 
            "Normal7"
        };
        
        // 选择前8张卡片进行展示
        List<String> selectedTypes = new ArrayList<>();
        int displayCount = Math.min(8, tileTypes.length);
        
        for (int i = 0; i < displayCount; i++) {
            selectedTypes.add(tileTypes[i]);
        }
        
        // 添加选中的卡片到堆中，稍微错开以显示叠放效果
        for (int i = 0; i < selectedTypes.size(); i++) {
            String tileType = selectedTypes.get(i);
            String imagePath = "/islands/" + tileType + ".png";
            ImageView cardView;
            
            // 尝试加载图片
            try {
                Image cardImage = new Image(getClass().getResourceAsStream(imagePath));
                if (cardImage != null && !cardImage.isError()) {
                    cardView = new ImageView(cardImage);
                } else {
                    // 如果找不到图片，使用默认背面
                    cardView = new ImageView(floodCardBackImage);
                }
            } catch (Exception e) {
                // 如果加载失败，使用默认背面
                cardView = new ImageView(floodCardBackImage);
                System.err.println("无法加载卡片图片: " + imagePath + " - " + e.getMessage());
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

