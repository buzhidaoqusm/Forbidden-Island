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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MenuView {
    /** The list of rule images to be displayed in the rules dialog */
    private List<Image> ruleImages;
    /** The current index of the displayed rule image */
    private int currentImageIndex = 0;

    private static final double WINDOW_WIDTH = 436;
    private static final double WINDOW_HEIGHT = 600;

    public Scene getMenuScene(Stage primaryStage, Player player) {
        // Create background
        String imagePath = "/background/MenuBG.png";
        InputStream imageStream = MenuView.class.getResourceAsStream(imagePath);
        Image backgroundImage = new Image(imageStream);
        ImageView backgroundView = new ImageView(backgroundImage);
        backgroundView.setFitWidth(WINDOW_WIDTH);
        backgroundView.setFitHeight(WINDOW_HEIGHT);
        backgroundView.setPreserveRatio(false);

        // Create welcome message label
        Label welcomeLabel = new Label("Welcome, " + player.getName() + "!");

        // Set font size and style for the welcome label
        welcomeLabel.setFont(new Font("Arial", 36));  // Set font to Arial, size 46
        welcomeLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-effect: dropshadow(gaussian, black, 2, 1, 0, 0);");

        // Style the buttons
        Button createRoomButton = createStyledButton("Create Room");
        Button joinRoomButton = createStyledButton("Join Room");
        Button rulesButton = createStyledButton("Rules");

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
        VBox layout = new VBox(40);
        layout.setAlignment(Pos.CENTER);

        // Add buttons to VBox
        layout.getChildren().addAll(welcomeLabel, createRoomButton, joinRoomButton, rulesButton);

        // Create StackPane to layer background and content
        StackPane root = new StackPane();
        root.getChildren().addAll(backgroundView, layout);

        // Create and return scene
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        return scene;
    }

    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.8);" +
                        "-fx-text-fill: #000000;" +
                        "-fx-font-size: 16px;" +
                        "-fx-padding: 10 20;" +
                        "-fx-min-width: 150px;" +
                        "-fx-background-radius: 12px;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 1.0);" +
                        "-fx-text-fill: #000000;" +
                        "-fx-font-size: 16px;" +
                        "-fx-padding: 10 20;" +
                        "-fx-min-width: 150px;" +
                        "-fx-background-radius: 12px;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.8);" +
                        "-fx-text-fill: #000000;" +
                        "-fx-font-size: 16px;" +
                        "-fx-padding: 10 20;" +
                        "-fx-min-width: 150px;" +
                        "-fx-background-radius: 12px;"
        ));
        return button;
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
