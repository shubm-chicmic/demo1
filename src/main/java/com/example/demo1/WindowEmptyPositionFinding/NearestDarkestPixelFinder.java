package com.example.demo1.WindowEmptyPositionFinding;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;
import java.util.Queue;

public class NearestDarkestPixelFinder {
    public static Point findNearestDarkestPixel(Mat originalImage, Point currentLocation) {
        // Convert the original image to grayscale
        Mat grayImage = new Mat();
        if (originalImage.channels() > 1) {
            Imgproc.cvtColor(originalImage, grayImage, Imgproc.COLOR_BGR2GRAY);
        } else {
            grayImage = originalImage.clone(); // Use the original image if it's already grayscale
        }

        // Create a queue for BFS traversal
        Queue<Point> queue = new LinkedList<>();
        boolean[][] visited = new boolean[grayImage.rows()][grayImage.cols()];

        // Start BFS from the current location
        queue.add(new Point((int) currentLocation.x, (int) currentLocation.y));
        visited[(int) currentLocation.y][(int) currentLocation.x] = true;

        // Define the 4 possible directions: up, down, left, right
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        while (!queue.isEmpty()) {
            Point current = queue.poll();

            // Check if the current pixel is dark
            if (isDark(grayImage.get((int) current.y, (int) current.x)[0])) {
                return current;
            }

            // Explore adjacent pixels
            for (int i = 0; i < 4; i++) {
                int newX = (int) current.x + dx[i];
                int newY = (int) current.y + dy[i];

                // Check if the new coordinates are within the image bounds and not visited
                if (newX >= 0 && newX < grayImage.cols() && newY >= 0 && newY < grayImage.rows() && !visited[newY][newX]) {
                    queue.add(new Point(newX, newY));
                    visited[newY][newX] = true;
                }
            }
        }

        // If no dark pixel is found, return null
        return null;
    }

    private static boolean isDark(double pixelValue) {
        // Define a threshold for darkness (adjust as needed)
        int darknessThreshold = 100;
        return pixelValue < darknessThreshold;
    }
}
