package com.island.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.layout.VBox;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class ActionLogView extends VBox {
    private static final String LOG_FILE_PATH = "game_action.log";
    private final boolean alsoPrintToConsole;
    private TextArea logTextArea;
    private int logAreaHeight = 120;
    private int logAreaWidth = 230;

    public ActionLogView() {
        this(true); // Default simultaneous output to console
    }

    public ActionLogView(boolean alsoPrintToConsole) {
        this.alsoPrintToConsole = alsoPrintToConsole;
        initializeLogView();
    }

    private void initializeLogView() {
        // 初始化日志显示区域
        logTextArea = new TextArea();
        logTextArea.setEditable(false);
        logTextArea.setBackground(new Background(new BackgroundFill(
                Color.rgb(30, 30, 30, 0.8),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));
        logTextArea.setStyle("""
            -fx-font-family: 'Monospaced';
            -fx-font-size: 12px;
            -fx-text-fill: white;
            """);


        // 设置滚动条
        this.setPadding(new Insets(5));
        this.setSpacing(5);
        this.getChildren().add(logTextArea);

    }


//        // 设置滚动条
//        VBox container = new VBox(logTextArea);
//        container.setPadding(new Insets(5));
//        container.setBackground(new Background(new BackgroundFill(
//                Color.rgb(10, 10, 10),
//                CornerRadii.EMPTY,
//                Insets.EMPTY
//        )));
//
//        getChildren().add(container);
//    public ObservableList<Node> getChildren() {
//        return java.util.List.of();
//    }

    public synchronized void log(String message) {
        String timestampedMessage = "[" + LocalDateTime.now() + "] " + message;
        writeToLogFile(timestampedMessage);

        appendToLogArea(timestampedMessage, "white");
        if (alsoPrintToConsole) {
            System.out.println(timestampedMessage);
        }

    }

    public void error(String message) {
        String timestampedMessage = "[ERROR] [" + LocalDateTime.now() + "] " + message;
        writeToLogFile(timestampedMessage);

        appendToLogArea(timestampedMessage, "#ff4444"); // 红色
        if (alsoPrintToConsole) {
            System.err.println(timestampedMessage);
        }
    }

    public void success(String message) {
        String timestampedMessage = "[SUCCESS] [" + LocalDateTime.now() + "] " + message;
        writeToLogFile(timestampedMessage);

        appendToLogArea(timestampedMessage, "#00C851"); // 绿色
        if (alsoPrintToConsole) {
            System.out.println(timestampedMessage);
        }
    }

    private void appendToLogArea(String message, String color) {
        Platform.runLater(() -> {
            logTextArea.appendText(message + "\n");
            // 应用颜色到最新行（需要HTML格式支持）
            logTextArea.setStyle(
                    " -fx-text-fill: " + color + ";"
            );
        });
    }

    public void setLogAreaHeight(int height) {
        this.logAreaHeight = height;
        logTextArea.setPrefHeight(height - 10);
    }

    public void setLogAreaWidth(int width) {
        this.logAreaWidth = width;
        logTextArea.setPrefWidth(width - 10);
    }

    private void writeToLogFile(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE_PATH, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    public VBox getView() {
        return this;
    }
}
