package com.island.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.control.ListView;
import javafx.application.Platform;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

import com.island.controller.GameController;
import com.island.controller.ActionBarController;
import com.island.model.Player;
import com.island.view.ActionType;

public class ActionBarView {

    private VBox viewPane; // Root pane for this view (changed to VBox)
    private HBox actionBarControls; // HBox for the action buttons and label
    private ListView<String> logListView; // For displaying game log messages
    private VBox logPane; // Container for the log title and list view
    private Label actionsRemainingLabel;
    private Button moveButton;
    private Button shoreUpButton;
    private Button giveCardButton;
    private Button captureTreasureButton;
    private Button useAbilityButton;
    private Button endTurnButton;

    // GameController reference
    private GameController gameController;

    // Constructor
    public ActionBarView(GameController gameController) {
        this.gameController = gameController;
        initialize();
    }

    private void initialize() {
        // --- Initialize Action Bar Controls --- 
        actionBarControls = new HBox(10); // Spacing between action elements
        actionBarControls.setPadding(new Insets(5, 10, 5, 10)); // Reduced padding for controls
        actionBarControls.setAlignment(Pos.CENTER_LEFT);
        // actionBarControls.setStyle("-fx-background-color: #d3d3d3;"); // Style moved to main pane

        actionsRemainingLabel = new Label("Actions: ?");
        actionsRemainingLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        moveButton = new Button("Move");
        shoreUpButton = new Button("Shore Up");
        giveCardButton = new Button("Give Card");
        captureTreasureButton = new Button("Capture Treasure");
        useAbilityButton = new Button("Use Ability");
        endTurnButton = new Button("End Turn");

        // --- Button Actions (Send requests to Controller) ---
        moveButton.setOnAction(event -> {
            System.out.println("Move button clicked");
            if (gameController != null) gameController.getActionBarController().handleMoveAction();
        });
        shoreUpButton.setOnAction(event -> {
            System.out.println("Shore Up button clicked");
            if (gameController != null) gameController.getActionBarController().handleShoreUpAction();
        });
        giveCardButton.setOnAction(event -> {
            System.out.println("Give Card button clicked");
            if (gameController != null) gameController.getActionBarController().handleGiveCardAction();
        });
        captureTreasureButton.setOnAction(event -> {
            System.out.println("Capture Treasure button clicked");
            if (gameController != null) gameController.getActionBarController().handleCaptureTreasureAction();
        });
        useAbilityButton.setOnAction(event -> {
            System.out.println("Use Ability button clicked");
            if (gameController != null) gameController.getActionBarController().handlePlaySpecialAction();
        });
        endTurnButton.setOnAction(event -> {
            System.out.println("End Turn button clicked");
            if (gameController != null) gameController.getActionBarController().handleEndTurnAction();
        });

        actionBarControls.getChildren().addAll(
            actionsRemainingLabel,
            moveButton,
            shoreUpButton,
            giveCardButton,
            captureTreasureButton,
            useAbilityButton,
            endTurnButton
        );

        // --- Initialize Log View --- 
        logPane = new VBox(5);
        logPane.setPadding(new Insets(5, 10, 10, 10)); // Padding for log area
        // logPane.setStyle("-fx-background-color: #f5f5dc;"); // Optional: different background for log

        Label logTitle = new Label("Game Log");
        logTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        logListView = new ListView<>();
        logListView.setPrefHeight(100); // Adjust height as needed
        logListView.setMouseTransparent(true); // Make read-only
        logListView.setFocusTraversable(false);

        logPane.getChildren().addAll(logTitle, logListView);

        // --- Combine Controls and Log in Main VBox --- 
        viewPane = new VBox(5); // Main container
        viewPane.setPadding(new Insets(0)); // No padding for the main VBox itself
        viewPane.setStyle("-fx-background-color: #d3d3d3;"); // Grey background for the whole bar
        viewPane.getChildren().addAll(actionBarControls, logPane);

        // Initially disable all action buttons until updated
        setAvailableActions(new ArrayList<>(), 0);
    }

    public void updateActions(List<Object> availableActions, int actionsRemaining) {
        Platform.runLater(() -> {
            actionsRemainingLabel.setText("Actions: " + actionsRemaining);
            setAvailableActions(availableActions, actionsRemaining);
        });
    }

    private void setAvailableActions(List<Object> availableActions, int actionsRemaining) {
        boolean canAct = actionsRemaining > 0;
        
        if (gameController != null) {
            ActionBarController actionBarController = gameController.getActionBarController();
            Player currentPlayer = actionBarController.getCurrentPlayer();
            
            // Enable/disable buttons based on game state and player abilities
            moveButton.setDisable(!(canAct));
            shoreUpButton.setDisable(!(canAct && actionBarController.canShoreUpTile(currentPlayer)));
            giveCardButton.setDisable(!(canAct && actionBarController.canGiveCard(currentPlayer)));
            captureTreasureButton.setDisable(!(canAct && actionBarController.canCaptureTreasure(currentPlayer)));
            useAbilityButton.setDisable(!actionBarController.canPlaySpecialCard(currentPlayer)); // Special abilities may have different rules
            
            // End Turn is always available after drawing treasure cards
            endTurnButton.setDisable(!actionBarController.hasDrawnTreasureCards());
        } else {
            // Fallback if controller is not available
            moveButton.setDisable(!canAct);
            shoreUpButton.setDisable(!canAct);
            giveCardButton.setDisable(!canAct);
            captureTreasureButton.setDisable(!canAct);
            useAbilityButton.setDisable(!canAct);
            endTurnButton.setDisable(false);
        }

        // Highlight active button (optional visual feedback)
        clearHighlights();
    }

    public void clearHighlights() {
        moveButton.setStyle(null);
        shoreUpButton.setStyle(null);
        giveCardButton.setStyle(null);
        captureTreasureButton.setStyle(null);
        useAbilityButton.setStyle(null);
        // endTurnButton usually doesn't need highlighting
    }

    public Pane getView() {
        return viewPane;
    }

    // --- Log Methods (migrated from ActionLogView) ---
    public void addLogMessage(String logMessage) {
        Platform.runLater(() -> {
            if (logListView != null) {
                logListView.getItems().add(logMessage);
                // Auto-scroll to the latest message
                logListView.scrollTo(logListView.getItems().size() - 1);
            }
        });
    }

    public void clearLog() {
        Platform.runLater(() -> {
            if (logListView != null) {
                logListView.getItems().clear();
            }
        });
    }
    
    /**
     * Update the action bar view, get the latest action status information from GameController
     */
    public void update() {
        if (gameController != null) {
            try {
                // Get the current player's remaining action points
                int actionsRemaining = gameController.getRemainingActions();
                
                // Get available actions list
                // Since getAvailableActions method might not exist, use an empty list
                List<Object> availableActions = new ArrayList<>(); 
                
                // Update action button states
                updateActions(availableActions, actionsRemaining);
                
                // Update water level indicator if the method exists
                try {
                    int waterLevel = gameController.getIslandController().getWaterLevel();
                    updateWaterLevelIndicator(waterLevel);
                } catch (Exception e) {
                    System.err.println("Error updating water level: " + e.getMessage());
                }
                
                System.out.println("Action bar view updated");
            } catch (Exception e) {
                System.err.println("Error updating action bar view: " + e.getMessage());
            }
        }
    }
    
    /**
     * Update water level indicator
     * @param waterLevel Current water level
     */
    private void updateWaterLevelIndicator(int waterLevel) {
        // Implement water level indicator update logic
        // For example: update water level label or progress bar
        System.out.println("Current water level: " + waterLevel);
        // In actual implementation, UI elements should be updated
    }
}
