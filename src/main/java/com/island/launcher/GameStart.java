package com.island.launcher;

import com.island.models.adventurers.Player;
import com.island.views.ui.MenuView;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.InputStream;

public class GameStart extends Application {
    private MenuView menuView;
    @Override
    public void start(Stage primaryStage) {
        // Load application icon once
        InputStream iconStream = getClass().getResourceAsStream("/icon/icon.png");
        Image applicationIcon = new Image(iconStream);
        primaryStage.getIcons().add(applicationIcon);

        // Create text input dialog
        TextInputDialog dialog = new TextInputDialog("Username");
        dialog.setTitle("Enter your name");
        dialog.setHeaderText("Please enter your name.");

        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(applicationIcon);

        // Process username when dialog is hidden
        dialog.setOnHiding(evt -> {
            String result = dialog.getResult();
            if (result != null && !result.trim().isEmpty()) {
                Player player = new Player(result);
                // After username input is complete, load the main menu
                loadMainMenu(primaryStage, player);
            } else {
                // If no username is entered, still require input
                dialog.show();
            }
        });

        // Add window close handler
        primaryStage.setOnCloseRequest(event -> {
            // Ensure clean shutdown
            System.out.println("Application closed by user.");
            if (menuView != null) {
                menuView.shutdown();
            }
            Platform.exit();
        });

        // Show input dialog
        dialog.show();
    }

    // Method to load the main menu
    private void loadMainMenu(Stage primaryStage, Player player) {
        // Initialize view
        menuView = new MenuView();
        // Set scene and display
        primaryStage.setScene(menuView.getMenuScene(primaryStage, player));
        primaryStage.setTitle("Forbidden Island");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
