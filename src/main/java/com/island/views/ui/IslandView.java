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
    private ImageView waterLevelView;  // Water level image view
    private Rectangle waterLevelIndicator; // Water level indicator
    private StackPane selectedTilePane; // Currently selected tile
    private List<Tile> highlightedTiles; // List of currently highlighted tiles
    static final double SCALE = 0.55;  // Scale factor
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

        // Save currently highlighted tiles
        List<Tile> tilesToHighlight = null;
        if (highlightedTiles != null && !highlightedTiles.isEmpty()) {
            tilesToHighlight = new ArrayList<>(highlightedTiles);
        }

        // Clear existing tiles first
        boardGrid.getChildren().clear();
        selectedTilePane = null;

        Room room = islandController.getRoom();
        // Iterate through all positions and tiles
        for (Map.Entry<Position, Tile> entry : tiles.entrySet()) {
            Position pos = entry.getKey();
            Tile tile = entry.getValue();

            if (!tile.isSunk()) {
                try {
                    // Create a StackPane for layering tile and player pieces
                    StackPane tileStack = new StackPane();

                    // Load tile image
                    String imagePath = tile.isNormal() ? ("/islands/" + tile.getName() + ".png") : ("/islands/" + tile.getName() + "_flood.png");
                    Image tileImage = new Image(getClass().getResourceAsStream(imagePath));
                    ImageView tileView = new ImageView(tileImage);

                    // Set image size
                    tileView.setFitWidth(TILE_SIZE);
                    tileView.setFitHeight(TILE_SIZE);

                    // Add tile image to StackPane
                    tileStack.getChildren().add(tileView);

                    // Check if any players are on this tile
                    checkPlayersOnTile(room, pos, tileStack);

                    // Add click event handler
                    tileStack.setOnMouseClicked(event -> {
                        // Clear all previous borders
                        clearAllBoarders();
                        // Add new border
                        addBoarder(tile);
                        islandController.handleTileClick(tile);
                    });

                    // Add StackPane to grid
                    boardGrid.add(tileStack, pos.getX(), pos.getY());

                } catch (Exception e) {
                    System.err.println("Unable to load image " + tile.getName() + ": " + e.getMessage());
                }
            } else {
                // Create a StackPane for layering tile and player pieces
                StackPane tileStack = new StackPane();

                // Check if any players are on this tile
                checkPlayersOnTile(room, pos, tileStack);
                // Add StackPane to grid
                boardGrid.add(tileStack, pos.getX(), pos.getY());
            }
        }

        // Place Treasures
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
                System.err.println("Unable to load image " + treasureNames[i] + ": " + e.getMessage());
            }
        }

        // Restore previously highlighted tiles
        if (tilesToHighlight != null) {
            addBoarders(tilesToHighlight);
        }
    }

    private void checkPlayersOnTile(Room room, Position pos, StackPane tileStack) {
        for (Player player : room.getPlayers()) {
            if (player.getPosition() != null && player.getPosition().equals(pos)) {
                try {
                    // Load player piece image
                    String playerImagePath = "/players/" + PlayerRole.getColor(player.getRole()) + ".png";
                    Image playerImage = new Image(getClass().getResourceAsStream(playerImagePath));
                    ImageView playerView = new ImageView(playerImage);

                    // Set piece size (smaller than tile)
                    playerView.setFitWidth(73 * 0.4);
                    playerView.setFitHeight(131 * 0.4);

                    // Add player piece to StackPane
                    tileStack.getChildren().add(playerView);
                } catch (Exception e) {
                    System.err.println("Unable to load player piece image: " + e.getMessage());
                }
            }
        }
    }

    public void addBoarders(List<Tile> tiles) {
        // Save list of highlighted tiles
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
        // Find corresponding StackPane in grid and add border
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

    // Clear all borders
    public void clearAllBoarders() {
        if (highlightedTiles != null) {
            highlightedTiles.clear();
        }
        
        // Clear border style from all tiles
        for (javafx.scene.Node node : boardGrid.getChildren()) {
            if (node instanceof StackPane tilePane) {
                tilePane.setStyle("");
            }
        }
    }

    public void initWaterLevel() {
        waterLevelBox.getChildren().clear();
        if (islandController != null) {
            // Load water level bar image
            Image waterLevelImage = new Image(getClass().getResourceAsStream("/islands/flood_meter.png"));
            waterLevelView = new ImageView(waterLevelImage);
            waterLevelView.setFitHeight(250);  // Set height
            waterLevelView.setPreserveRatio(true);  // Maintain aspect ratio

            // Add water level indicator
            StackPane waterLevelStack = new StackPane();
            waterLevelStack.getChildren().add(waterLevelView);

            // Create water level indicator (semi-transparent red rectangle)
            waterLevelIndicator = new Rectangle(40, 15);
            waterLevelIndicator.setFill(Color.rgb(255, 0, 0, 0.5)); // Semi-transparent red
            waterLevelIndicator.setStroke(Color.BLACK);
            waterLevelIndicator.setStrokeWidth(0);

            // 160px margin from top
            VBox.setMargin(waterLevelStack, new Insets(140, 0, 0, 0));

            // Set indicator position based on current water level
            updateWaterLevelIndicator();

            waterLevelStack.getChildren().add(waterLevelIndicator);

            waterLevelBox.getChildren().addAll(waterLevelStack);
        }
    }
    // Update water level indicator position
    public void updateWaterLevelIndicator() {
        int waterLevel = islandController.getWaterLevel();
        // Water level 1-10, mapped to positions on water level bar
        double yOffset = (waterLevel - 1) * 21 + 28; // Adjusted based on actual water level bar image

        // Set indicator position (relative to water level bar)
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
     * Close island view, clean up resources
     */
    public void shutdown() {
        // Clean up grid resources
        if (boardGrid != null) {
            boardGrid.getChildren().clear();
        }
        // Clean up water level display resources
        if (waterLevelBox != null) {
            waterLevelBox.getChildren().clear();
        }
        // Clean up controller references
        islandController = null;
    }
}
