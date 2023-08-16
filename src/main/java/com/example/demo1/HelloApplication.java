package com.example.demo1;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
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
    public String empTimeCal(String empCode) {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2M2NlN2JmZjkzZTkxMzA2N2QwMmNlOGQiLCJlbWFpbCI6InNodWIubWlzaHJhMjIxMEBnbWFpbC5jb20iLCJ0aW1lIjoxNjkxNDg1OTUwMjY5LCJpYXQiOjE2OTE0ODU5NTB9.NazGmjzozuxoMJlPg7nbfYXmXOgOlXjMtwl95Saesiw";
//        String url = "https://apigateway.erp.chicmic.in/v1/biometric/punches";
        String url = "https://apigateway.erp.chicmic.in/v1/biometric/time-spent";

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, token);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            LocalDate date = LocalDate.now();
            // Set the request body
            String requestBody = "{\"date\": \""+ date.toString() + "\", \"empId\": \"" + empCode + "\"}";
            System.out.println(requestBody);
            httpPost.setEntity(new StringEntity(requestBody));

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    HttpEntity responseEntity = response.getEntity();
                    String responseBody = EntityUtils.toString(responseEntity);
                    String totalTimeInWorkZoneStr = extractTotalTime(responseBody);

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                    LocalTime totalTimeInWorkZone = LocalTime.parse(totalTimeInWorkZoneStr, formatter);
                    LocalTime timeLeftToComplete6Hours = LocalTime.of(6, 0)
                            .minusHours(totalTimeInWorkZone.getHour())
                            .minusMinutes(totalTimeInWorkZone.getMinute());
                    LocalTime timeLeftToComplete8Hours = LocalTime.of(8, 0)
                            .minusHours(totalTimeInWorkZone.getHour())
                            .minusMinutes(totalTimeInWorkZone.getMinute());

                    // Add the remaining time to totalTimeInWorkZone
                    LocalTime completionTime6hours = LocalTime.now().plusHours(timeLeftToComplete6Hours.getHour())
                            .plusMinutes(timeLeftToComplete6Hours.getMinute());
                    LocalTime completionTime8hours = LocalTime.now().plusHours(timeLeftToComplete8Hours.getHour())
                            .plusMinutes(timeLeftToComplete8Hours.getMinute());

                    // Format the completion times without nanoseconds
                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                    String formattedCompletionTime6hours = completionTime6hours.format(outputFormatter);
                    String formattedCompletionTime8hours = completionTime8hours.format(outputFormatter);

                    return "" + formattedCompletionTime6hours +
                            "\n" + formattedCompletionTime8hours;
                }
                else {
                    // Handle non-OK response status
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception
        }

        // Return appropriate result
        return null;
    }
    private double offsetX;
    private double offsetY;
   @Override
    public void start(Stage primaryStage) {
        // Create a Stage for the text display on the top bar
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

        // Create the root layout as an AnchorPane
        AnchorPane root = new AnchorPane();

        // Create a Text node to display your text
        String empTime = empTimeCal("524"); // Get your text
        Text helloText = new Text(empTime);
        helloText.setFont(Font.font("Arial", 15)); // Set your desired font and size
        helloText.setFill(Color.WHITE); // Set your desired text color

        double padding = 5;
        AnchorPane.setLeftAnchor(helloText, padding);
        AnchorPane.setTopAnchor(helloText, padding);
        AnchorPane.setBottomAnchor(helloText, padding);
        AnchorPane.setRightAnchor(helloText, padding);

        root.getChildren().add(helloText);



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
//        helloText.setOnMouseClicked(event -> {
//           SettingsDisplay settingsDisplay = new SettingsDisplay();
//           settingsDisplay.showSettingsDisplay(event.getScreenX(), event.getScreenY(), empTime);
//       });
       // Create two icons (ImageViews) and position them
       ImageView icon1 = new ImageView(new Image("/input.png")); // Replace with the actual path to your icon image
       ImageView icon2 = new ImageView(new Image("/settings.png")); // Replace with the actual path to your icon image

       double iconSize = 30;
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

       root.getChildren().addAll(icon1, icon2);

       // Add event handler to show/hide icons on mouse click
       root.setOnMouseClicked(event -> {
           if (helloText.isVisible()) {
               helloText.setVisible(false);
               icon1.setVisible(true);
               icon2.setVisible(true);
           } else {
               helloText.setVisible(true);
               icon1.setVisible(false);
               icon2.setVisible(false);
           }
       });

       Scene scene = new Scene(root);

        scene.setFill(null);

        topBarStage.setScene(scene);

        icon1.setOnMouseClicked(event -> {
            System.out.println("onMouseClicked icon 1");
           showWebView(topBarStage, "/input.html");
       });

       icon2.setOnMouseClicked(event -> {
           System.out.println("onMouseClicked icon 2");
           showWebView(topBarStage, "/input.html");
       });

        topBarStage.show();

        primaryStage.setOnCloseRequest(event -> {
            topBarStage.close();
            System.exit(0);
        });
    }
    private void showWebView(Stage stage, String htmlFilePath) {
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.load(getClass().getResource(htmlFilePath).toExternalForm());

        AnchorPane anchorPane = new AnchorPane(webView);
        Scene webViewScene = new Scene(anchorPane);

        stage.setScene(webViewScene);
    }

    public static void main(String[] args) throws IOException {
//        Runtime runtime = Runtime.getRuntime();
//        runtime.exec("echo '1234' | sudo systemctl poweroff");
        launch(args);
    }
}
