package com.island.view;

import com.island.network.RoomController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import com.island.controller.GameController;
import com.island.model.Player;
import com.island.model.Explorer;
import com.island.view.GameView;
import java.util.UUID;

public class CreateRoomView {

    private Stage primaryStage;
    // Placeholder for Player object (host)
    // private Player hostPlayer;
    // GameController reference
    private GameController gameController;

    private ListView<String> playerListView;
    private ComboBox<String> difficultyComboBox;
    
    // Image resources
    private Image roomBackgroundImage;
    private Image roomTitleImage;

    private RoomController roomController;

    // Constructor (potentially needs Player and GameController)
    public CreateRoomView(Stage primaryStage /*, Player hostPlayer */) {
        this.primaryStage = primaryStage;
        // this.hostPlayer = hostPlayer;
        this.gameController = null;
        this.playerListView = new ListView<>();
        this.difficultyComboBox = new ComboBox<>();
        loadImages();
    }
    
    // Constructor with GameController
    public CreateRoomView(Stage primaryStage, GameController gameController /*, Player hostPlayer */) {
        this.primaryStage = primaryStage;
        this.gameController = gameController;
        // this.hostPlayer = hostPlayer;
        this.playerListView = new ListView<>();
        this.difficultyComboBox = new ComboBox<>();
        loadImages();
    }
    
    /**
     * Load room-related images
     */
    private void loadImages() {
        try {
            // Load room background image
            roomBackgroundImage = new Image(getClass().getResourceAsStream("/image/UI/room_background.jpg"));
            // Load room title image
            roomTitleImage = new Image(getClass().getResourceAsStream("/image/UI/create_room_title.png"));
        } catch (Exception e) {
            System.err.println("Room image resources loading failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Scene createScene() {
        StackPane root = new StackPane();
        
        // Add background image
        if (roomBackgroundImage != null) {
            ImageView backgroundImageView = new ImageView(roomBackgroundImage);
            backgroundImageView.setFitWidth(600);
            backgroundImageView.setFitHeight(400);
            backgroundImageView.setPreserveRatio(true);
            root.getChildren().add(backgroundImageView);
        }
        
        // Add content panel
        BorderPane contentPane = createContentPane();
        root.getChildren().add(contentPane);
        
        return new Scene(root, 600, 400);
    }
    
    private BorderPane createContentPane() {
        BorderPane contentPane = new BorderPane();
        contentPane.setPadding(new Insets(20));
        
        // Room title or image
        if (roomTitleImage != null) {
            ImageView titleImageView = new ImageView(roomTitleImage);
            titleImageView.setFitWidth(200);
            titleImageView.setFitHeight(50);
            titleImageView.setPreserveRatio(true);
            HBox titleBox = new HBox(titleImageView);
            titleBox.setAlignment(Pos.CENTER);
            contentPane.setTop(titleBox);
            BorderPane.setMargin(titleBox, new Insets(0, 0, 15, 0));
        } else {
            Label titleLabel = new Label("Create Room");
            titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
            titleLabel.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.7), 3, 0, 0, 0);");
            HBox titleBox = new HBox(titleLabel);
            titleBox.setAlignment(Pos.CENTER);
            contentPane.setTop(titleBox);
            BorderPane.setMargin(titleBox, new Insets(0, 0, 15, 0));
        }

        // Center: Player List and Settings
        VBox centerBox = new VBox(15);
        centerBox.setAlignment(Pos.CENTER);

        Label playerListLabel = new Label("Players in Room:");
        playerListLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        // Initialize player list (e.g., add the host)
        // if (hostPlayer != null) {
        //     playerListView.getItems().add(hostPlayer.getName() + " (Host)");
        // }
        playerListView.setPrefHeight(150);
        playerListView.setStyle("-fx-control-inner-background: rgba(255, 255, 255, 0.7);");

        Label difficultyLabel = new Label("Select Difficulty:");
        difficultyLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        difficultyComboBox.getItems().addAll("Novice", "Normal", "Elite", "Legendary");
        difficultyComboBox.setValue("Normal"); // Default difficulty

        centerBox.getChildren().addAll(playerListLabel, playerListView, difficultyLabel, difficultyComboBox);
        contentPane.setCenter(centerBox);

        // Bottom: Buttons
        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);

        Button returnButton = new Button("Return");
        returnButton.setStyle("-fx-font-size: 14px; -fx-background-radius: 10;");
        returnButton.setOnAction(event -> {
            System.out.println("Return button clicked.");
            // Transition back to MenuView
            if (gameController != null) {
                // This method is not yet implemented in GameController
                // gameController.showMainMenu();
                // Using direct transition for now
                System.out.println("Returning to Main Menu...");
                MenuView menuView = new MenuView(primaryStage);
                primaryStage.setScene(menuView.createScene());
                primaryStage.setTitle("Forbidden Island - Main Menu");
            } else {
                System.out.println("Returning to Main Menu...");
                MenuView menuView = new MenuView(primaryStage);
                primaryStage.setScene(menuView.createScene());
                primaryStage.setTitle("Forbidden Island - Main Menu");
            }
        });

        Button startGameButton = new Button("Start Game");
        startGameButton.setStyle("-fx-font-size: 14px; -fx-background-radius: 10;");
        startGameButton.setOnAction(event -> {
//            String roomId = getView().getId(); // 生成唯一房间ID（如时间戳+随机数）
            String selectedDifficulty = difficultyComboBox.getValue();
//            gameController.createRoom(roomId, selectedDifficulty);
            System.out.println("Start Game button clicked. Difficulty: " + selectedDifficulty);
            // Validate if enough players have joined (usually handled by controller)
            // Notify the controller to start the game
            // 启动广播
//            roomController.startBroadcast(roomId);
            if (gameController != null) {
                // 添加调试信息，检查 gameController 的状态
                System.out.println("GameController: " + gameController);
                System.out.println("RoomController: " + gameController.getRoomController());
                System.out.println("Room: " + gameController.getRoom());
                if (gameController.getRoom() != null) {
                    System.out.println("Players in Room: " + gameController.getRoom().getPlayers());
                }
                System.out.println("IslandController: " + gameController.getIslandController());
                System.out.println("PlayerController: " + gameController.getPlayerController());
                
                try {
                    // 确保有玩家在房间里
                    if (gameController.getRoom() != null && 
                        (gameController.getRoom().getPlayers() == null || gameController.getRoom().getPlayers().isEmpty())) {
                        System.out.println("Adding test player to room...");
                        // 添加一个测试玩家
                        Player testPlayer = new Explorer("TestPlayer");
                        gameController.getRoom().addPlayer(testPlayer);
                    }
                    
                    // 创建 GameView 如果它为 null
                    if (gameController.getGameView() == null) {
                        System.out.println("Creating GameView...");
                        GameView gameView = new GameView(primaryStage, gameController);
                        gameController.setGameView(gameView);
                    }
                    
                    // The controller's startGame method exists, but takes a seed parameter
                    // that we need to pass directly
                    System.out.println("Starting game with seed: " + System.currentTimeMillis());
                    gameController.startGame(System.currentTimeMillis());  // Use current time as seed
                } catch (Exception e) {
                    System.err.println("Error starting game: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Starting game setup...");
                // Fallback when controller is not available
            }
            // Transition to GameView (handled by controller after setup)
        });

        bottomBox.getChildren().addAll(returnButton, startGameButton);
        contentPane.setBottom(bottomBox);
        BorderPane.setMargin(bottomBox, new Insets(15, 0, 0, 0));

        return contentPane;
    }

    /**
     * Updates the list of players displayed in the view.
     * Called by the controller when a new player joins.
     * @param playerName The name of the player who joined.
     */
    public void addPlayerToList(String playerName) {
        // Ensure UI updates are run on the JavaFX Application Thread
        Platform.runLater(() -> {
            playerListView.getItems().add(playerName);
        });
    }

    /**
     * Updates the list of players displayed in the view.
     * Called by the controller when a player leaves.
     * @param playerName The name of the player who left.
     */
    public void removePlayerFromList(String playerName) {
        Platform.runLater(() -> {
            playerListView.getItems().remove(playerName);
            // Potentially remove host tag if needed
            playerListView.getItems().remove(playerName + " (Host)");
        });
    }

    // Getters for UI elements if needed by controller
    public ListView<String> getPlayerListView() {
        return playerListView;
    }

    public ComboBox<String> getDifficultyComboBox() {
        return difficultyComboBox;
    }
    
    /**
     * Get the root node of the view
     * @return The root node of the view
     */
    public Pane getView() {
        return createContentPane();
    }
    
    /**
     * Update the view
     * Implements Observer pattern, updates the interface when game state changes
     */
    public void update() {
        if (gameController != null) {
            // Update player list
            Platform.runLater(() -> {
                playerListView.getItems().clear();
                // Add players from the game controller
                // These methods are not yet implemented in GameController
                // gameController.getPlayerList().forEach(player -> {
                //     playerListView.getItems().add(player.getName());
                // });
                System.out.println("CreateRoomView updated - would display player list here");
            });
        }
    }

    private void handleCreateRoom() {
        if (gameController != null) {
            try {
                // 生成唯一的房间ID
                String roomId = UUID.randomUUID().toString();
                
                // 设置房间ID
                if (gameController.getRoom() != null) {
                    gameController.getRoom().setRoomId(roomId);
                    
                    // 更新玩家列表显示
                    Platform.runLater(() -> {
                        playerListView.getItems().clear();
                        for (Player player : gameController.getRoom().getPlayers()) {
                            String displayName = player.getName();
                            if (gameController.getRoom().isHost(player.getName())) {
                                displayName += " (房主)";
                            }
                            playerListView.getItems().add(displayName);
                        }
                    });
                    
                    // 显示房间ID
                    Platform.runLater(() -> {
                        Label roomIdLabel = new Label("房间ID: " + roomId);
                        roomIdLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                        // 将房间ID标签添加到界面上
                        // 这里需要根据实际布局调整添加位置
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("创建房间失败");
                    alert.setHeaderText(null);
                    alert.setContentText("创建房间时发生错误: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }
    }
}
