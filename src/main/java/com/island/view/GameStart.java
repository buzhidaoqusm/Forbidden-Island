import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// Assuming Player class exists in a model package
// import model.Player;

/**
 * GameStart: Responsible for implementing the game startup interface, including user name
 * input. Handles the first step of the player's interaction when entering the game,
 * creates a Player object after the player enters the user name, and transitions to the
 * main menu interface.
 */
public class GameStart {

    private Stage primaryStage;
    // Placeholder for Player class - replace with actual import
    // private Player player;

    public GameStart(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public Scene createScene() {
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Label nameLabel = new Label("Enter your username:");
        TextField nameInput = new TextField();
        nameInput.setPromptText("Username");
        nameInput.setMaxWidth(200);

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(event -> {
            String username = nameInput.getText().trim();
            if (!username.isEmpty()) {
                // Create Player object (assuming Player constructor takes username)
                player = new Player(username);
                System.out.println("Player created: " + username);
                MainApplication.showMenuView(primaryStage, player);
                // Or: gameController.showMainMenu(player);

                // Placeholder transition - replace with actual logic
                MenuView menuView = new MenuView(primaryStage /*, player */); // Pass player if needed
                primaryStage.setScene(menuView.createScene());
                primaryStage.setTitle("Forbidden Island - Main Menu");

            } else {
                // Handle empty username case (e.g., show an error message)
                System.out.println("Username cannot be empty.");
                // You might want to show an alert dialog here
            }
        });

        root.getChildren().addAll(nameLabel, nameInput, submitButton);

        return new Scene(root, 300, 200);
    }

    public static class MainApplication extends Application {
        @Override
        public void start(Stage primaryStage) {
            GameStart gameStart = new GameStart(primaryStage);
            primaryStage.setScene(gameStart.createScene());
            primaryStage.setTitle("Forbidden Island - Welcome");
            primaryStage.show();
        }

        public static void main(String[] args) {
            launch(args);
        }

        // Method to transition to MenuView (called from GameStart)
        public static void showMenuView(Stage stage, Player player) {
             MenuView menuView = new MenuView(stage, player);
             stage.setScene(menuView.createScene());
             stage.setTitle("Forbidden Island - Main Menu");
        }
    }
    
}
