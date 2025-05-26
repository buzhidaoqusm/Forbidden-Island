package com.island.view;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ActionLogView {
    private static final String LOG_FILE_PATH = "game_action.log";
    private final boolean alsoPrintToConsole;
    
    // UI Components
    private VBox logViewPane;
    private TextArea logArea;
    private Label titleLabel;
    private DateTimeFormatter timeFormatter;

    public ActionLogView() {
        this(true); // Default simultaneous output to console
    }

    public ActionLogView(boolean alsoPrintToConsole) {
        this.alsoPrintToConsole = alsoPrintToConsole;
        this.timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        initializeView();
    }
    
    private void initializeView() {
        // 创建日志视图面板
        logViewPane = new VBox(5);
        logViewPane.setPadding(new Insets(5));
        
        // 创建标题标签
        titleLabel = new Label("游戏日志");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        // 创建日志文本区域
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(150);
        logArea.setPrefWidth(200);
        
        // 添加组件到面板
        logViewPane.getChildren().addAll(titleLabel, logArea);
    }

    public synchronized void log(String message) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String timestampedMessage = "[" + timestamp + "] " + message;

        // 写入文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE_PATH, true))) {
            writer.write(timestampedMessage);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }

        // 输出到控制台
        if (alsoPrintToConsole) {
            System.out.println(timestampedMessage);
        }
        
        // 更新UI
        appendToLogArea(timestampedMessage, null);
    }
    
    public void success(String message) {
        log("✓ " + message);
        appendToLogArea("✓ " + message, Color.GREEN);
    }
    
    public void warning(String message) {
        log("⚠ " + message);
        appendToLogArea("⚠ " + message, Color.ORANGE);
    }
    
    public void error(String message) {
        log("✗ " + message);
        appendToLogArea("✗ " + message, Color.RED);
    }
    
    private void appendToLogArea(String message, Color color) {
        if (logArea != null) {
            Platform.runLater(() -> {
                if (color != null) {
                    // 添加带颜色的文本
                    logArea.appendText(message + "\n");
                } else {
                    // 添加普通文本
                    logArea.appendText(message + "\n");
                }
                
                // 滚动到底部
                logArea.setScrollTop(Double.MAX_VALUE);
            });
        }
    }
    
    public void setLogAreaHeight(double height) {
        if (logArea != null) {
            logArea.setPrefHeight(height);
        }
    }
    
    public void setLogAreaWidth(double width) {
        if (logArea != null) {
            logArea.setPrefWidth(width);
        }
    }
    
    public VBox getView() {
        return logViewPane;
    }
}
