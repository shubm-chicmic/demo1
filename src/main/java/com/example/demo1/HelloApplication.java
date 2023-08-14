package com.example.demo1;

import javafx.application.Platform;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

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
//    @Override
//    public void start(Stage primaryStage) {
//        // Create a label to display information
//        Label infoLabel = new Label(empTimeCal("574")); // Replace with your data
//
//        // Set the label's text color based on background color
//        infoLabel.setTextFill(Color.WHITE); // Assuming you want white text
//        infoLabel.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
//
//        // Create a transparent stage without decorations
//        primaryStage.initStyle(StageStyle.TRANSPARENT);
//
//        // Create a stack pane to hold the label
//        StackPane root = new StackPane(infoLabel);
//
//        Scene scene = new Scene(root, 300, 50);
//
//        // Make the scene background transparent
//        scene.setFill(null);
//
//        primaryStage.setScene(scene);
//
//        // Set the stage to be always on top
//        primaryStage.setAlwaysOnTop(true);
//
//        // Position the stage at the top of the screen
//        double screenHeight = Screen.getPrimary().getVisualBounds().getMinY();
//        primaryStage.setY(screenHeight);
//
//        // Show the stage
//        primaryStage.show();
//
//        // Close the application when the stage is closed
//        primaryStage.setOnCloseRequest(event -> {
//            Platform.exit();
//            System.exit(0);
//        });
//    }
private double offsetX;
    private double offsetY;
    boolean isHoverBoxVisible = false;
    boolean isFlipped = false;

    @Override
    public void start(Stage primaryStage) {
        // Create a Stage for the text display on the top bar
        Stage topBarStage = new Stage();
        topBarStage.initStyle(StageStyle.TRANSPARENT);
        topBarStage.setAlwaysOnTop(true);

        // Create the root layout as an AnchorPane
        AnchorPane root = new AnchorPane();

        // Create a Text node to display your text
        String empTime = empTimeCal("574"); // Get your text
        Text helloText = new Text(empTime);
        helloText.setFont(Font.font("Arial", 15)); // Set your desired font and size
        helloText.setFill(Color.WHITE); // Set your desired text color

        // Create a transparent background rectangle to hold the text
        double padding = 10;
        AnchorPane.setLeftAnchor(helloText, padding);
        AnchorPane.setTopAnchor(helloText, padding);

        // Add the text to the root layout
        root.getChildren().add(helloText);
        ImageView settingsIcon = new ImageView(new Image(getClass().getResourceAsStream("/settings.png")));
        ImageView inputIcon = new ImageView(new Image(getClass().getResourceAsStream("/input.png")));
        settingsIcon.setFitWidth(20);
        settingsIcon.setFitHeight(20);
        inputIcon.setFitWidth(20);
        inputIcon.setFitHeight(20);

        // Create a VBox to hold the icons
        VBox iconContainer = new VBox(settingsIcon, inputIcon);
        iconContainer.setSpacing(10);

        // Create a StackPane to hold the text and icons
        StackPane textAndIconsPane = new StackPane();
        textAndIconsPane.getChildren().addAll(helloText, iconContainer);

        // Create a FlipPane for the flip animation
        FlipPane flipPane = new FlipPane(root, textAndIconsPane);
//        flipPane.setFlipDuration(Duration.millis(500)); // Set the flip animation duration

        // Add the flip pane to the root layout
        root.getChildren().add(flipPane);
        // Create a semi-transparent curved box for hover effect
        Rectangle hoverBox = new Rectangle();
        hoverBox.setArcWidth(10);
        hoverBox.setArcHeight(10);
        hoverBox.setFill(Color.rgb(255, 255, 255, 0.5)); // Semi-transparent white
        hoverBox.setStroke(Color.WHITE); // White border
        hoverBox.setStrokeWidth(9); // Border width
        hoverBox.setVisible(false); // Initially hidden

        // Show the hover box on hover
        helloText.setOnMouseEntered(event -> {
            if (!isHoverBoxVisible && !isFlipped) {
                double hoverBoxX = helloText.getBoundsInParent().getMinX() - padding;
                double hoverBoxY = helloText.getBoundsInParent().getMinY() - padding;
                hoverBox.setWidth(helloText.getBoundsInParent().getWidth() + padding * 2);
                hoverBox.setHeight(helloText.getBoundsInParent().getHeight() + padding * 2);
                hoverBox.setLayoutX(hoverBoxX);
                hoverBox.setLayoutY(hoverBoxY);
                hoverBox.setVisible(true);
                isHoverBoxVisible = true;
            }
        });

        // Hide the hover box when the mouse exits
        helloText.setOnMouseExited(event -> {
            hoverBox.setVisible(false);
            isHoverBoxVisible = false;
        });

        // Handle click on the text to flip the pane
        helloText.setOnMouseClicked(event -> {
            if (!isFlipped) {
                flipPane.flip();
                isFlipped = true;
            }
        });

        // Handle click on the input icon
        inputIcon.setOnMouseClicked(event -> {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Input Dialog");
                    dialog.setHeaderText("Enter a value:");
                    dialog.setContentText("Value:");

                    // Style the dialog
                    DialogPane dialogPane = dialog.getDialogPane();
                    dialogPane.setStyle("-fx-background-color: black; -fx-text-fill: white;");

                    Optional<String> result = dialog.showAndWait();
                    result.ifPresent(value -> System.out.println("Value entered: " + value));
                });

        // Set the dimensions of the hover box based on the text bounds
        helloText.boundsInLocalProperty().addListener((observable, oldValue, newValue) -> {
            double textWidth = newValue.getWidth();
            double textHeight = newValue.getHeight();

            hoverBox.setWidth(textWidth + 20 * 2);
            hoverBox.setHeight(textHeight + 20 * 2);
        });

        // Show the hover box on hover
        helloText.setOnMouseEntered(event -> {
            System.out.println("Mouse is entering");
            double hoverBoxX = helloText.getLayoutX() - 20;
            double hoverBoxY = helloText.getLayoutY() - 20;
            hoverBox.setLayoutX(hoverBoxX);
            hoverBox.setLayoutY(hoverBoxY);
            hoverBox.setVisible(true);
        });

        // Hide the hover box when the mouse exits
        helloText.setOnMouseExited(event -> hoverBox.setVisible(false));

        // Set the draggable behavior
        root.setOnMousePressed(event -> {
            offsetX = event.getSceneX();
            offsetY = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            topBarStage.setX(event.getScreenX() - offsetX);
            topBarStage.setY(event.getScreenY() - offsetY);
        });

        // Add the hover box to the root layout
        root.getChildren().add(hoverBox);

        // Create a Scene with the AnchorPane as the root
        Scene scene = new Scene(root); // Auto-adjust dimensions to fit content

        // Make the scene background transparent
        scene.setFill(null);

        // Set the scene for the top bar Stage
        topBarStage.setScene(scene);

        // Show the top bar Stage
        topBarStage.show();

        // Close the application when the primary stage is closed
        primaryStage.setOnCloseRequest(event -> {
            topBarStage.close();
            Platform.exit();
            System.exit(0);
        });
    }




    public static void main(String[] args) throws IOException {
//        Runtime runtime = Runtime.getRuntime();
//        runtime.exec("echo '1234' | sudo systemctl poweroff");
        launch(args);
    }
}
