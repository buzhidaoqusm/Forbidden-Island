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
            
            // 更安全的初始化顺序
            // 1. 先创建GameController，不传入RoomController
            gameController = new GameController(null);
            
            // 2. 创建RoomController并将GameController传入
            roomController = new RoomController(gameController, room);
            roomController.start();
            
            // 3. 将RoomController设置到GameController
            gameController.setRoomController(roomController);
            
            // 4. 重新获取Room对象并设置到相关控制器
            room = roomController.getRoom();
            
            // 创建游戏启动界面
            GameStart gameStart = new GameStart(primaryStage, gameController);
            
            // 设置主舞台场景
            primaryStage.setScene(gameStart.createScene());
            primaryStage.setTitle("禁闭岛 - 欢迎");
            primaryStage.setResizable(false);
            
            // 添加窗口关闭事件处理
            primaryStage.setOnCloseRequest((WindowEvent event) -> {
                try {
                    // 清理资源、关闭网络连接等
                    if (gameController != null) {
                        gameController.shutdown();
                    }
                } catch (Exception e) {
                    System.err.println("关闭游戏时出错: " + e.getMessage());
                } finally {
                    // 确保应用完全退出
                    Platform.exit();
                    System.exit(0);
                }
            });
            
            // 显示主舞台
            primaryStage.show();
            
            // 日志输出
            System.out.println("游戏成功启动");
        } catch (Exception e) {
            System.err.println("游戏启动失败: " + e.getMessage());
            e.printStackTrace();
            // 即使出错也确保程序正常退出
            Platform.exit();
            System.exit(1);
        }
    }
    
    /**
     * 程序入口方法
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            // 启动JavaFX应用
            launch(args);
        } catch (Exception e) {
            System.err.println("程序启动失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * 获取游戏控制器实例
     * @return 游戏控制器实例
     */
    public GameController getGameController() {
        return gameController;
    }
    
    /**
     * 获取房间控制器实例
     * @return 房间控制器实例
     */
    public RoomController getRoomController() {
        return roomController;
    }
    
    /**
     * 获取游戏房间实例
     * @return 房间实例
     */
    public Room getRoom() {
        return room;
    }
} 