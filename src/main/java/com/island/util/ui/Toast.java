package com.island.util.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * The Toast class provides non-modal, temporary notifications that automatically disappear after a short duration.
 * 
 * These notifications:
 * - Slide in from the top of the parent window
 * - Display for a configurable duration
 * - Slide out automatically
 * - Come in different styles (info, success, warning, error) with appropriate colors
 * - Are non-blocking, allowing users to continue interacting with the application
 * 
 * This implementation is inspired by mobile toast notifications and modern web notification systems.
 */
public class Toast {
    
    /**
     * Shows a toast notification with customizable parameters.
     * 
     * This core method handles the creation and animation of all toast notifications.
     * It creates a new transparent stage, positions it relative to the owner stage,
     * and applies entrance and exit animations.
     * 
     * @param ownerStage The parent window that owns this notification
     * @param message The message text to display in the notification
     * @param durationSeconds How long the notification should remain visible (in seconds)
     * @param toastType The type of notification which determines its styling
     */
    public static void show(Stage ownerStage, String message, double durationSeconds, ToastType toastType) {
        Platform.runLater(() -> {
            Stage toastStage = new Stage();
            toastStage.initOwner(ownerStage);
            toastStage.initStyle(StageStyle.TRANSPARENT); // Transparent background
            toastStage.initModality(Modality.NONE); // Non-modal, doesn't block user interaction
            toastStage.setAlwaysOnTop(true); // Always stay above other windows
            
            // Create the notification content label
            Label label = new Label(message);
            label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            
            // Create a container for the notification
            StackPane root = new StackPane(label);
            root.setPadding(new Insets(15, 25, 15, 25));
            root.setAlignment(Pos.CENTER);
            
            // Apply styles based on notification type
            switch (toastType) {
                case INFO:
                    root.setStyle("-fx-background-color: rgba(0, 120, 215, 0.9); -fx-background-radius: 5;");
                    label.setTextFill(Color.WHITE);
                    break;
                case SUCCESS:
                    root.setStyle("-fx-background-color: rgba(16, 185, 129, 0.9); -fx-background-radius: 5;");
                    label.setTextFill(Color.WHITE);
                    break;
                case WARNING:
                    root.setStyle("-fx-background-color: rgba(245, 158, 11, 0.9); -fx-background-radius: 5;");
                    label.setTextFill(Color.WHITE);
                    break;
                case ERROR:
                    root.setStyle("-fx-background-color: rgba(239, 68, 68, 0.9); -fx-background-radius: 5;");
                    label.setTextFill(Color.WHITE);
                    break;
            }
            
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT); // Transparent scene background
            toastStage.setScene(scene);
            
            // Calculate position (centered at the top of the parent window)
            toastStage.setOnShown(e -> {
                toastStage.setX(ownerStage.getX() + (ownerStage.getWidth() - toastStage.getWidth()) / 2);
                toastStage.setY(ownerStage.getY());
            });
            
            // Show the notification
            toastStage.show();
            
            // Create animation transitions for sliding in and out
            TranslateTransition slideIn = new TranslateTransition(Duration.seconds(0.3), root);
            slideIn.setFromY(-50);
            slideIn.setToY(0);
            
            TranslateTransition slideOut = new TranslateTransition(Duration.seconds(0.3), root);
            slideOut.setFromY(0);
            slideOut.setToY(-50);
            
            // Setup animation sequence
            slideIn.setOnFinished(e -> {
                // Pause for the specified duration before sliding out
                Timeline pause = new Timeline(new KeyFrame(Duration.seconds(durationSeconds)));
                pause.setOnFinished(event -> {
                    slideOut.play();
                });
                pause.play();
            });
            
            slideOut.setOnFinished(e -> toastStage.close());
            
            // Start the animation sequence
            slideIn.play();
        });
    }
    
    /**
     * Shows an informational toast notification with default duration (2.5 seconds).
     * 
     * Information toasts appear as blue panels and should be used for general, 
     * non-critical information that may be helpful to the user.
     * 
     * @param ownerStage The parent window that owns this notification
     * @param message The informational message to display
     */
    public static void info(Stage ownerStage, String message) {
        show(ownerStage, message, 2.5, ToastType.INFO);
    }
    
    /**
     * Shows a success toast notification with default duration (2.5 seconds).
     * 
     * Success toasts appear as green panels and should be used to confirm
     * the successful completion of an operation or user action.
     * 
     * @param ownerStage The parent window that owns this notification
     * @param message The success message to display
     */
    public static void success(Stage ownerStage, String message) {
        show(ownerStage, message, 2.5, ToastType.SUCCESS);
    }
    
    /**
     * Shows a warning toast notification with default duration (2.5 seconds).
     * 
     * Warning toasts appear as orange/amber panels and should be used for
     * potential issues that don't prevent the application from functioning
     * but may require user attention.
     * 
     * @param ownerStage The parent window that owns this notification
     * @param message The warning message to display
     */
    public static void warning(Stage ownerStage, String message) {
        show(ownerStage, message, 2.5, ToastType.WARNING);
    }
    
    /**
     * Shows an error toast notification with default duration (2.5 seconds).
     * 
     * Error toasts appear as red panels and should be used to notify users
     * of operation failures or issues that may affect application functionality.
     * For critical errors that require immediate action, consider using a modal dialog instead.
     * 
     * @param ownerStage The parent window that owns this notification
     * @param message The error message to display
     */
    public static void error(Stage ownerStage, String message) {
        show(ownerStage, message, 2.5, ToastType.ERROR);
    }
    
    /**
     * Enumeration of toast notification types.
     * Each type corresponds to a different visual styling and purpose.
     */
    public enum ToastType {
        /**
         * For general information messages (blue)
         */
        INFO,
        
        /**
         * For successful operation messages (green)
         */
        SUCCESS,
        
        /**
         * For warning messages (orange/amber) 
         */
        WARNING,
        
        /**
         * For error messages (red)
         */
        ERROR
    }
} 