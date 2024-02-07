package com.example.demo1.ImageTextAndIconAnalysis;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
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
import java.util.List;

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

    public static void main(String[] args) {
        try {
            // Example usage: Find empty space for an application window with width 200 and height 100
            Rectangle emptySpace = findEmptySpace(200, 100);
            if (emptySpace != null) {
                System.out.println("Empty space found: " + emptySpace);
            } else {
                System.out.println("No empty space found.");
            }
            Mat capturedImage = Imgcodecs.imread("captured_screen.png");

            // Convert to grayscale
            Mat grayImage = new Mat();
            Imgproc.cvtColor(capturedImage, grayImage, Imgproc.COLOR_BGR2GRAY);

            // Thresholding to separate text/images from background
            Mat thresholdedImage = new Mat();
            Imgproc.threshold(grayImage, thresholdedImage, 200, 255, Imgproc.THRESH_BINARY_INV);

            // Find contours of potential empty space regions
            Mat hierarchy = new Mat();
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(thresholdedImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            // Filter out contours based on size or other criteria
            List<MatOfPoint> emptySpaceContours = new ArrayList<>();
            for (MatOfPoint contour : contours) {
                Rect boundingRect = Imgproc.boundingRect(contour);
                // Example filter: only consider contours with area within a specific range
                if (boundingRect.area() > 1000 && boundingRect.area() < 50000) {
                    emptySpaceContours.add(contour);
                }
            }

            // Draw filtered contours on original image
            Mat resultImage = new Mat();
            capturedImage.copyTo(resultImage);
            Imgproc.drawContours(resultImage, emptySpaceContours, -1, new Scalar(0, 255, 0), 2);

            // Save result image
            Imgcodecs.imwrite("empty_space_regions.png", resultImage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
