package com.example.demo1.WindowEmptyPositionFinding;

import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class findDarkAreas {
    public static List<Rect> findDarkRegions(BufferedImage originalImage) {
        List<Rect> darkRegions = new ArrayList<>();

        // Convert the original image to grayscale
        BufferedImage grayscaleImage = convertToGrayscale(originalImage);

        // Define the threshold for darkness (adjust as needed)
        int darknessThreshold = 100; // Assuming 0 is black and 255 is white in grayscale

        // Iterate over each pixel in the grayscale image
        for (int y = 0; y < grayscaleImage.getHeight(); y++) {
            for (int x = 0; x < grayscaleImage.getWidth(); x++) {
                // Get the RGB color of the pixel
                Color color = new Color(grayscaleImage.getRGB(x, y));

                // Calculate the brightness (luminance) of the pixel
                int brightness = (color.getRed() + color.getGreen() + color.getBlue()) / 3;

                // Check if the brightness is below the darkness threshold
                if (brightness < darknessThreshold) {
                    // If the pixel is darker than grey, expand the region to include adjacent pixels
                    Rect region = expandRegion(originalImage, x, y, darknessThreshold);
                    darkRegions.add(region);
                }
            }
        }

        return darkRegions;
    }

    private static BufferedImage convertToGrayscale(BufferedImage originalImage) {
        BufferedImage grayscaleImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayscaleImage.getGraphics();
        g.drawImage(originalImage, 0, 0, null);
        g.dispose();
        return grayscaleImage;
    }

    private static Rect expandRegion(BufferedImage image, int startX, int startY, int threshold) {
        int maxX = image.getWidth() - 1;
        int maxY = image.getHeight() - 1;
        int minX = 0;
        int minY = 0;

        // Initialize the region bounds to the starting pixel coordinates
        int minXRegion = startX;
        int maxXRegion = startX;
        int minYRegion = startY;
        int maxYRegion = startY;

        // Create a boolean mask to keep track of visited pixels
        boolean[][] visited = new boolean[image.getWidth()][image.getHeight()];
        visited[startX][startY] = true;
        // Create a queue for breadth-first search
        Queue<Point> queue = new LinkedList<>();
        queue.offer(new Point(startX, startY));

        while (!queue.isEmpty()) {
            Point currentPixel = queue.poll();

            // Check neighboring pixels in 8 directions
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int x = (int) (currentPixel.x + dx);
                    int y = (int) (currentPixel.y + dy);

                    // Check if the neighboring pixel is within the image bounds and not visited
                    if (x >= minX && x <= maxX && y >= minY && y <= maxY && !visited[x][y]) {
                        // Get the RGB color of the neighboring pixel
                        Color neighborColor = new Color(image.getRGB(x, y));

                        // Calculate the brightness (luminance) of the neighboring pixel
                        int brightness = (neighborColor.getRed() + neighborColor.getGreen() + neighborColor.getBlue()) / 3;

                        // Check if the neighboring pixel is darker than the threshold
                        if (brightness < threshold) {
                            // Expand the region to include the neighboring pixel
                            minXRegion = Math.min(minXRegion, x);
                            maxXRegion = Math.max(maxXRegion, x);
                            minYRegion = Math.min(minYRegion, y);
                            maxYRegion = Math.max(maxYRegion, y);

                            // Mark the neighboring pixel as visited
                            visited[x][y] = true;

                            // Add the neighboring pixel to the queue for further exploration
                            queue.offer(new Point(x, y));
                        }
                    }
                }
            }
        }

        // Create a rectangle representing the expanded region
        int regionWidth = maxXRegion - minXRegion + 1;
        int regionHeight = maxYRegion - minYRegion + 1;
        return new Rect(minXRegion, minYRegion, regionWidth, regionHeight);
    }

}
