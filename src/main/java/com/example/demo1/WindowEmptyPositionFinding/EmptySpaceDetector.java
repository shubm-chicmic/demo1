package com.example.demo1.WindowEmptyPositionFinding;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EmptySpaceDetector {


    public static List<Rect> findDarkAreas(Mat originalImage) {
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

        return darkAreas;
    }



    public static boolean isValidArea(Rect rect) {
        // Filter out contours representing text or graphics based on size or other criteria
        // You can customize this method based on your specific requirements
        return rect.area() > 100; // Example: exclude small areas
    }



    public static BufferedImage highlightEmptySpaces(BufferedImage originalImage) {
        // Convert the original image to grayscale
        Mat grayImage = convertToGrayscale(originalImage);

        // Apply thresholding to the grayscale image
        Mat binaryImage = applyThreshold(grayImage, 100); // Adjust threshold value as needed
        System.out.println("Im applying threshold");
        // Find contours in the binary image
        List<Rect> darkAreas = findDarkAreas(grayImage);
        System.out.println("Dark areas: " + darkAreas.size());
        Highlighter.highlightAreasWithBox(originalImage, darkAreas);
        List<Rect> emptySpaces = filterGraphics(originalImage, darkAreas);

        BufferedImage highlightedImage = highlightAreas(originalImage, emptySpaces);
        // Save the highlighted image to the root directory
        try {
            File outputImageFile = new File("highlighted_image.png");
            ImageIO.write(highlightedImage, "png", outputImageFile);
            System.out.println("Highlighted image saved to: " + outputImageFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return highlightedImage;
    }

    private static Mat convertToGrayscale(BufferedImage originalImage) {
        Mat mat = new Mat();
        Mat grayMat = new Mat();
        Imgproc.cvtColor(bufferedImageToMat(originalImage), mat, Imgproc.COLOR_BGR2GRAY);
        return mat;
    }

    private static Mat applyThreshold(Mat grayImage, int thresholdValue) {
        Mat binaryImage = new Mat();
        Imgproc.threshold(grayImage, binaryImage, thresholdValue, 255, Imgproc.THRESH_BINARY);
        return binaryImage;
    }

    private static List<Rect> findContours(Mat binaryImage) {
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(binaryImage, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        List<Rect> boundingRects = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            boundingRects.add(Imgproc.boundingRect(contour));
        }
        return boundingRects;
    }

    private static List<Rect> filterGraphics(BufferedImage originalImage, List<Rect> darkAreas) {
        List<Rect> emptySpaces = new ArrayList<>();
        for (Rect rect : darkAreas) {
            boolean isGraphic = false;
            // Iterate over each pixel in the rectangle
            for (int y = rect.y; y < rect.y + rect.height; y++) {
                for (int x = rect.x; x < rect.x + rect.width; x++) {
                    // Get the RGB values of the pixel
                    int rgb = originalImage.getRGB(x, y);
                    Color color = new Color(rgb);
                    // Check if the pixel is not dark or not empty
                    if (!isDark(color) || !isEmpty(color)) {
                        isGraphic = true;
                        break;
                    }
                }
                if (isGraphic) {
                    break;
                }
            }
            // If the rectangle contains no graphics, add it to the list of empty spaces
            if (!isGraphic) {
                emptySpaces.add(rect);
            }
        }
        return emptySpaces;
    }



    private static boolean isDark(Color color) {
        // Define a threshold for darkness (adjust as needed)
        int darknessThreshold = 100;
        // Check if the color is dark based on its brightness (luminance)
        return (color.getRed() + color.getGreen() + color.getBlue()) / 3 < darknessThreshold;
    }

    private static boolean isEmpty(Color color) {
        // Check if the color is empty (white)
        return color.equals(Color.WHITE);
    }


    private static BufferedImage highlightAreas(BufferedImage originalImage, List<Rect> emptySpaces) {
        Graphics2D g2d = originalImage.createGraphics();
        g2d.setColor(Color.RED);
        for (Rect rect : emptySpaces) {
            g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
        }
        g2d.dispose();
        return originalImage;
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
            data = new byte[intData.length * 3];
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
}
