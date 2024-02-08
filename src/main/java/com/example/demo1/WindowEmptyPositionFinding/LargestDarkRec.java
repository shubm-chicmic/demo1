package com.example.demo1.WindowEmptyPositionFinding;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.example.demo1.WindowEmptyPositionFinding.EmptySpaceDetector.isValidArea;

public class LargestDarkRec {
    // Constants for defining the location priority
    private static final int TOP = 1;
    private static final int BOTTOM = 2;
    private static final int LEFT = 3;
    private static final int RIGHT = 4;
    private static final int MIDDLE = 5;

    public static Rect findLargestDarkestArea(Mat originalImage) {
        // Convert the original image to grayscale
        Mat grayImage = new Mat();
        if (originalImage.channels() > 1) {
            Imgproc.cvtColor(originalImage, grayImage, Imgproc.COLOR_BGR2GRAY);
        } else {
            grayImage = originalImage.clone(); // Use the original image if it's already grayscale
        }

        // Apply histogram equalization to enhance contrast
        Mat equalizedImage = new Mat();
        Imgproc.equalizeHist(grayImage, equalizedImage);

        // Apply Gaussian blur to smooth the image and reduce noise
        Mat blurredImage = new Mat();
        Imgproc.GaussianBlur(equalizedImage, blurredImage, new Size(5, 5), 0);

        // Apply adaptive thresholding to segment dark areas
        Mat binaryMask = new Mat();
        Imgproc.adaptiveThreshold(blurredImage, binaryMask, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 11, 4);

        // Perform morphological operations to further refine the regions
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.erode(binaryMask, binaryMask, kernel);
        Imgproc.dilate(binaryMask, binaryMask, kernel);

        // Find contours in the binary mask
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(binaryMask, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Filter out contours representing text or graphics
        List<Rect> darkAreas = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            if (isValidArea(rect)) {
                darkAreas.add(rect);
            }
        }

        // Sort dark areas by size (largest first)
        Collections.sort(darkAreas, Comparator.comparingDouble(LargestDarkRec::calculateArea));


        // Find the location of the largest dark area
        return findLocation(originalImage, darkAreas);
    }

    private static boolean isValidArea(Rect rect) {
        // Filter out contours representing text or graphics based on size or other criteria
        // You can customize this method based on your specific requirements
        return rect.area() > 100; // Example: exclude small areas
    }

    private static double calculateArea(Rect rect) {
        return rect.width * rect.height;
    }
    private static Rect findLocation(Mat originalImage, List<Rect> darkAreas) {
        // Find the dimensions of the original image
        int imageWidth = originalImage.width();
        int imageHeight = originalImage.height();

        // Find the center point of the image
        Point center = new Point(imageWidth / 2, imageHeight / 2);

        // Initialize variables to store the location of the largest dark area
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        int maxArea = 0;
        int locationPriority = MIDDLE;

        // Iterate through the dark areas and prioritize by location
        for (Rect rect : darkAreas) {
            int area = rect.width * rect.height;
            int centerX = rect.x + rect.width / 2;
            int centerY = rect.y + rect.height / 2;

            int distanceToCenter = (int) Math.sqrt(Math.pow(centerX - center.x, 2) + Math.pow(centerY - center.y, 2));
            int minXDistance = centerX;
            int minYDistance = centerY;
            int maxXDistance = imageWidth - centerX;
            int maxYDistance = imageHeight - centerY;

            // Check if the area is closer to the top
            if (minYDistance < minXDistance && minYDistance < maxXDistance && minYDistance < maxYDistance && minYDistance < distanceToCenter) {
                if (area > maxArea) {
                    minX = rect.x;
                    minY = rect.y;
                    maxX = rect.x + rect.width;
                    maxY = rect.y + rect.height;
                    maxArea = area;
                    locationPriority = TOP;
                }
            }
            // Check if the area is closer to the bottom
            else if (maxYDistance < minXDistance && maxYDistance < maxXDistance && maxYDistance < minYDistance && maxYDistance < distanceToCenter) {
                if (area > maxArea) {
                    minX = rect.x;
                    minY = rect.y;
                    maxX = rect.x + rect.width;
                    maxY = rect.y + rect.height;
                    maxArea = area;
                    locationPriority = BOTTOM;
                }
            }
            // Check if the area is closer to the left
            else if (minXDistance < minYDistance && minXDistance < maxXDistance && minXDistance < maxYDistance && minXDistance < distanceToCenter) {
                if (area > maxArea) {
                    minX = rect.x;
                    minY = rect.y;
                    maxX = rect.x + rect.width;
                    maxY = rect.y + rect.height;
                    maxArea = area;
                    locationPriority = LEFT;
                }
            }
            // Check if the area is closer to the right
            else if (maxXDistance < minXDistance && maxXDistance < minYDistance && maxXDistance < maxYDistance && maxXDistance < distanceToCenter) {
                if (area > maxArea) {
                    minX = rect.x;
                    minY = rect.y;
                    maxX = rect.x + rect.width;
                    maxY = rect.y + rect.height;
                    maxArea = area;
                    locationPriority = RIGHT;
                }
            }
        }

        // If the location priority is still MIDDLE, choose the largest area
        if (locationPriority == MIDDLE) {
            Rect largestArea = darkAreas.get(0);
            minX = largestArea.x;
            minY = largestArea.y;
            maxX = largestArea.x + largestArea.width;
            maxY = largestArea.y + largestArea.height;
        }

        return new Rect(minX, minY, maxX - minX, maxY - minY);
    }

}
