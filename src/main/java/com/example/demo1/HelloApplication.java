package com.example.demo1;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class HelloApplication extends Application {
    public String extractTotalTime(String responseBody) {
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

    public String empTimeCal(String empCode, Integer workingTime) {
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

                    LocalTime timeLeftToCompleteInputHours = LocalTime.of(workingTime, 0)
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

            Thread counterThread = new Thread(() -> {
                for (long seconds = totalSeconds; seconds >= 0; seconds--) {
                    long hours = seconds / 3600;
                    long minutes = (seconds % 3600) / 60;
                    long remainingSeconds = seconds % 60;

                    String remainingTime = String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);

                    // Update the remainingTime string
                    synchronized (this) {
                        remainingTimeStr = remainingTime;
                    }

                    try {
                        Thread.sleep(1000); // Wait for one second
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            counterThread.start();
            return String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
        }
    }

    private double offsetX;
    private double offsetY;
    // Add a flag to keep track of the current state
    private boolean iconsVisible = false;
    private boolean timeVisible = false;
//    private WebView webView;
//    private WebEngine webEngine;
//    private boolean htmlView = false;
    private boolean flagToggle= false;

    @Override
    public void start(Stage primaryStage) {

        Stage topBarStage = new Stage();
        topBarStage.initStyle(StageStyle.TRANSPARENT);

//        topBarStage.setAlwaysOnTop(true);
        // Set default position to bottom right corner
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();
        double defaultX = screenWidth - 100; // Adjust the X-coordinate as needed
        double defaultY = screenHeight - 100; // Adjust the Y-coordinate as needed
        topBarStage.setX(defaultX);
        topBarStage.setY(defaultY);

        AnchorPane root = new AnchorPane();

        Integer timeField = 8;
        String empCode = "544";
        // Create a Text node to display your text
        String empTime = empTimeCal(empCode, timeField); // Get your text
        remainingTimeStr = remainingTime(empTime);
        Text helloText = new Text(empTime);
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
        ImageView icon1 = new ImageView(new Image("/input.png")); // Replace with the actual path to your icon image
        ImageView icon2 = new ImageView(new Image("/settings.png")); // Replace with the actual path to your icon image

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


        root.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2){
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
            }else {
                if (timeVisible) {
                    helloText.setText(empTime);
                    timeVisible = false;
                } else {
                    helloText.setText(remainingTimeStr);
                    timeVisible = true;
                }
            }
        });

        icon1.setOnMouseClicked(event -> {
//            WebView webView = new WebView();
            HtmlDialog dialog = new HtmlDialog("/input.html");
//            webView.getEngine().load(getClass().getResource("/input.html").toExternalForm()); // Replace with your HTML content
//            webView.setPrefSize(300, 200); // Adjust the size as needed
//            Scene webViewScene = new Scene(webView);
//            Stage webViewStage = new Stage();
//
//            webViewStage.setScene(webViewScene);
//
//            webViewScene.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
//                if (keyEvent.getCode() == KeyCode.ENTER) {
//                    webViewStage.hide();
//                }
//            });
//
//            webViewStage.show();
////            topBarStage.hide();

        });


        icon2.setOnMouseClicked(event -> {
            System.out.println("icon2 clicked");
            Stage settingsStage = new Stage();
            settingsStage.setTitle("Settings");

            AnchorPane settingsRoot = new AnchorPane();

            // Create a checkbox that automatically fills timesheet
            CheckBox autoFillCheckbox = new CheckBox("Automatically Fill Timesheet");
            autoFillCheckbox.setSelected(flagToggle); // Set checkbox state based on flagToggle
            autoFillCheckbox.setOnAction(checkEvent -> {
                flagToggle = autoFillCheckbox.isSelected(); // Update flagToggle based on checkbox state

                // Perform actions based on flagToggle state (e.g., fill timesheet)
                if (flagToggle) {
                    // Automatically fill timesheet
                } else {
                    // Disable automatic timesheet filling
                }
            });

            // Position the autoFillCheckbox within the settingsRoot
            settingsRoot.getChildren().add(autoFillCheckbox);
            Scene settingsScene = new Scene(settingsRoot, 400, 300);
            settingsStage.setScene(settingsScene);

            settingsStage.show();
        });

        root.getChildren().addAll(icon1, icon2, helloText);

        Scene scene = new Scene(root);

        scene.setFill(null);
        topBarStage.setScene(scene);
        topBarStage.show();

        primaryStage.setOnCloseRequest(event -> {
            topBarStage.close();
            System.exit(0);
            primaryStage.close();
        });
    }

    private InputData showWebView(Stage topBarStage, String htmlFilePath, String defaultEmpCode, String defaultTimeField) {
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.load(getClass().getResource(htmlFilePath).toExternalForm()); // Replace with your HTML content
        InputData inputData = new InputData();

        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                // Set the default values in the input fields
                webEngine.executeScript("document.getElementById('employeeCode').value = '" + defaultEmpCode + "'");
                webEngine.executeScript("document.getElementById('timeField').value = '" + defaultTimeField + "'");
            }
        });


        webView.setPrefSize(300, 200); // Adjust the size as needed

//        AnchorPane anchorPane = new AnchorPane(webView);
//        anchorPane.setStyle("-fx-background-color: transparent;");
//        AnchorPane.setTopAnchor(webView, 0.0);
//        AnchorPane.setBottomAnchor(webView, 0.0);
//        AnchorPane.setLeftAnchor(webView, 0.0);
//        AnchorPane.setRightAnchor(webView, 0.0);

        Scene webViewScene = new Scene(webView);
        Stage webViewStage = new Stage();
        webViewStage.initStyle(StageStyle.TRANSPARENT);
        webViewStage.setScene(webViewScene);
        webViewStage.show();
        topBarStage.hide();

        webView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                Object empCodeResult = webEngine.executeScript("document.getElementById('employeeCode').value");
                Object timeFieldResult = webEngine.executeScript("document.getElementById('timeField').value");
                if (empCodeResult != null && timeFieldResult != null) {
                    inputData.setEmpCode(empCodeResult.toString());
                    inputData.setTimeField(timeFieldResult.toString());

                }
                webViewStage.hide();
                topBarStage.show();
            } else if (event.getCode() == KeyCode.CLOSE_BRACKET) {
                inputData.setEmpCode("12345");
            }
        });
        return inputData;
    }


    private void notifyInput(String empCode, String timeField, Text helloText, WebView webView, Stage stage) {
        System.out.println("Notify Input: " + empCode);
        String newEmpTime = empTimeCal(empCode, Integer.valueOf(timeField == null ? "0" : timeField));
//        helloText.setText(newEmpTime);
//        webView.setPrefSize(0, 0);
//        webView.setVisible(false);
        stage.hide();
    }


    public static void main(String[] args) throws IOException {
//        Runtime runtime = Runtime.getRuntime();
//        runtime.exec("echo '1234' | sudo systemctl poweroff");
        launch(args);
    }
}
