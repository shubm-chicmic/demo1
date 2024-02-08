package com.example.demo1.WindowEmptyPositionFinding;

import com.example.demo1.ImageTextAndIconAnalysis.ScreenUtils;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.awt.*;
import java.awt.image.BufferedImage;

import static com.example.demo1.WindowEmptyPositionFinding.ScreenAnalyzer.bufferedImageToMat;
import static com.example.demo1.WindowEmptyPositionFinding.ScreenAnalyzer.captureScreen;

public class WindowPositionManager {
    public static void setWindowPosition(Stage stage) {
//       Rect rectangle = ScreenAnalyzer.findEmptyArea(stage.getWidth(), stage.getHeight(), 50);
//       if(rectangle != null) {
//           System.out.println("Stage Width: " + stage.getWidth() + " Stage Height: " + stage.getHeight());
//           System.out.println(rectangle);
//           System.out.println("X: " + rectangle.x + " Y: " + rectangle.y);
//           System.out.println("Width: " + rectangle.width + " Height: " + rectangle.height);
//           stage.setX(rectangle.x);
//           stage.setY(rectangle.y);
//       }
        double currentX = stage.getX();
        double currentY = stage.getY();
        BufferedImage screenShot = null;
        try {
            screenShot = captureScreen();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }

        Mat image = bufferedImageToMat(screenShot);
        org.opencv.core.Point currentLocation = new org.opencv.core.Point(currentX, currentY);
        // Call the NearestDarkestPixelFinder method to find the nearest darkest pixel
        org.opencv.core.Point nearestDarkPixel = NearestDarkestPixelFinder.findNearestDarkestPixel(image, currentLocation);

        // Check if a nearest darkest pixel is found
        if (nearestDarkPixel != null) {
            // Set the stage position using the coordinates of the nearest darkest pixel
            stage.setX(nearestDarkPixel.x);
            stage.setY(nearestDarkPixel.y);
        }
    }

    public static void monitorAndAdjustWindowPosition(Stage stage) {
        // Periodically monitor the screen and adjust the window position
        Thread monitorThread = new Thread(() -> {
            while (true) {
                try {
                    // Check for
                    System.out.println("Monitoring screen");
                    // changes in the screen layout every few seconds
                    Thread.sleep(3000); // Adjust the interval as needed
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
