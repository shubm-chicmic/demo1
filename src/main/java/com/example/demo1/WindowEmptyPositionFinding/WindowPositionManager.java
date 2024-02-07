package com.example.demo1.WindowEmptyPositionFinding;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class WindowPositionManager {
    public static void setWindowPosition(Stage stage) {
        // Get the bounds of the primary screen
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();

        // Calculate the available empty space on the screen
        double availableX = screenBounds.getWidth() - stage.getWidth();
        double availableY = screenBounds.getHeight() - stage.getHeight();

        // Set the position of the stage to occupy the available empty space
        double newX = screenBounds.getMinX() + availableX / 2;
        double newY = screenBounds.getMinY() + availableY / 2;
        stage.setX(newX);
        stage.setY(newY);
        System.out.println("New Position: " + newX + " " + newY);
    }

    public static void monitorAndAdjustWindowPosition(Stage stage) {
        // Periodically monitor the screen and adjust the window position
        Thread monitorThread = new Thread(() -> {
            while (true) {
                try {
                    // Check for changes in the screen layout every few seconds
                    Thread.sleep(5000); // Adjust the interval as needed
                    System.out.println("Monitoring screen");
                    // Set the window position
                    setWindowPosition(stage);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
    }
}
