package com.island.launcher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import com.island.controller.GameController;
import com.island.model.Room;
import com.island.network.RoomController;
import com.island.view.GameStart;

/**
 * Game launcher class, responsible for initializing the game controller and starting the game interface
 * Serves as the main entry point for the Forbidden Island game
 */
public class Launcher extends Application {
    
    private GameController gameController;
    private RoomController roomController;
    private Room room;
    
    /**
     * JavaFX application start method
     * @param primaryStage The primary stage
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize room
            room = new Room();
            
            // Initialize room controller
            roomController = new RoomController(room);
            
            // Initialize game controller with RoomController
            gameController = new GameController(roomController);
            
            // Create game start interface
            GameStart gameStart = new GameStart(primaryStage, gameController);
            
            // Set primary stage scene
            primaryStage.setScene(gameStart.createScene());
            primaryStage.setTitle("Forbidden Island - Welcome");
            primaryStage.setResizable(false);
            
            // Add window close event handler
            primaryStage.setOnCloseRequest((WindowEvent event) -> {
                try {
                    // Clean up resources, close network connections, etc.
                    if (gameController != null) {
                        gameController.shutdown();
                    }
                } catch (Exception e) {
                    System.err.println("Error when closing game: " + e.getMessage());
                } finally {
                    // Ensure application fully exits
                    Platform.exit();
                    System.exit(0);
                }
            });
            
            // Show primary stage
            primaryStage.show();
            
            // Log output
            System.out.println("Game started successfully");
        } catch (Exception e) {
            System.err.println("Game startup failed: " + e.getMessage());
            e.printStackTrace();
            // Ensure program exits normally even on error
            Platform.exit();
            System.exit(1);
        }
    }
    
    /**
     * Program entry method
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        try {
            // Launch JavaFX application
            launch(args);
        } catch (Exception e) {
            System.err.println("Program startup failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Get game controller instance
     * @return Game controller instance
     */
    public GameController getGameController() {
        return gameController;
    }
    
    /**
     * Get room controller instance
     * @return Room controller instance
     */
    public RoomController getRoomController() {
        return roomController;
    }
    
    /**
     * Get game room instance
     * @return Room instance
     */
    public Room getRoom() {
        return room;
    }
} 