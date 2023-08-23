package com.example.demo1;

import com.example.demo1.AutoUpdate.AutoUpdateManager;
import javafx.application.Application;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Scene;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.swing.*;

//import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;
public class HelloApplication extends Application {
    public static String extractTotalTime(String responseBody) {
        // Find the index of "totalTimeInWorkZone" in the response body
        int totalTimeIndex = responseBody.indexOf("\"totalTimeInWorkZone\":\"");
        if (totalTimeIndex != -1) {
            // Extract the value of "totalTimeInWorkZone"
            int valueStartIndex = totalTimeIndex + "\"totalTimeInWorkZone\":\"".length();
            int valueEndIndex = responseBody.indexOf('"', valueStartIndex);
            if (valueEndIndex != -1) {
                String totalTimeInWorkZone = responseBody.substring(valueStartIndex, valueEndIndex);
                return totalTimeInWorkZone;
            }
        }

        // Handle if "totalTimeInWorkZone" is not found or extraction fails
        return "Error: Unable to extract totalTimeInWorkZone";
    }

    public static String empTimeCal(String empCode, String workingTime) {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2M2NlN2JmZjkzZTkxMzA2N2QwMmNlOGQiLCJlbWFpbCI6InNodWIubWlzaHJhMjIxMEBnbWFpbC5jb20iLCJ0aW1lIjoxNjkxNDg1OTUwMjY5LCJpYXQiOjE2OTE0ODU5NTB9.NazGmjzozuxoMJlPg7nbfYXmXOgOlXjMtwl95Saesiw";
//        String url = "https://apigateway.erp.chicmic.in/v1/biometric/punches";
        String url = "https://apigateway.erp.chicmic.in/v1/biometric/time-spent";

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, token);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            LocalDate date = LocalDate.now();
            // Set the request body
            String requestBody = "{\"date\": \"" + date.toString() + "\", \"empId\": \"" + empCode + "\"}";
            System.out.println(requestBody);
            httpPost.setEntity(new StringEntity(requestBody));

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    HttpEntity responseEntity = response.getEntity();
                    String responseBody = EntityUtils.toString(responseEntity);
                    String totalTimeInWorkZoneStr = extractTotalTime(responseBody);
                    if (totalTimeInWorkZoneStr == null || totalTimeInWorkZoneStr.isEmpty()) {
                        totalTimeInWorkZoneStr = "00:00:00";
                    }
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                    LocalTime totalTimeInWorkZone = LocalTime.parse(totalTimeInWorkZoneStr, formatter);

                    LocalTime timeLeftToCompleteInputHours = LocalTime.of(Integer.parseInt(workingTime), 0)
                            .minusHours(totalTimeInWorkZone.getHour())
                            .minusMinutes(totalTimeInWorkZone.getMinute());
//                    LocalTime timeLeftToComplete8Hours = LocalTime.of(8, 0)
//                            .minusHours(totalTimeInWorkZone.getHour())
//                            .minusMinutes(totalTimeInWorkZone.getMinute());

                    // Add the remaining time to totalTimeInWorkZone
                    LocalTime completionTimeInputHours = LocalTime.now().plusHours(timeLeftToCompleteInputHours.getHour())
                            .plusMinutes(timeLeftToCompleteInputHours.getMinute());
                    // Format the completion times without nanoseconds
                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                    String formattedCompletionTimeInputHours = completionTimeInputHours.format(outputFormatter);
                    return formattedCompletionTimeInputHours;

                } else {
                    // Handle non-OK response status
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "00:00:00";
            // Handle exception
        }

        // Return appropriate result
        return null;
    }

    private volatile String remainingTimeStr = "";

    public String remainingTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime currentTime = LocalTime.now();
        LocalTime targetTime = LocalTime.parse(time, formatter);

        if (currentTime.isAfter(targetTime)) {
            return "0";
        } else {
            Duration duration = Duration.between(currentTime, targetTime);
            long totalSeconds = duration.getSeconds();
//
//            Thread counterThread = new Thread(() -> {
//                for (long seconds = totalSeconds; seconds >= 0; seconds--) {
//                    long hours = seconds / 3600;
//                    long minutes = (seconds % 3600) / 60;
//                    long remainingSeconds = seconds % 60;
//
//                    String remainingTime = String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
//
//                    // Update the remainingTime string
//                    synchronized (this) {
//                        remainingTimeStr = remainingTime;
//                    }
//
//                    try {
//                        Thread.sleep(1000); // Wait for one second
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//
//            counterThread.start();
            return String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
        }
    }
    public static boolean isFirstTimeGreaterThanSecond(LocalTime firstTime, LocalTime secondTime) {
        return firstTime.isAfter(secondTime);
    }

    private double offsetX;
    private double offsetY;
    // Add a flag to keep track of the current state
    private boolean iconsVisible = false;
    private boolean timeVisible = false;
    private boolean autoFillTimeSheet = false;
    public boolean autoUpdateTime = false;
    public String[] timeField = {"8"};
    public String[] empCode = {"574"};
    Text helloText = new Text("");
    AtomicReference<String> empTime = new AtomicReference<String>("");

    @Override
    public void start(Stage primaryStage) {
        AutoUpdateRunner runner = new AutoUpdateRunner(this);
        AutoUpdateManager.addPropertyChangeListener(runner);

        Stage topBarStage = new Stage();
        topBarStage.initStyle(StageStyle.TRANSPARENT);
        topBarStage.setAlwaysOnTop(true);
        // Set default position to bottom right corner
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();
        double defaultX = screenWidth - 100; // Adjust the X-coordinate as needed
        double defaultY = screenHeight - 100; // Adjust the Y-coordinate as needed
        topBarStage.setX(defaultX);
        topBarStage.setY(defaultY);

        AnchorPane root = new AnchorPane();

        // Create a Text node to display your text
        empTime.set(empTimeCal(empCode[0], timeField[0]));
        remainingTimeStr = remainingTime(empTime.get());
        helloText.setText(empTime.get());
        helloText.setFont(Font.font("Arial", 15)); // Set your desired font and size
        helloText.setFill(Color.WHITE); // Set your desired text color

        double padding = 5;
        AnchorPane.setLeftAnchor(helloText, padding);
        AnchorPane.setTopAnchor(helloText, padding);
        AnchorPane.setBottomAnchor(helloText, padding);
        AnchorPane.setRightAnchor(helloText, padding);


        double borderThickness = 1.5;
        root.setStyle("-fx-border-color: transparent; -fx-border-width: " + borderThickness + "px; -fx-border-radius: 10px; -fx-padding: " + padding + "px;");

        AnchorPane.setLeftAnchor(helloText, padding + borderThickness);
        AnchorPane.setTopAnchor(helloText, padding + borderThickness);




        root.setOnMouseEntered(event -> {
            root.setStyle("-fx-border-color: white; -fx-border-width: " + borderThickness + "px; -fx-border-radius: 10px; -fx-padding: " + padding + "px;");

        });
        root.setOnMouseExited(event -> {
            root.setStyle("-fx-border-color: transparent; -fx-border-width: " + borderThickness + "px; -fx-border-radius: 10px; -fx-padding: " + padding + "px;");

        });

        root.setOnMousePressed(event -> {
            offsetX = event.getSceneX();
            offsetY = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            topBarStage.setX(event.getScreenX() - offsetX);
            topBarStage.setY(event.getScreenY() - offsetY);
        });
        ImageView icon1 = new ImageView(new Image("/log-in.png")); // Replace with the actual path to your icon image
        ImageView icon2 = new ImageView(new Image("/settings2.png")); // Replace with the actual path to your icon image

        double iconSize = 25;
        icon1.setFitWidth(iconSize);
        icon1.setFitHeight(iconSize);
        icon2.setFitWidth(iconSize);
        icon2.setFitHeight(iconSize);


        AnchorPane.setLeftAnchor(icon1, padding + borderThickness);
        AnchorPane.setTopAnchor(icon1, padding + borderThickness);

        AnchorPane.setRightAnchor(icon2, padding + borderThickness);
        AnchorPane.setTopAnchor(icon2, padding + borderThickness);

        // Hide the icons initially
        icon1.setVisible(false);
        icon2.setVisible(false);
        icon1.setOnMouseClicked(event -> {
//            System.out.println("\u001B[33m " + timeField + " code : " + empCode[0] + " time : " + empTime );
            InputData inputData = new InputData();
            inputData.setTimeField(timeField[0]);
            inputData.setEmpCode(empCode[0]);
            JDialog jDialog = EmployeeForm.showForm(inputData);
            System.out.println("values in the main : " + inputData.getEmpCode() + " " + inputData.getTimeField());


            // Get the bounds of the icon2 in screen coordinates
            Bounds icon2Bounds = icon2.localToScreen(icon2.getBoundsInLocal());

            // Calculate the position for the form just above the icon2
            double formWidth = 400; // Adjust the width as needed
            double formHeight = 200; // Adjust the height as needed
            double formX = icon2Bounds.getMinX(); // X position is same as icon2's left boundary
            double formY = icon2Bounds.getMinY() - 100; // Y position is just above icon2

            // Set the position for the form
            EmployeeForm.setFormPosition(formX, formY);

//            final String updatedEmpCode = inputData.getEmpCode();
//            final int updatedTimeField = Integer.parseInt(inputData.getTimeField());
////
//            // Update helloText with the new empTime

            jDialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    empCode[0] = inputData.getEmpCode();
                    timeField[0] = (inputData.getTimeField());
                    empTime.set(empTimeCal(empCode[0], timeField[0]));
                    remainingTimeStr = remainingTime(empTime.get());
                    System.out.println("updatedEmpCode : " + empCode[0]);
                    helloText.setText(String.valueOf(empTime));
                    helloText.setVisible(true);
                    icon1.setVisible(false);
                    icon2.setVisible(false);
                    iconsVisible = false;
                    timeVisible = false;
                    System.out.println("icon clicked " + helloText.getText());


                }


            });
//            Platform.runLater(() -> {
//                helloText.setText(String.valueOf(empTime[0]));
//                helloText.setVisible(true);
//                icon1.setVisible(false);
//                icon2.setVisible(false);
//                iconsVisible = false;
//                timeVisible = false;
//            });
            System.out.println("icon clicked " + helloText.getText());
            runner.setAutoUpdateTime(false);
        });

        icon2.setOnMouseClicked(event -> {
            runner.setAutoUpdateTime(true);

        });



        root.setOnMouseClicked(event -> {
//            System.out.println("Mouse clicked " + helloText.getText());
            if (event.getClickCount() == 2) {
                if (iconsVisible) {
                    // Text box is visible, so hide icons and show text box
                    helloText.setVisible(true);
                    icon1.setVisible(false);
                    icon2.setVisible(false);
                    iconsVisible = false;
                } else {
                    // Icons are visible, so show icons and hide text box
                    helloText.setVisible(false);
                    icon1.setVisible(true);
                    icon2.setVisible(true);
                    iconsVisible = true;
                }
            } else {
                if (timeVisible) {
                    helloText.setText(String.valueOf(empTime));
                    timeVisible = false;
                } else {
                    helloText.setText(remainingTimeStr);
                    timeVisible = true;
                }
            }
        });
//        TimeUpdateScheduler timeUpdateScheduler = new TimeUpdateScheduler();
//        UpdateTask updateTask = new UpdateTask(this, empCode[0], timeField[0]);
//        timeUpdateScheduler.startUpdateTask(updateTask);
        // Register the listener first
        runner.setAutoUpdateTime(false);
           // schedule task

        Timer timer = new Timer();
        final TimerTask[] conditionTask = new TimerTask[1]; // Declare a final array to hold the TimerTask

        conditionTask[0] = new TimerTask() {
            @Override
            public void run() {
                if (isFirstTimeGreaterThanSecond(LocalTime.now(), LocalTime.parse(empTime.get()))) {
                    System.out.println("Hello text " + helloText.getText());

                    TimeSheetFill timeSheetFill = new TimeSheetFill();
                    String response = timeSheetFill.fillTimeSheet(empCode[0], timeField[0]);
                    System.out.println("\u001B[33m response inside the timesheet" + response + "\u001B[0m");
                    conditionTask[0].cancel();
                }
            }
        };
        timer.scheduleAtFixedRate(conditionTask[0], 0, 5000);


        root.getChildren().addAll(icon1, icon2, helloText);

        Scene scene = new Scene(root);

        scene.setFill(null);
        topBarStage.setScene(scene);
        topBarStage.show();

        primaryStage.setOnCloseRequest(event -> {
            conditionTask[0].cancel();
            timer.cancel();
            topBarStage.close();
            System.exit(0);
            primaryStage.close();

        });
    }

    //    private static String getFormFieldValue(String htmlContent, String fieldName) {
//        int start = htmlContent.indexOf(fieldName + "\"") + fieldName.length() + 2;
//        int end = htmlContent.indexOf("\"", start);
//        return htmlContent.substring(start, end);
//    }
    private String getAttributeValueFromTagById(String htmlContent, String tagName, String id, String attributeName) {
        String pattern = "<" + tagName + "\\s+[^>]*id=\"" + id + "\"[^>]*" + attributeName + "=\"([^\"]*)\"";
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(htmlContent);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return ""; // Return an empty string if not found
        }
    }
    public void updateUIComponents(String updatedEmpTime) {
        Platform.runLater(() -> {
            helloText.setText(updatedEmpTime);
            remainingTimeStr = remainingTime(updatedEmpTime);
            empTime.set(updatedEmpTime);

        });
    }

    private void executeJavaScript(JEditorPane editorPane, String jsCode) {
        Platform.runLater(() -> {
            WebView webView = new WebView(); // Create an HTMLEditor
            WebEngine webEngine = webView.getEngine(); // Get the WebEngine

            // Load a dummy content to the HTMLEditor
            webEngine.loadContent("<html><head></head><body></body></html>");

            // Execute the JavaScript code
            webEngine.executeScript(jsCode);
        });
    }

    public static String addAttributeValueToTagById(String htmlContent, String tagName, String id, String attribute, String value) {
        String tagPattern = "<" + tagName + "\\s+[^>]*?id=\"" + id + "\"[^>]*>";
        Pattern pattern = Pattern.compile(tagPattern);
        Matcher matcher = pattern.matcher(htmlContent);

        if (matcher.find()) {
            String tag = matcher.group();
            String replacement = tag.replaceFirst(">", " " + attribute + "=\"" + value + "\">");
            htmlContent = htmlContent.replace(tag, replacement);
        }

        return htmlContent;
    }

    public static void main(String[] args) throws IOException {
//        Runtime runtime = Runtime.getRuntime();
//        runtime.exec("echo '1234' | sudo systemctl poweroff");
        launch(args);
    }
}
