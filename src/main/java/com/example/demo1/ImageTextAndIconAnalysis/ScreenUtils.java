package com.example.demo1.ImageTextAndIconAnalysis;

import javafx.util.Pair;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.videoio.VideoCapture;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ScreenUtils {

    static {
        // Load OpenCV native library dynamically
        OpenCV.loadShared();
    }

    public static Rectangle findEmptySpace(int appWidth, int appHeight) {
        // Capture screen using AWT Robot
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage screenImage = null;
        try {
            screenImage = new Robot().createScreenCapture(screenRect);
            File outputFile = new File("captured_screen.png");
            ImageIO.write(screenImage, "png", outputFile);

            System.out.println("Screen captured and saved to: " + outputFile.getAbsolutePath());
        } catch (AWTException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Convert BufferedImage to Mat
        Mat frame = bufferedImageToMat(screenImage);

        // Convert image to grayscale
        Mat grayscaleImage = convertToGrayscale(frame);

        // Apply thresholding
        Mat thresholdedImage = applyThreshold(grayscaleImage);

        // Find contours
        List<MatOfPoint> contours = findContours(thresholdedImage);

        // Filter out small contours
        contours = filterContours(contours, 100); // Adjust the threshold as needed

        // Find bounding rectangles of remaining contours
        List<Rectangle> boundingRectangles = findBoundingRectangles(contours);

        // Find the largest empty space
        Rectangle largestEmptySpace = findLargestEmptySpace(boundingRectangles, appWidth, appHeight);

        return largestEmptySpace;
    }

    private static Mat bufferedImageToMat(BufferedImage image) {
        // Convert BufferedImage to Mat
        BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        convertedImage.getGraphics().drawImage(image, 0, 0, null);

        Mat matImage = new Mat(convertedImage.getHeight(), convertedImage.getWidth(), CvType.CV_8UC3);
        DataBuffer dataBuffer = convertedImage.getRaster().getDataBuffer();
        byte[] data;
        if (dataBuffer instanceof DataBufferByte) {
            data = ((DataBufferByte) dataBuffer).getData();
        } else if (dataBuffer instanceof DataBufferInt) {
            int[] intData = ((DataBufferInt) dataBuffer).getData();
            data = new byte[intData.length * 4];
            for (int i = 0; i < intData.length; i++) {
                data[i * 3] = (byte) ((intData[i] >> 16) & 0xFF); // Blue
                data[i * 3 + 1] = (byte) ((intData[i] >> 8) & 0xFF); // Green
                data[i * 3 + 2] = (byte) (intData[i] & 0xFF); // Red
            }
        } else {
            throw new IllegalArgumentException("Unsupported data buffer type: " + dataBuffer.getClass().getName());
        }
        matImage.put(0, 0, data);
        return matImage;
    }




    private static Mat convertToGrayscale(Mat image) {
        // Convert image to grayscale
        Mat grayscaleImage = new Mat();
        Imgproc.cvtColor(image, grayscaleImage, Imgproc.COLOR_BGR2GRAY);
        return grayscaleImage;
    }

    private static Mat applyThreshold(Mat image) {
        // Apply thresholding
        Mat thresholdedImage = new Mat();
        Imgproc.threshold(image, thresholdedImage, 127, 255, Imgproc.THRESH_BINARY);
        return thresholdedImage;
    }

    private static List<MatOfPoint> findContours(Mat image) {
        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;
    }

    private static List<MatOfPoint> filterContours(List<MatOfPoint> contours, double minArea) {
        // Filter out small contours
        List<MatOfPoint> filteredContours = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area > minArea) {
                filteredContours.add(contour);
            }
        }
        return filteredContours;
    }

    private static List<Rectangle> findBoundingRectangles(List<MatOfPoint> contours) {
        // Find bounding rectangles of contours
        List<Rectangle> boundingRectangles = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            boundingRectangles.add(new Rectangle(rect.x, rect.y, rect.width, rect.height));
        }
        return boundingRectangles;
    }

    private static Rectangle findLargestEmptySpace(List<Rectangle> boundingRectangles, int appWidth, int appHeight) {
        // Find the largest empty space
        Rectangle largestEmptySpace = null;
        int largestArea = Integer.MIN_VALUE;
        for (Rectangle rectangle : boundingRectangles) {
            // Check if this rectangle can accommodate the application window
            int x = rectangle.x;
            int y = rectangle.y;
            int width = rectangle.width;
            int height = rectangle.height;
            if (width >= appWidth && height >= appHeight) {
                int area = width * height;
                if (area > largestArea) {
                    largestArea = area;
                    largestEmptySpace = rectangle;
                }
            }
        }
        return largestEmptySpace;
    }

    public static Pair<Integer, Integer> screenEmptySpaceFinder() {
        try {
            // Example usage: Find empty space for an application window with width 200 and height 100
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenImage = null;
            try {
                screenImage = new Robot().createScreenCapture(screenRect);
                File outputFile = new File("captured_screen.png");
                ImageIO.write(screenImage, "png", outputFile);

                System.out.println("Screen captured and saved to: " + outputFile.getAbsolutePath());
            } catch (AWTException | IOException e) {
                e.printStackTrace();
            }

            Mat capturedImage = Imgcodecs.imread("captured_screen.png");

            // Convert to grayscale
            Mat grayImage = new Mat();
            Imgproc.cvtColor(capturedImage, grayImage, Imgproc.COLOR_BGR2GRAY);

            // Apply adaptive thresholding to separate text/lines from background
            Mat binaryImage = new Mat();
            Imgproc.adaptiveThreshold(grayImage, binaryImage, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 15, 5); // Adjust parameters as needed

            // Perform morphological operations to reduce noise
            Mat morphedImage = new Mat();
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 1)); // Adjust kernel size
            Imgproc.morphologyEx(binaryImage, morphedImage, Imgproc.MORPH_CLOSE, kernel);

            // Find contours
            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(morphedImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            // Filter contours based on area or other characteristics (e.g., aspect ratio)
            List<MatOfPoint> filteredContours = new ArrayList<>();
            for (MatOfPoint contour : contours) {
                double area = Imgproc.contourArea(contour);
                if (area > 10 && area < 50000) { // Adjust area threshold as needed
                    // You can add additional criteria here, such as aspect ratio or solidity

                }else {
                    filteredContours.add(contour);
                }
            }

            // Visualize filtered contours (for debugging)
            Mat resultImage = new Mat();
            capturedImage.copyTo(resultImage);
            Imgproc.drawContours(resultImage, filteredContours, -1, new Scalar(0, 255, 0), 2);

            // Save result image (for visualization)
            Imgcodecs.imwrite("contours_detected.png", resultImage);
            double totalArea = capturedImage.rows() * capturedImage.cols();

// Calculate the combined area occupied by detected text and lines
            double occupiedArea = 0.0;
            for (MatOfPoint contour : filteredContours) {
                occupiedArea += Imgproc.contourArea(contour);
            }

// Calculate the remaining empty space
            double remainingArea = totalArea - occupiedArea;
            Random random = new Random();
            int x = random.nextInt(capturedImage.cols());
            int y = random.nextInt(capturedImage.rows());
            Pair<Integer, Integer> coordinates = new Pair<>(x, y);

            // Return the Pair object containing the coordinates
            Pair<Integer, Integer> emptySpaceCoordinates = findLatestEmptySpace(capturedImage, resultImage);
            System.out.println("emptySpaceCoordinates: " + emptySpaceCoordinates);
            return emptySpaceCoordinates;

            // Select empty space (e.g., largest connected component)
            // Implementation depends on the specific requirements and characteristics of your images

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Pair<Integer, Integer> findLatestEmptySpace(Mat capturedImage, Mat contoursDetectedImage) {
        // Convert contours detected image to grayscale
        Mat grayContoursImage = new Mat();
        Imgproc.cvtColor(contoursDetectedImage, grayContoursImage, Imgproc.COLOR_BGR2GRAY);

        // Threshold to create a binary image where non-contour areas are white (255) and contour areas are black (0)
        Mat binaryContoursImage = new Mat();
        Imgproc.threshold(grayContoursImage, binaryContoursImage, 1, 255, Imgproc.THRESH_BINARY);

        // Find contours in the binary image
        List<MatOfPoint> emptyContours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binaryContoursImage, emptyContours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find the largest contour, which represents the largest empty space
        double largestEmptyArea = 0.0;
        MatOfPoint largestEmptyContour = null;
        for (MatOfPoint emptyContour : emptyContours) {
            double emptyContourArea = Imgproc.contourArea(emptyContour);
            if (emptyContourArea > largestEmptyArea) {
                largestEmptyArea = emptyContourArea;
                largestEmptyContour = emptyContour;
            }
        }

        // If no empty space is found, return null or handle appropriately
        if (largestEmptyContour == null) {
            return null;
        }

        // Calculate the centroid of the largest empty space contour
        Moments moments = Imgproc.moments(largestEmptyContour);
        int centroidX = (int) (moments.get_m10() / moments.get_m00());
        int centroidY = (int) (moments.get_m01() / moments.get_m00());

        // Return the centroid coordinates
        return new Pair<>(centroidX, centroidY);
    }


}
