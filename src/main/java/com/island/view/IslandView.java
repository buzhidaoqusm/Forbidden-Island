import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import javafx.geometry.Pos;

import com.island.controller.GameController;
import com.island.controller.PlayerController;
import com.island.controller.IslandController;
import com.island.model.Tile;
import com.island.model.Player;
import com.island.model.Position;
 import com.island.model.Island;

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

    // Constructor
    public IslandView(GameController gameController) {
        this.gameController = gameController;
        initialize();
    }

    private void initialize() {
        gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        // gridPane.setStyle("-fx-background-color: grey;"); // Optional background

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

        viewPane = new Pane(gridPane); // Wrap gridPane in a Pane if needed for positioning
    }

    private Pane createTileRepresentation(Object tile, int row, int col) {
        Pane tilePane = new Pane();
        Rectangle background = new Rectangle(TILE_SIZE, TILE_SIZE);
        Label nameLabel = new Label("Tile " + row + "," + col); // Placeholder name

        // Set initial appearance (e.g., based on tile state)
        background.setFill(NORMAL_TILE_COLOR);
        background.setStroke(Color.BLACK);

        // Add click listener
        tilePane.setOnMouseClicked(event -> {
            System.out.println("Clicked on tile at (" + row + ", " + col + ")");
            // Pass interaction to the controller
            if (gameController != null) {
                gameController.getIslandController().handleTileClick(new Position(row, col));
            }
        });

        tilePane.getChildren().addAll(background, nameLabel);
        // Center label (basic example)
        nameLabel.setLayoutX(5);
        nameLabel.setLayoutY(5);

        return tilePane;
    }

    public void updateTileView(int row, int col, Tile tile) {
        // Find the corresponding Pane in the gridPane
        // This requires a way to map row/col back to the Node in the grid
        javafx.application.Platform.runLater(() -> {
            // Example: Get node by row/col (might need adjustment based on gridPane structure)
            javafx.scene.Node node = getNodeByRowColumnIndex(row, col, gridPane);
            if (node instanceof Pane) {
                Pane tilePane = (Pane) node;
                Rectangle background = (Rectangle) tilePane.getChildren().get(0); // Assuming rect is first
                Label nameLabel = (Label) tilePane.getChildren().get(1); // Assuming label is second

                String tileName = tile.getName();
                Tile.State state = tile.getState();
                boolean isShoredUp = tile.isShoredUp();

                nameLabel.setText(tileName);
                switch (state) {
                    case NORMAL:
                        background.setFill(NORMAL_TILE_COLOR);
                        break;
                    case FLOODED:
                        background.setFill(FLOODED_TILE_COLOR);
                        break;
                    case SUNK:
                        background.setFill(SUNK_TILE_COLOR);
                        nameLabel.setText("SUNK"); // Indicate sunk state
                        break;
                }

                if (isShoredUp && state != Tile.State.SUNK) {
                    background.setStroke(SHORED_UP_BORDER_COLOR);
                    background.setStrokeWidth(SHORED_UP_BORDER_WIDTH);
                } else {
                    background.setStroke(Color.BLACK);
                    background.setStrokeWidth(1.0);
                }

                // Placeholder update:
                background.setFill(Color.rgb((int)(Math.random()*255), (int)(Math.random()*255), (int)(Math.random()*255)));
                nameLabel.setText("Updated "+row+","+col);

            } else {
                System.err.println("Could not find Pane at row " + row + ", col " + col);
            }
        });
    }

    public void updatePlayerMarker(Object player, Object newPosition) {
        javafx.application.Platform.runLater(() -> {
            System.out.println("Updating player marker for "  + player.getName()  + " at "  + newPosition );
            
            int row = newPosition.getRow();
            int col = newPosition.getCol();
            Node node = getNodeByRowColumnIndex(row, col, gridPane);
            if (node instanceof Pane) {
                 Pane tilePane = (Pane) node;
                 javafx.scene.shape.Circle playerMarker = new javafx.scene.shape.Circle(TILE_SIZE / 4, player.getColor());
                 Remove previous markers of this player before adding
                 tilePane.getChildren().add(playerMarker);
                 Position the marker within the tile pane
                 playerMarker.setCenterX(TILE_SIZE / 2);
                 playerMarker.setCenterY(TILE_SIZE / 2);
            }
        });
    }

    public void highlightTiles(java.util.List<Object> positions, String highlightType) {
        clearHighlights(); // Clear previous highlights first
        javafx.application.Platform.runLater(() -> {
            Color highlightColor = Color.YELLOW; // Default highlight
            if ("shore_up".equals(highlightType)) {
                highlightColor = Color.LIGHTGREEN;
            }
            for (Position pos : positions) {
                Node node = getNodeByRowColumnIndex(pos.getRow(), pos.getCol(), gridPane);
                if (node instanceof Pane) {
                    Pane tilePane = (Pane) node;
                    Rectangle background = (Rectangle) tilePane.getChildren().get(0);
                    Apply highlight (e.g., change border)
                    background.setStroke(highlightColor);
                    background.setStrokeWidth(3.0);
                    System.out.println("Highlighting tile at " + pos + " for " + highlightType);
                }
            }
            System.out.println("Highlighting tiles (placeholder). Count: " + positions.size());
        });
    }

    public void clearHighlights() {
        javafx.application.Platform.runLater(() -> {
            for (javafx.scene.Node node : gridPane.getChildren()) {
                if (node instanceof Pane) {
                    Pane tilePane = (Pane) node;
                    Rectangle background = (Rectangle) tilePane.getChildren().get(0);
                    // Reset border to default (or based on shored-up status)
                    // This needs to check the actual tile state again
                    background.setStroke(Color.BLACK); // Simplified reset
                    background.setStrokeWidth(1.0);
                }
            }
            System.out.println("Cleared highlights (placeholder).");
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
                        if (player.getCurrentTile() != null) {
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

    private javafx.scene.Node getNodeByRowColumnIndex(final int row, final int column, GridPane gridPane) {
        javafx.collections.ObservableList<javafx.scene.Node> children = gridPane.getChildren();
        for (javafx.scene.Node node : children) {
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
