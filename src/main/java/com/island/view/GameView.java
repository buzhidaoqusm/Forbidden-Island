package com.island.view;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.scene.control.ScrollPane;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.island.view.IslandView;
import com.island.view.PlayerView;
import com.island.view.CardView;
import com.island.view.ActionBarView;
import com.island.view.ActionLogView;
import com.island.controller.GameController;
import com.island.model.Player;
import com.island.model.Position;

public class GameView {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private Scene gameScene;

    // Sub-views (actual instances)
    private IslandView islandView;
    private PlayerView playerView;
    private CardView cardView;
    private ActionBarView actionBarView;
    private ActionLogView actionLogView;
    
    // Sub-view panes
    private ScrollPane islandViewPane;
    private Pane playerViewPane;
    private Pane cardViewPane;
    private Pane actionBarViewPane;
    private VBox actionLogViewPane;
    
    // Game interface image resources
    private Image gameBackgroundImage;
    private Image victoryIcon;
    private Image defeatIcon;
    private Map<Integer, Image> treasureIcons = new HashMap<>();

    // GameController reference
    private GameController gameController;

    // Constructor
    public GameView(Stage primaryStage, GameController gameController) {
        this.primaryStage = primaryStage;
        this.gameController = gameController;
        loadImages();
        initialize();
    }
    
    /**
     * Load game interface images
     */
    private void loadImages() {
        try {
            // 使用多种可能的路径尝试加载图片
            // Load game background image
            String[] backgroundPaths = {
                "/Map/background.jpg",
                "/images/background.jpg",
                "/Map/Arena.jpg"
            };
            
            for (String path : backgroundPaths) {
                try {
                    gameBackgroundImage = new Image(getClass().getResourceAsStream(path));
                    if (gameBackgroundImage != null && !gameBackgroundImage.isError()) {
                        System.out.println("成功加载背景图: " + path);
                        break;
                    }
                } catch (Exception e) {
                    // 继续尝试下一个路径
                }
            }
            
            // 如果背景加载失败，创建默认背景
            if (gameBackgroundImage == null || gameBackgroundImage.isError()) {
                // 创建一个渐变背景作为备用
                javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(1200, 800);
                javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
                
                // 绘制渐变背景
                javafx.scene.paint.LinearGradient gradient = 
                    new javafx.scene.paint.LinearGradient(0, 0, 0, 800, false, 
                    javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new javafx.scene.paint.Stop(0, Color.LIGHTBLUE),
                    new javafx.scene.paint.Stop(1, Color.DARKBLUE));
                
                gc.setFill(gradient);
                gc.fillRect(0, 0, 1200, 800);
                
                // 将Canvas转换为Image
                javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                params.setFill(Color.TRANSPARENT);
                gameBackgroundImage = canvas.snapshot(params, null);
                
                System.out.println("使用默认背景替代图片");
            }
            
            // Load game status icons
            // 尝试加载胜利图标
            String[] victoryPaths = {
                "/Design/victory.png",
                "/images/victory.png",
                "/Design/Icons/victory.png"
            };
            
            for (String path : victoryPaths) {
                try {
                    victoryIcon = new Image(getClass().getResourceAsStream(path));
                    if (victoryIcon != null && !victoryIcon.isError()) {
                        System.out.println("成功加载胜利图标: " + path);
                        break;
                    }
                } catch (Exception e) {
                    // 继续尝试
                }
            }
            
            // 如果胜利图标加载失败，创建默认图标
            if (victoryIcon == null || victoryIcon.isError()) {
                javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(200, 200);
                javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
                
                // 绘制一个金色圆圈和胜利标志
                gc.setFill(Color.GOLD);
                gc.fillOval(20, 20, 160, 160);
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(10);
                gc.strokeLine(60, 100, 90, 130);
                gc.strokeLine(90, 130, 140, 70);
                
                javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                params.setFill(Color.TRANSPARENT);
                victoryIcon = canvas.snapshot(params, null);
                
                System.out.println("使用默认胜利图标");
            }
            
            // 尝试加载失败图标
            String[] defeatPaths = {
                "/Design/defeat.png",
                "/images/defeat.png",
                "/Design/Icons/defeat.png"
            };
            
            for (String path : defeatPaths) {
                try {
                    defeatIcon = new Image(getClass().getResourceAsStream(path));
                    if (defeatIcon != null && !defeatIcon.isError()) {
                        System.out.println("成功加载失败图标: " + path);
                        break;
                    }
                } catch (Exception e) {
                    // 继续尝试
                }
            }
            
//            // 如果失败图标加载失败，创建默认图标
//            if (defeatIcon == null || defeatIcon.isError()) {
//                javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(200, 200);
//                javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
//
//                // 绘制一个红色圆圈和叉号
//                gc.setFill(Color.DARKRED);
//                gc.fillOval(20, 20, 160, 160);
//                gc.setStroke(Color.WHITE);
//                gc.setLineWidth(10);
//                gc.strokeLine(60, 60, 140, 140);
//                gc.strokeLine(60, 140, 140, 60);
//
//                javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
//                params.setFill(Color.TRANSPARENT);
//                defeatIcon = canvas.snapshot(params, null);
//
//                System.out.println("使用默认失败图标");
//            }
            
            // Load treasure icons with fallback
            String[][] treasurePaths = {
                {"/TreasureCards/earth_stone.png", "/images/treasures/earth_stone.png"},
                {"/TreasureCards/wind_statue.png", "/images/treasures/wind_statue.png"},
                {"/TreasureCards/fire_crystal.png", "/images/treasures/fire_crystal.png"},
                {"/TreasureCards/ocean_chalice.png", "/images/treasures/ocean_chalice.png"}
            };
            
//            // 定义备用颜色
//            Color[] fallbackColors = {
//                Color.BROWN, // 地
//                Color.LIGHTBLUE, // 风
//                Color.RED, // 火
//                Color.BLUE // 水
//            };
//
//            for (int i = 0; i < treasurePaths.length; i++) {
//                Image treasureImage = null;
//
//                // 尝试多个路径
//                for (String path : treasurePaths[i]) {
//                    try {
//                        treasureImage = new Image(getClass().getResourceAsStream(path));
//                        if (treasureImage != null && !treasureImage.isError()) {
//                            treasureIcons.put(i + 1, treasureImage);
//                            System.out.println("成功加载宝藏图标 " + (i+1) + ": " + path);
//                            break;
//                        }
//                    } catch (Exception e) {
//                        // 继续尝试下一个路径
//                    }
//                }
//
//                // 如果还是失败，创建一个备用图标
//                if (treasureImage == null || treasureImage.isError()) {
//                    javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(100, 100);
//                    javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
//
//                    // 绘制一个简单的宝藏图标
//                    gc.setFill(fallbackColors[i]);
//                    gc.fillRect(10, 10, 80, 80);
//                    gc.setFill(Color.GOLD);
//                    gc.fillOval(30, 30, 40, 40);
//                    gc.setStroke(Color.BLACK);
//                    gc.strokeRect(10, 10, 80, 80);
//
//                    javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
//                    params.setFill(Color.TRANSPARENT);
//                    treasureIcons.put(i + 1, canvas.snapshot(params, null));
//
//                    System.out.println("使用默认宝藏图标 " + (i+1));
//                }
//            }
            
//            System.out.println("游戏界面图片资源加载完成，成功: " + treasureIcons.size() + " 个宝藏图标");
            
        } catch (Exception e) {
            System.err.println("游戏界面图片资源加载失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initialize() {
        rootLayout = new BorderPane();
        
        // If background image exists, add it to StackPane
        if (gameBackgroundImage != null) {
            StackPane rootContainer = new StackPane();
            
            // Add background image
            ImageView backgroundImageView = new ImageView(gameBackgroundImage);
            backgroundImageView.setFitWidth(1200);
            backgroundImageView.setFitHeight(800);
            backgroundImageView.setPreserveRatio(false); // Stretch to fill entire area
            
            // Add semi-transparent overlay for better readability
            javafx.scene.shape.Rectangle overlay = new javafx.scene.shape.Rectangle(1200, 800);
            overlay.setFill(Color.rgb(173, 216, 230, 0.5)); // Semi-transparent light blue
            
            rootContainer.getChildren().addAll(backgroundImageView, overlay, rootLayout);
            
            // Create scene
            gameScene = new Scene(rootContainer, 1200, 800);
        } else {
            // If no background image, use solid color background
            rootLayout.setStyle("-fx-background-color: #add8e6;"); // Light blue background
            gameScene = new Scene(rootLayout, 1200, 800);
        }

        // Initialize view components
        islandView = new IslandView(gameController);
        playerView = new PlayerView(gameController);
        // cardView is not displayed anymore as per requirement
        cardView = null; // Set to null to indicate it's not used
        actionBarView = new ActionBarView(gameController);
        // We're not using a separate ActionLogView anymore, using the one integrated in ActionBarView
        actionLogView = null;

        // Register views as observers to be implemented later
        // Currently the observer pattern is not fully implemented in GameController

        // Get panes from view components
        islandViewPane = islandView.getView();
        playerViewPane = playerView.getView();
        // cardViewPane is not needed anymore
        cardViewPane = null;
        actionBarViewPane = actionBarView.getView();
        // actionLogViewPane is not needed anymore since it's integrated in ActionBarView
        actionLogViewPane = null;

        // Set layout
        // Center: Island view (main game board)
        rootLayout.setCenter(islandViewPane);

        // Left: Player information view
        rootLayout.setLeft(playerViewPane);

        // Bottom: Action Bar only - we'll integrate the log into the action bar view
        // This eliminates the duplicate logs and reduces vertical space usage
        rootLayout.setBottom(actionBarViewPane);
        
        // Add appropriate margin
        BorderPane.setMargin(actionBarViewPane, new Insets(5, 10, 10, 10));

        // Set margins
        BorderPane.setMargin(playerViewPane, new Insets(10));
        
        // 记录游戏初始化日志
        actionBarView.addLogMessage("游戏界面初始化完成");
    }

    public Scene getScene() {
        return gameScene;
    }

    public void showGameOverInterface() {
        Platform.runLater(() -> {
            // Create game over overlay
            StackPane gameOverPane = new StackPane();
            gameOverPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);"); // Semi-transparent black background
            
            VBox gameOverContent = new VBox(20);
            gameOverContent.setAlignment(javafx.geometry.Pos.CENTER);
            
            // Add game over icon (defeat)
            if (defeatIcon != null) {
                ImageView iconView = new ImageView(defeatIcon);
                iconView.setFitWidth(200);
                iconView.setFitHeight(200);
                iconView.setPreserveRatio(true);
                gameOverContent.getChildren().add(iconView);
            }
            
            // Add game over text
            Label gameOverLabel = new Label("Game Over");
            gameOverLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
            gameOverLabel.setStyle("-fx-text-fill: red;");
            gameOverContent.getChildren().add(gameOverLabel);
            
            gameOverPane.getChildren().add(gameOverContent);
            rootLayout.setCenter(gameOverPane); // Replace center content
            
            // 记录游戏结束日志
            if (actionBarView != null) {
                actionBarView.addLogMessage("游戏结束 - 你们失败了！");
            }
            
            System.out.println("Showing game over interface");
        });
    }
    
    public void showVictoryInterface() {
        Platform.runLater(() -> {
            // Create victory overlay
            StackPane victoryPane = new StackPane();
            victoryPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);"); // Semi-transparent black background
            
            VBox victoryContent = new VBox(20);
            victoryContent.setAlignment(javafx.geometry.Pos.CENTER);
            
            // Add victory icon
            if (victoryIcon != null) {
                ImageView iconView = new ImageView(victoryIcon);
                iconView.setFitWidth(200);
                iconView.setFitHeight(200);
                iconView.setPreserveRatio(true);
                victoryContent.getChildren().add(iconView);
            }
            
            // Add victory text
            Label victoryLabel = new Label("Victory!");
            victoryLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
            victoryLabel.setStyle("-fx-text-fill: gold;");
            victoryContent.getChildren().add(victoryLabel);
            
            victoryPane.getChildren().add(victoryContent);
            rootLayout.setCenter(victoryPane); // Replace center content
            
            // 记录胜利日志
            if (actionBarView != null) {
                actionBarView.addLogMessage("恭喜！你们成功逃离了禁闭岛！");
            }
            
            System.out.println("Showing victory interface");
        });
    }
    
    /**
     * Initialize the game, update all view components
     */
    public void initGame() {
        // Notify all view components to update
        updateAllViews();
    }
    
    /**
     * Update all view components
     */
    public void updateAllViews() {
        if (gameController == null) return;
        
        if (islandView != null) {
            islandView.update();
            System.out.println("Update island view");
        }
        
        if (playerView != null) {
            playerView.update();
            System.out.println("Update player view");
        }
        
        // Card view is no longer used
        // if (cardView != null) {
        //     cardView.update();
        //     System.out.println("Update card view");
        // }
        
        if (actionBarView != null) {
            actionBarView.update();
            System.out.println("Update actionbar view");
        }
    }

    /**
     * Set the primary stage scene and show it
     */
    public void setPrimaryStage() {
        try {
            primaryStage.setTitle("Forbidden Island - Game");
            primaryStage.setScene(gameScene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();
            System.out.println("Game window displayed successfully");
        } catch (Exception e) {
            System.err.println("Error displaying game window: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Called when returning to main menu
     */
    public void returnToMainMenu() {
        // This would connect to GameController if the method was implemented
        System.out.println("Returning to main menu");
        // Future implementation will call: gameController.showMainMenu();
    }

    public IslandView getIslandView() { return islandView; }
    public PlayerView getPlayerView() { return playerView; }
    public CardView getCardView() { return cardView; }
    public ActionBarView getActionBarView() { return actionBarView; }
    public ActionLogView getActionLogView() { return actionLogView; }
    
    /**
     * Update player position on the board
     * @param player Player to update
     * @param newPosition New position
     */
    public void updatePlayerPosition(Player player, Position newPosition) {
        if (islandView != null) {
            islandView.updatePlayerMarker(player, newPosition);
        }
        if (playerView != null) {
            playerView.updatePlayerInfo(player);
        }
        if (actionBarView != null) {
            actionBarView.addLogMessage(player.getName() + " 移动到位置 [" + newPosition.getX() + ", " + newPosition.getY() + "]");
        }
    }
    
    /**
     * Update method called by observed subjects
     * Will be used when Observer pattern is implemented
     */
    public void update() {
        updateAllViews();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Highlights valid positions on the island view.
     * This is used to show players where they can move, shore up, etc.
     *
     * @param positions The list of positions to highlight
     * @param highlightType The type of highlight to apply (e.g., "move", "shore_up")
     */
    public void highlightValidPositions(List<Position> positions, String highlightType) {
        if (islandView != null) {
            islandView.highlightTiles(positions, highlightType);
        }
    }

    /**
     * Resets all tile borders, removing any highlights.
     */
    public void resetTileBorders() {
        if (islandView != null) {
            islandView.clearHighlights();
        }
    }
}
