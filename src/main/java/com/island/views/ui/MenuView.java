package com.island.views.ui;

import com.island.models.adventurers.Player;
import com.island.views.room.CreateRoomView;
import com.island.views.room.JoinRoomView;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MenuView {
    /** The list of rule images to be displayed in the rules dialog */
    private List<Image> ruleImages;
    /** The current index of the displayed rule image */
    private int currentImageIndex = 0;

    public Scene getMenuScene(Stage primaryStage, Player player) {
        // Create welcome message label
        Label welcomeLabel = new Label("Welcome, " + player.getName() + "!");

        // Set font size
        welcomeLabel.setFont(new Font("Arial", 46));  // Set font to Arial, size 46
        // Set font color
        welcomeLabel.setStyle("-fx-text-fill: #000000;");  // Set font color to black

        Button createRoomButton = new Button("Create Room");
        Button joinRoomButton = new Button("Join Room");
        Button rulesButton = new Button("Rules");

        // Button click events (navigate to corresponding scenes)
        createRoomButton.setOnAction(e -> {
            // Create room interface
            primaryStage.setScene(new CreateRoomView(primaryStage, player).getScene());
        });

        joinRoomButton.setOnAction(e -> {
            // Join room interface
            primaryStage.setScene(new JoinRoomView(primaryStage, player).getScene());
        });

        rulesButton.setOnAction(evt -> {
            loadRuleImages();
            showRulesDialog();
        });

        // Create VBox layout, set button spacing to 20 pixels
        VBox layout = new VBox(20);
        layout.setStyle("-fx-alignment: center;");  // Set horizontal alignment to center

        // Add buttons to VBox
        layout.getChildren().addAll(welcomeLabel, createRoomButton, joinRoomButton, rulesButton);

        // Create and return the scene
        return new Scene(layout, 800, 500);
    }

    private void loadRuleImages() {
        ruleImages = new ArrayList<>();
        String rulePath = "src/main/resources/rule/";

        // Load all rule images
        for (int i = 1; i <= 8; i++) {
            String filename = String.format("ForbiddenIslandTM-RULES_page-%04d.jpg", i);
            File file = new File(rulePath + filename);
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                ruleImages.add(image);
            }
        }
    }

    private void showRulesDialog() {
        Stage rulesStage = new Stage();
        rulesStage.initModality(Modality.APPLICATION_MODAL);
        rulesStage.setTitle("Game Rules");

        // Create ImageView for displaying rules
        ImageView imageView = new ImageView();
        imageView.setFitWidth(600);
        imageView.setFitHeight(800);
        imageView.setPreserveRatio(true);

        // Navigation buttons
        Button prevButton = new Button("Previous");
        Button nextButton = new Button("Next");
        Button closeButton = new Button("Close");

        // Page counter label
        Label pageLabel = new Label();

        // Update image display
        updateRuleDisplay(imageView, pageLabel);

        // Button actions
        prevButton.setOnAction(e -> {
            if (currentImageIndex > 0) {
                currentImageIndex--;
                updateRuleDisplay(imageView, pageLabel);
            }
        });

        nextButton.setOnAction(e -> {
            if (currentImageIndex < ruleImages.size() - 1) {
                currentImageIndex++;
                updateRuleDisplay(imageView, pageLabel);
            }
        });

        closeButton.setOnAction(e -> rulesStage.close());

        // Layout
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(prevButton, pageLabel, nextButton, closeButton);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(imageView, buttonBox);

        Scene scene = new Scene(layout);
        rulesStage.setScene(scene);
        rulesStage.show();
    }

    private void updateRuleDisplay(ImageView imageView, Label pageLabel) {
        if (!ruleImages.isEmpty()) {
            imageView.setImage(ruleImages.get(currentImageIndex));
            pageLabel.setText(String.format("Page %d of %d", currentImageIndex + 1, ruleImages.size()));
        }
    }
}
