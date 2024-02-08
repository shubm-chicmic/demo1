package com.example.demo1.WindowEmptyPositionFinding;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Point;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScreenAnalyzer {

    static {
        // Load OpenCV native library dynamically
        OpenCV.loadShared();
    }

    public static Rect findEmptyArea(double minWidth, double minHeight, double minDarkness) {
        try {
            BufferedImage screenShot = captureScreen();

            Mat image = bufferedImageToMat(screenShot);

            // Convert to grayscale
            Mat gray = new Mat();
            Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

            // Apply thresholding to separate foreground from background
            Mat binary = new Mat();
            Imgproc.threshold(gray, binary, minDarkness, 255, Imgproc.THRESH_BINARY);

            // Find contours
            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            // Filter contours based on size and darkness
            List<Rect> emptyAreas = new ArrayList<>();
            for (MatOfPoint contour : contours) {
                Rect rect = Imgproc.boundingRect(contour);
                if (rect.width >= minWidth && rect.height >= minHeight && calculateDarkness(gray, rect) >= minDarkness) {
                    emptyAreas.add(rect);
                }
            }
            return LargestDarkRec.findLargestDarkestArea(image);
//            EmptySpaceDetector.highlightEmptySpaces(screenShot);
//            System.out.println("Empty areas found: " + emptyAreas.size());
//            // Highlight contours and empty areas for testing
//            highlightContours(screenShot, contours, emptyAreas, "output.png");
//
//            // Return the first empty area found (or null if none found)
//            if (!emptyAreas.isEmpty()) {
//                return emptyAreas.get(0);
//            } else {
//                return null;
//            }
        } catch (AWTException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedImage captureScreen() throws AWTException {
        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        return robot.createScreenCapture(screenRect);
    }

    private static double calculateDarkness(Mat gray, Rect rect) {
        Mat roi = new Mat(gray, rect);
        Scalar mean = Core.mean(roi);
        return mean.val[0];
    }

    private static void highlightContours(BufferedImage originalImage, List<MatOfPoint> contours, List<Rect> emptyAreas, String outputImagePath) {
        try {
            // Convert the input image to BGR format
            BufferedImage bgrImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = bgrImage.createGraphics();
            g.drawImage(originalImage, 0, 0, null);
            g.dispose();

            // Convert the BufferedImage to a Mat object
            Mat image = bufferedImageToMat(bgrImage);

            // Draw contours on the image
            for (MatOfPoint contour : contours) {
                Imgproc.drawContours(image, List.of(contour), -1, new Scalar(0, 255, 0), 2);
            }

            // Draw rectangles for empty areas on the image
            for (Rect rect : emptyAreas) {
                Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255), 2);
            }

            // Convert the Mat back to a BufferedImage
            BufferedImage outputImage = matToBufferedImage(image);

            // Write the output image to disk
            ImageIO.write(outputImage, "png", new File(outputImagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage matToBufferedImage(Mat mat) {
        int type = mat.channels() > 1 ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage image = new BufferedImage(mat.width(), mat.height(), type);
        mat.get(0, 0, ((DataBufferByte) image.getRaster().getDataBuffer()).getData());
        return image;
    }

    public static Mat bufferedImageToMat(BufferedImage image) {
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
