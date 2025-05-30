package com.island.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import com.island.models.Island;
import com.island.models.Tile;
import com.island.models.TileState;
import com.island.models.Position;
import com.island.models.TreasureType;

public class IslandViewTest extends Application {

    private StandaloneIslandView islandView;
    private int currentWaterLevel = 2;

    @Override
    public void start(Stage primaryStage) {
        try {
            // 创建独立的 IslandView 实例
            islandView = new StandaloneIslandView();

            // 获取 IslandView 的主视图
            Pane islandViewPane = islandView.getView();

            // 创建控制面板
            VBox controlPanel = createControlPanel();

            // 创建主布局
            HBox root = new HBox(10);
            root.setPadding(new Insets(10));
            root.getChildren().addAll(islandViewPane, controlPanel);

            // 创建场景
            Scene scene = new Scene(root, 1000, 700);

            // 设置并显示主舞台
            primaryStage.setTitle("禁闭岛主界面测试");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createControlPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc;");

        // 水位线控制
        Label waterLevelLabel = new Label("水位线控制");
        waterLevelLabel.setStyle("-fx-font-weight: bold;");

        Button increaseWaterLevelBtn = new Button("增加水位线");
        increaseWaterLevelBtn.setOnAction(e -> {
            if (currentWaterLevel < 5) {
                currentWaterLevel++;
                updateWaterLevel();
            }
        });

        Button decreaseWaterLevelBtn = new Button("减少水位线");
        decreaseWaterLevelBtn.setOnAction(e -> {
            if (currentWaterLevel > 2) {
                currentWaterLevel--;
                updateWaterLevel();
            }
        });

        HBox waterLevelControls = new HBox(10, decreaseWaterLevelBtn, increaseWaterLevelBtn);

        // 岛屿状态控制
        Label islandStateLabel = new Label("岛屿状态控制");
        islandStateLabel.setStyle("-fx-font-weight: bold;");

        Button floodRandomTileBtn = new Button("随机淹没一个岛屿");
        floodRandomTileBtn.setOnAction(e -> floodRandomTile());

        Button restoreAllTilesBtn = new Button("恢复所有岛屿");
        restoreAllTilesBtn.setOnAction(e -> restoreAllTiles());

        // 添加所有控件到面板
        panel.getChildren().addAll(
                waterLevelLabel,
                waterLevelControls,
                new Label("当前水位线: " + currentWaterLevel),
                islandStateLabel,
                floodRandomTileBtn,
                restoreAllTilesBtn
        );

        return panel;
    }

    private void updateWaterLevel() {
        if (islandView != null) {
            islandView.setWaterLevel(currentWaterLevel);
            System.out.println("水位线已更新为: " + currentWaterLevel);
        }
    }

    private void floodRandomTile() {
        if (islandView != null) {
            islandView.floodRandomTile();
            System.out.println("已随机淹没一个岛屿");
        }
    }

    private void restoreAllTiles() {
        if (islandView != null) {
            islandView.restoreAllTiles();
            System.out.println("已恢复所有岛屿");
        }
    }

    // 独立的 IslandView 类，不依赖于 GameController
    public class StandaloneIslandView {
        private GridPane gridPane;
        private Pane viewPane;
        private Map<Position, Pane> tileViews = new HashMap<>();
        private Map<Position, MockTile> tiles = new HashMap<>();
        private int waterLevel = 2;

        // 常量
        private static final double TILE_SIZE = 80.0;
        private static final Color NORMAL_TILE_COLOR = Color.SANDYBROWN;
        private static final Color FLOODED_TILE_COLOR = Color.LIGHTBLUE;
        private static final Color SUNK_TILE_COLOR = Color.DARKBLUE;

        public StandaloneIslandView() {
            initialize();
        }

        private void initialize() {
            // 创建网格布局
            gridPane = new GridPane();
            gridPane.setHgap(5);
            gridPane.setVgap(5);
            gridPane.setPadding(new Insets(10));
            gridPane.setStyle("-fx-background-color: transparent;");

            // 初始化岛屿布局
            initializeIsland();

            // 创建主视图面板
            viewPane = new Pane();

            // 创建背景
            Canvas background = new Canvas(500, 500);
            GraphicsContext gc = background.getGraphicsContext2D();
            gc.setFill(Color.BLUE.deriveColor(1, 1, 1, 0.3));
            gc.fillRect(0, 0, 500, 500);

            // 添加背景和网格到主视图
            viewPane.getChildren().addAll(background, gridPane);

            // 调整网格位置
            gridPane.setLayoutX(50);
            gridPane.setLayoutY(50);

            System.out.println("StandaloneIslandView 初始化完成");
        }

        private void initializeIsland() {
            // 初始化游戏地图，与 IslandView 中的布局对应
            String[] islandNames = {
                    // 第一行
                    "Blue", "Red",
                    // 第二行
                    "Green", "Earth1", "Fire1", "Yellow",
                    // 第三行
                    "Normal1", "Normal2", "Normal3", "Normal4", "Normal5", "Normal6",
                    // 第四行
                    "Normal7", "Normal8", "Normal9", "Normal10", "Wind1", "Ocean1",
                    // 第五行
                    "Black", "Earth2", "Fire2", "White",
                    // 第六行
                    "Wind2", "Ocean2"
            };

            int[][] islandLayout = {
                    {0, 0, 1, 1, 0, 0},
                    {0, 1, 1, 1, 1, 0},
                    {1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1},
                    {0, 1, 1, 1, 1, 0},
                    {0, 0, 1, 1, 0, 0}
            };

            int islandIndex = 0;
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 6; col++) {
                    if (islandLayout[row][col] == 1) {
                        String islandName = islandNames[islandIndex++];
                        Position pos = new Position(row, col);
                        MockTile tile = new MockTile(islandName, pos, null);
                        tiles.put(pos, tile);

                        // 创建瓦片视图
                        Pane tileView = createTileView(tile);
                        tileViews.put(pos, tileView);

                        // 添加到网格
                        gridPane.add(tileView, col, row);
                    } else {
                        // 如果这个位置没有瓦片，添加一个空白的Pane
                        Pane emptyPane = new Pane();
                        emptyPane.setPrefSize(TILE_SIZE, TILE_SIZE);
                        gridPane.add(emptyPane, col, row);
                    }
                }
            }
        }

        private Pane createTileView(MockTile tile) {
            Pane tilePane = new Pane();
            tilePane.setPrefSize(TILE_SIZE, TILE_SIZE);

            // 创建瓦片背景
            Rectangle tileRect = new Rectangle(TILE_SIZE, TILE_SIZE);

            // 根据瓦片状态设置颜色
            switch (tile.getState()) {
                case NORMAL:
                    tileRect.setFill(NORMAL_TILE_COLOR);
                    break;
                case FLOODED:
                    tileRect.setFill(FLOODED_TILE_COLOR);
                    break;
                case SUNK:
                    tileRect.setFill(SUNK_TILE_COLOR);
                    break;
            }

            tileRect.setStroke(Color.BLACK);
            tileRect.setStrokeWidth(1);

            // 创建瓦片标签
            Label nameLabel = new Label(tile.getName());
            nameLabel.setTextFill(Color.BLACK);
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
            nameLabel.setLayoutX(5);
            nameLabel.setLayoutY(5);

            // 将背景和标签添加到瓦片面板
            tilePane.getChildren().addAll(tileRect, nameLabel);

            return tilePane;
        }

        public void updateTileView(Position pos) {
            MockTile tile = tiles.get(pos);
            if (tile != null && tileViews.containsKey(pos)) {
                Pane tileView = tileViews.get(pos);
                tileView.getChildren().clear();

                // 重新创建瓦片视图
                Rectangle tileRect = new Rectangle(TILE_SIZE, TILE_SIZE);

                // 根据瓦片状态设置颜色
                switch (tile.getState()) {
                    case NORMAL:
                        tileRect.setFill(NORMAL_TILE_COLOR);
                        break;
                    case FLOODED:
                        tileRect.setFill(FLOODED_TILE_COLOR);
                        break;
                    case SUNK:
                        tileRect.setFill(SUNK_TILE_COLOR);
                        break;
                }

                tileRect.setStroke(Color.BLACK);
                tileRect.setStrokeWidth(1);

                // 创建瓦片标签
                Label nameLabel = new Label(tile.getName());
                nameLabel.setTextFill(Color.BLACK);
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
                nameLabel.setLayoutX(5);
                nameLabel.setLayoutY(5);

                // 将背景和标签添加到瓦片面板
                tileView.getChildren().addAll(tileRect, nameLabel);
            }
        }

        public void setWaterLevel(int waterLevel) {
            this.waterLevel = waterLevel;
            // 在实际应用中，这里可以更新水位线的视觉显示
        }

        public void floodRandomTile() {
            // 随机选择一个岛屿并淹没它
            List<Position> positions = new ArrayList<>(tiles.keySet());
            if (!positions.isEmpty()) {
                int randomIndex = (int)(Math.random() * positions.size());
                Position randomPos = positions.get(randomIndex);
                MockTile tile = tiles.get(randomPos);
                if (tile != null) {
                    if (tile.getState() == TileState.NORMAL) {
                        tile.setState(TileState.FLOODED);
                    } else if (tile.getState() == TileState.FLOODED) {
                        tile.setState(TileState.SUNK);
                    }
                    updateTileView(randomPos);
                }
            }
        }

        public void restoreAllTiles() {
            // 恢复所有岛屿到正常状态
            for (Map.Entry<Position, MockTile> entry : tiles.entrySet()) {
                MockTile tile = entry.getValue();
                tile.setState(TileState.NORMAL);
                updateTileView(entry.getKey());
            }
        }

        public Pane getView() {
            return viewPane;
        }
    }

    // 简单的 MockTile 类
    public class MockTile {
        private String name;
        private Position position;
        private TileState state;
        private TreasureType treasureType;

        public MockTile(String name, Position position, TreasureType treasureType) {
            this.name = name;
            this.position = position;
            this.treasureType = treasureType;
            this.state = TileState.NORMAL;
        }

        public String getName() {
            return name;
        }

        public TileState getState() {
            return state;
        }

        public void setState(TileState state) {
            this.state = state;
        }

        public boolean isShoredUp() {
            return false;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}