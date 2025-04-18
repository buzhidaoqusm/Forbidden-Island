import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ListView;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

// Assuming ActionType enum or similar exists
// enum ActionType { MOVE, SHORE_UP, GIVE_CARD, CAPTURE_TREASURE, USE_ABILITY, END_TURN }
// Assuming GameController exists
// import controller.GameController;
// Assuming Player class exists
// import model.Player;


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

    // Placeholder for GameController
    // private GameController gameController;

    // Constructor
    public ActionBarView(/* GameController gameController */) {
        // this.gameController = gameController;
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
            // if (gameController != null) gameController.handleAction(ActionType.MOVE);
        });
        shoreUpButton.setOnAction(event -> {
            System.out.println("Shore Up button clicked");
            // if (gameController != null) gameController.handleAction(ActionType.SHORE_UP);
        });
        giveCardButton.setOnAction(event -> {
            System.out.println("Give Card button clicked");
            // if (gameController != null) gameController.handleAction(ActionType.GIVE_CARD);
        });
        captureTreasureButton.setOnAction(event -> {
            System.out.println("Capture Treasure button clicked");
            // if (gameController != null) gameController.handleAction(ActionType.CAPTURE_TREASURE);
        });
        useAbilityButton.setOnAction(event -> {
            System.out.println("Use Ability button clicked");
            // if (gameController != null) gameController.handleAction(ActionType.USE_ABILITY);
        });
        endTurnButton.setOnAction(event -> {
            System.out.println("End Turn button clicked");
            // if (gameController != null) gameController.handleAction(ActionType.END_TURN);
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
        javafx.application.Platform.runLater(() -> {
            actionsRemainingLabel.setText("Actions: " + actionsRemaining);
            setAvailableActions(availableActions, actionsRemaining);
        });
    }

    private void setAvailableActions(List<Object> availableActions, int actionsRemaining) {
        boolean canAct = actionsRemaining > 0;

        // Example logic: Enable button if the action type is in the list AND actions remain
        // moveButton.setDisable(!(availableActions.contains(ActionType.MOVE) && canAct));
        // shoreUpButton.setDisable(!(availableActions.contains(ActionType.SHORE_UP) && canAct));
        // giveCardButton.setDisable(!(availableActions.contains(ActionType.GIVE_CARD) && canAct));
        // captureTreasureButton.setDisable(!(availableActions.contains(ActionType.CAPTURE_TREASURE) && canAct));
        // useAbilityButton.setDisable(!(availableActions.contains(ActionType.USE_ABILITY) && canAct)); // Abilities might cost 0 actions

        // Placeholder: Enable/disable based on simple rules
        moveButton.setDisable(!canAct);
        shoreUpButton.setDisable(!canAct);
        giveCardButton.setDisable(!canAct);
        captureTreasureButton.setDisable(!canAct);
        useAbilityButton.setDisable(!canAct); // Assuming ability costs an action for now

        // End Turn is always available (unless game state prevents it)
        endTurnButton.setDisable(false);

        // Highlight active button (optional visual feedback)
        clearHighlights();
        // Example: if (gameController.isActionSelected(ActionType.MOVE)) {
        //     moveButton.setStyle("-fx-border-color: blue; -fx-border-width: 2;");
        // }
    }

    public void clearHighlights() {
        moveButton.setStyle(null);
        shoreUpButton.setStyle(null);
        giveCardButton.setStyle(null);
        captureTreasureButton.setStyle(null);
        useAbilityButton.setStyle(null);
        // endTurnButton usually doesn't need highlighting
    }

    public VBox getView() {
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
}