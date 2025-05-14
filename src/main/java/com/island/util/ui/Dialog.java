package com.island.util.ui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * The Dialog class provides utility methods for displaying various types of notifications and alerts to the user.
 * 
 * This class offers a centralized way to show different kinds of user interface dialogs:
 * - Standard modal message dialogs
 * - Non-modal toast notifications with different styling based on message type (info, success, warning, error)
 * 
 * All dialog methods are static, allowing them to be easily called from anywhere in the application
 * without needing to instantiate the Dialog class.
 */
public class Dialog {

    /**
     * Displays a modal information dialog with the specified title and content.
     * The dialog blocks user interaction with the application until dismissed.
     * 
     * @param title The title text to display in the dialog window's title bar
     * @param content The main message content to display in the dialog body
     */
    public static void showMessage(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    
    /**
     * Displays an informational toast notification.
     * The notification appears as a blue panel that slides in from the top of the specified stage,
     * remains visible briefly, then slides out automatically.
     * 
     * @param stage The parent stage over which the toast should appear
     * @param message The message to display in the toast notification
     */
    public static void showToast(Stage stage, String message) {
        Toast.info(stage, message);
    }
    
    /**
     * Displays a success toast notification.
     * The notification appears as a green panel that slides in from the top of the specified stage,
     * remains visible briefly, then slides out automatically.
     * Use this for positive confirmations of user actions.
     * 
     * @param stage The parent stage over which the toast should appear
     * @param message The success message to display in the toast notification
     */
    public static void showSuccessToast(Stage stage, String message) {
        Toast.success(stage, message);
    }
    
    /**
     * Displays a warning toast notification.
     * The notification appears as an orange/amber panel that slides in from the top of the specified stage,
     * remains visible briefly, then slides out automatically.
     * Use this for non-critical warnings that don't require immediate user action.
     * 
     * @param stage The parent stage over which the toast should appear
     * @param message The warning message to display in the toast notification
     */
    public static void showWarningToast(Stage stage, String message) {
        Toast.warning(stage, message);
    }
    
    /**
     * Displays an error toast notification.
     * The notification appears as a red panel that slides in from the top of the specified stage,
     * remains visible briefly, then slides out automatically.
     * Use this for error messages that should be brought to the user's attention
     * but don't require immediate action.
     * 
     * @param stage The parent stage over which the toast should appear
     * @param message The error message to display in the toast notification
     */
    public static void showErrorToast(Stage stage, String message) {
        Toast.error(stage, message);
    }
}
