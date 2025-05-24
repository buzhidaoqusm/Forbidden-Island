package com.island.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class ActionLogView {
    private static final String LOG_FILE_PATH = "game_action.log";
    private final boolean alsoPrintToConsole;

    public ActionLogView() {
        this(true); // Default simultaneous output to console
    }

    public ActionLogView(boolean alsoPrintToConsole) {
        this.alsoPrintToConsole = alsoPrintToConsole;
    }

    public synchronized void log(String message) {
        String timestampedMessage = "[" + LocalDateTime.now() + "] " + message;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE_PATH, true))) {
            writer.write(timestampedMessage);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }

        if (alsoPrintToConsole) {
            System.out.println(timestampedMessage);
        }
    }
}
