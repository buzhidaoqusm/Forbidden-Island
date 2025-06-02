package com.island.views.ui;

import com.island.controller.IslandController;
import com.island.models.*;
import com.island.models.adventurers.Player;
import com.island.models.adventurers.PlayerRole;
import com.island.models.island.Island;
import com.island.models.island.Position;
import com.island.models.island.Tile;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
    private List<Tile> highlightedTiles; // List of currently highlighted tiles
    static final double SCALE = 0.55;  // Scale factor
    private static final double TILE_SIZE = 147 * SCALE;
    private static final Color HIGHLIGHT_COLOR = Color.RED;
    private static final double BORDER_WIDTH = 2.0;

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
        // Get all players on this tile
        List<Player> playersOnTile = room.getPlayers().stream()
                .filter(player -> player.getPosition() != null && player.getPosition().equals(pos))
                .toList();

        // If there are no players on this tile, return early
        if (playersOnTile.isEmpty()) {
            return;
        }

        // Calculate offsets based on number of players
        double[][] offsets = {
                {0, 0},                    // 1 player - center
                {-10, -10, 10, 10},       // 2 players - diagonal
                {-15, -15, 0, 0, 15, 15}, // 3 players - triangle
                {-15, -15, -15, 15, 15, -15, 15, 15} // 4 players - corners
        };

        // Get the appropriate offset array based on number of players (capped at 4)
        int playerCount = Math.min(playersOnTile.size(), 4);
        double[] currentOffsets = offsets[playerCount - 1];

        // Add each player with their calculated offset
        for (int i = 0; i < playersOnTile.size() && i < 4; i++) {
            Player player = playersOnTile.get(i);
            try {
                // Load player token image
                String playerImagePath = "/players/" + PlayerRole.getColor(player.getRole()) + ".png";
                Image playerImage = new Image(getClass().getResourceAsStream(playerImagePath));
                ImageView playerView = new ImageView(playerImage);

                // Set token size
                double tokenWidth = 73 * 0.4;
                double tokenHeight = 131 * 0.4;
                playerView.setFitWidth(tokenWidth);
                playerView.setFitHeight(tokenHeight);

                // Apply offset
                StackPane.setMargin(playerView, new Insets(
                        currentOffsets[i * 2],     // top offset
                        0,                         // right offset
                        0,                         // bottom offset
                        currentOffsets[i * 2 + 1]  // left offset
                ));

                // Add player token to the tile
                tileStack.getChildren().add(playerView);
            } catch (Exception e) {
                System.err.println("Failed to load player token image: " + e.getMessage());
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
        // Find the corresponding StackPane and add border
        for (javafx.scene.Node node : boardGrid.getChildren()) {
            if (node instanceof StackPane tilePane) {
                Integer columnIndex = GridPane.getColumnIndex(tilePane);
                Integer rowIndex = GridPane.getRowIndex(tilePane);

                if (columnIndex != null && rowIndex != null &&
                        columnIndex == tile.getPosition().getX() &&
                        rowIndex == tile.getPosition().getY()) {

                    // Remove any existing border
                    tilePane.getChildren().removeIf(child -> child instanceof Rectangle &&
                            "tile-border".equals(child.getId()));

                    // Create new border rectangle
                    Rectangle border = new Rectangle(
                            TILE_SIZE - BORDER_WIDTH,
                            TILE_SIZE - BORDER_WIDTH
                    );
                    border.setId("tile-border");
                    border.setFill(Color.TRANSPARENT);
                    border.setStroke(HIGHLIGHT_COLOR);
                    border.setStrokeWidth(BORDER_WIDTH);

                    // Ensure border is always on top
                    border.setMouseTransparent(true);
                    tilePane.getChildren().add(border);
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

        // Remove all border rectangles
        for (Node node : boardGrid.getChildren()) {
            if (node instanceof StackPane tilePane) {
                tilePane.getChildren().removeIf(child ->
                        child instanceof Rectangle && "tile-border".equals(child.getId())
                );
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
