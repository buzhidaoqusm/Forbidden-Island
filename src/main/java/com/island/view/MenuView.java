import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// import model.Player;
// import view.CreateRoomView;
// import view.JoinRoomView;
import com.island.controller.GameController;

public class MenuView {

    private Stage primaryStage;private Player player;
    
    // GameController reference
    private GameController gameController;

    // Constructor without Player
    public MenuView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.gameController = null;
    }
    
    // Constructor with GameController
    public MenuView(Stage primaryStage, GameController gameController) {
        this.primaryStage = primaryStage;
        this.gameController = gameController;
    }


    public MenuView(Stage primaryStage, Player player) {
        this.primaryStage = primaryStage;
        this.player = player;
    }
    
    public Scene createScene() {
        VBox root = new VBox(15); // Spacing between buttons
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(25));

        Button createRoomButton = new Button("Create Room");
        createRoomButton.setPrefWidth(150);
        createRoomButton.setOnAction(event -> {
            System.out.println("Create Room button clicked.");
            // Transition to CreateRoomView
            if (gameController != null) {
                gameController.showCreateRoomView();
            } else {
                System.out.println("Transitioning to Create Room view...");
                CreateRoomView createRoomView = new CreateRoomView(primaryStage);
                primaryStage.setScene(createRoomView.createScene());
                primaryStage.setTitle("Forbidden Island - Create Room");
            }
        });

        Button joinRoomButton = new Button("Join Room");
        joinRoomButton.setPrefWidth(150);
        joinRoomButton.setOnAction(event -> {
            System.out.println("Join Room button clicked.");
            // Transition to JoinRoomView
            if (gameController != null) {
                gameController.showJoinRoomView();
            } else {
                System.out.println("Transitioning to Join Room view...");
                JoinRoomView joinRoomView = new JoinRoomView(primaryStage);
                primaryStage.setScene(joinRoomView.createScene());
                primaryStage.setTitle("Forbidden Island - Join Room");
            }
        });}

        Button exitButton = new Button("Exit Game");
        exitButton.setPrefWidth(150);
        exitButton.setOnAction(event -> {
            System.out.println("Exit Game button clicked.");
            Platform.exit(); // Closes the JavaFX application
        });

        root.getChildren().addAll(createRoomButton, joinRoomButton, exitButton);

        return new Scene(root, 350, 250);
    }


    public Button getCreateRoomButton() { return createRoomButton; }
    public Button getJoinRoomButton() { return joinRoomButton; }
    public Button getExitButton() { return exitButton; }
    
    
    /**
     * Update the view
     * Implements Observer pattern, updates the interface when game state changes
     */
    public void update() {
        if (gameController != null) {
            // In menu view, we may need to update some status information
            // For example: player online status, available room list, etc.
            System.out.println("MenuView updated");
            // In actual implementation, UI elements should be updated based on game controller state
        }
    }
    
    /**
     * Get the root node of the view
     * @return The root node of the view
     */
    public Pane getView() {
        VBox root = new VBox(15); // Spacing between buttons
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(25));

        Button createRoomButton = new Button("Create Room");
        createRoomButton.setPrefWidth(150);
        createRoomButton.setOnAction(event -> {
            System.out.println("Create Room button clicked.");
            if (gameController != null) {
                gameController.showCreateRoomView();
            } else {
                System.out.println("Transitioning to Create Room view...");
                CreateRoomView createRoomView = new CreateRoomView(primaryStage);
                primaryStage.setScene(createRoomView.createScene());
                primaryStage.setTitle("Forbidden Island - Create Room");
            }
        });

        Button joinRoomButton = new Button("Join Room");
        joinRoomButton.setPrefWidth(150);
        joinRoomButton.setOnAction(event -> {
            System.out.println("Join Room button clicked.");
            if (gameController != null) {
                gameController.showJoinRoomView();
            } else {
                System.out.println("Transitioning to Join Room view...");
                JoinRoomView joinRoomView = new JoinRoomView(primaryStage);
                primaryStage.setScene(joinRoomView.createScene());
                primaryStage.setTitle("Forbidden Island - Join Room");
            }
        });

        Button exitButton = new Button("Exit Game");
        exitButton.setPrefWidth(150);
        exitButton.setOnAction(event -> {
            System.out.println("Exit Game button clicked.");
            Platform.exit(); // Closes the JavaFX application
        });

        root.getChildren().addAll(createRoomButton, joinRoomButton, exitButton);
        
        return root;
    }
}
