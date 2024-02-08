package com.example.demo1.WindowEmptyPositionFinding;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Highlighter {
    public static BufferedImage highlightAreasWithBox(BufferedImage originalImage, List<Rect> emptySpaces) {
        // Create a copy of the original image
        BufferedImage highlightedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = highlightedImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, null);

        // Set the color for highlighting the empty spaces
        g2d.setColor(Color.RED);

        // Fill the empty spaces with the specified color
        for (Rect rect : emptySpaces) {
            g2d.fillRect(rect.x, rect.y, rect.width, rect.height);
        }

        g2d.dispose();
        saveImage(highlightedImage, "highlightedWithBox.png");
        return highlightedImage;
    }

    public static void highlightAndSaveImage(BufferedImage originalImage, List<Rect> rectangles, Color highlightColor, String outputFilePath) {
        // Create a Graphics2D object to draw on the original image
        Graphics2D g2d = originalImage.createGraphics();

        // Set the highlight color
        g2d.setColor(highlightColor);

        // Iterate over each rectangle and draw it on the image
        for (Rect rect : rectangles) {
            g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
        }

        // Dispose of the Graphics2D object
        g2d.dispose();

        // Save the output image
        saveImage(originalImage, outputFilePath);
    }

    private static void saveImage(BufferedImage image, String outputFilePath) {
        try {
            File outputFile = new File(outputFilePath);
            ImageIO.write(image, "png", outputFile);
            System.out.println("Output image saved to: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
        }
    }
}

