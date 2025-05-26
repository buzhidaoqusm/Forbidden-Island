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
        // Initialize log display area
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


        // Set scrollbar
        this.setPadding(new Insets(5));
        this.setSpacing(5);
        this.getChildren().add(logTextArea);

    }

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

        appendToLogArea(timestampedMessage, "#ff4444"); // red
        if (alsoPrintToConsole) {
            System.err.println(timestampedMessage);
        }
    }

    public void success(String message) {
        String timestampedMessage = "[SUCCESS] [" + LocalDateTime.now() + "] " + message;
        writeToLogFile(timestampedMessage);

        appendToLogArea(timestampedMessage, "#00C851"); // green
        if (alsoPrintToConsole) {
            System.out.println(timestampedMessage);
        }
    }

    private void appendToLogArea(String message, String color) {
        Platform.runLater(() -> {
            logTextArea.appendText(message + "\n");
            // Apply color to the latest line (requires HTML format support)
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
