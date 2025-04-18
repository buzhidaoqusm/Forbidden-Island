import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// import model.Player;
// import view.CreateRoomView;
// import view.JoinRoomView;

public class MenuView {

    private Stage primaryStage;
    // Placeholder for Player object - uncomment and use if needed
    // private Player player;

    // Constructor without Player
    public MenuView(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    // Constructor with Player (if needed)
    /*
    public MenuView(Stage primaryStage, Player player) {
        this.primaryStage = primaryStage;
        this.player = player;
    }
    */

    public Scene createScene() {
        VBox root = new VBox(15); // Spacing between buttons
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(25));

        Button createRoomButton = new Button("Create Room");
        createRoomButton.setPrefWidth(150);
        createRoomButton.setOnAction(event -> {
            System.out.println("Create Room button clicked.");
            // Transition to CreateRoomView
            // CreateRoomView createRoomView = new CreateRoomView(primaryStage, player);
            // primaryStage.setScene(createRoomView.createScene());
            // primaryStage.setTitle("Forbidden Island - Create Room");
            // Placeholder action:
            System.out.println("Transitioning to Create Room view...");
            // Replace with actual transition logic, potentially involving a controller
            // e.g., gameController.showCreateRoomView();
        });

        Button joinRoomButton = new Button("Join Room");
        joinRoomButton.setPrefWidth(150);
        joinRoomButton.setOnAction(event -> {
            System.out.println("Join Room button clicked.");
            // Transition to JoinRoomView
            // JoinRoomView joinRoomView = new JoinRoomView(primaryStage, player);
            // primaryStage.setScene(joinRoomView.createScene());
            // primaryStage.setTitle("Forbidden Island - Join Room");
            // Placeholder action:
            System.out.println("Transitioning to Join Room view...");
            // Replace with actual transition logic, potentially involving a controller
            // e.g., gameController.showJoinRoomView();
        });

        Button exitButton = new Button("Exit Game");
        exitButton.setPrefWidth(150);
        exitButton.setOnAction(event -> {
            System.out.println("Exit Game button clicked.");
            Platform.exit(); // Closes the JavaFX application
        });

        root.getChildren().addAll(createRoomButton, joinRoomButton, exitButton);

        return new Scene(root, 350, 250);
    }

    // Getters for buttons if needed for external event handling (e.g., by a controller)
    /*
    public Button getCreateRoomButton() { return createRoomButton; }
    public Button getJoinRoomButton() { return joinRoomButton; }
    public Button getExitButton() { return exitButton; }
    */
}
