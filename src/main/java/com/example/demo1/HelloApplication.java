package com.example.demo1;

import com.example.demo1.AutoUpdate.AutoUpdateManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Scene;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

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
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.time.format.DateTimeParseException;
import java.util.concurrent.atomic.AtomicReference;

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
    public static boolean autoFillTimeSheet = false;
    public static boolean autoPCTurnOff = false;
    public boolean autoUpdateTime = false;
    public String[] timeField = {"8"};
    public String[] empCode = {"574"};
    private boolean isDialogVisible = false;
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
        boolean[] isIcon3Visible = {false};
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
            if(!isIcon3Visible[0])
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
        ImageView icon3 = new ImageView(new Image("/alert.gif")); // Replace with the actual path to your icon image

        double iconSize = 25;
        icon1.setFitWidth(iconSize);
        icon1.setFitHeight(iconSize);
        icon2.setFitWidth(iconSize);
        icon2.setFitHeight(iconSize);
        icon3.setFitWidth(iconSize + 10);
        icon3.setFitHeight(iconSize + 10);



        AnchorPane.setLeftAnchor(icon1, padding + borderThickness);
        AnchorPane.setTopAnchor(icon1, padding + borderThickness);

        AnchorPane.setRightAnchor(icon2, padding + borderThickness);
        AnchorPane.setTopAnchor(icon2, padding + borderThickness);

        AnchorPane.setTopAnchor(icon3, (root.getHeight()) / 2); // Center vertically
        AnchorPane.setLeftAnchor(icon3, (root.getWidth()) / 2);

        // Hide the icons initially
        icon1.setVisible(false);
        icon2.setVisible(false);
        icon3.setVisible(false);

        icon1.setOnMouseClicked(event -> {
//            System.out.println("\u001B[33m " + timeField + " code : " + empCode[0] + " time : " + empTime );
           if(!isDialogVisible) {
               InputData inputData = new InputData();
               inputData.setTimeField(timeField[0]);
               inputData.setEmpCode(empCode[0]);
               JDialog jDialog = EmployeeForm.showForm(inputData);
               System.out.println("values in the main : " + inputData.getEmpCode() + " " + inputData.getTimeField());
               Bounds icon2Bounds = icon2.localToScreen(icon2.getBoundsInLocal());

               double formWidth = 400;
               double formHeight = 200;
               double formX = icon2Bounds.getMinX();
               double formY = icon2Bounds.getMinY() - 100;

               // Set the position for the form
               EmployeeForm.setFormPosition(formX, formY);
               jDialog.addWindowListener(new WindowAdapter() {
                   @Override
                   public void windowClosed(WindowEvent e) {
                       if(inputData.getEmpCode() != empCode[0] && inputData.getTimeField() != timeField[0]) {
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

           }else {
               EmployeeForm.hideForm();
           }
           isDialogVisible = !isDialogVisible;
        });
//  runner.setAutoUpdateTime(false);
        icon2.setOnMouseClicked(event -> {


        });
        icon3.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                helloText.setVisible(true);
                icon1.setVisible(false);
                icon2.setVisible(false);
                icon3.setVisible(false);
                iconsVisible = false;
                isIcon3Visible[0] = false;
            }
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
            }else if (event.getButton() == MouseButton.MIDDLE) {
                empTime.set(empTimeCal(empCode[0], timeField[0]));
                remainingTimeStr = remainingTime(empTime.get());

                if(timeVisible) {
                    helloText.setText(remainingTimeStr);
                }else {
                    helloText.setText(empTime.get());
                }
                NotificationUtils.showNotification("Refreshed....");

            }
            else {
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
                    icon3.setVisible(true);
                    helloText.setVisible(false);
                    icon1.setVisible(false);
                    icon2.setVisible(false);
                    iconsVisible = false;
                    isIcon3Visible[0] = true;
                }
            }
        };
        timer.scheduleAtFixedRate(conditionTask[0], 0, 5000);


        root.getChildren().addAll(icon1, icon2, icon3, helloText);

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

    public void updateUIComponents(String updatedEmpTime) {
        Platform.runLater(() -> {
            helloText.setText(updatedEmpTime);
            remainingTimeStr = remainingTime(updatedEmpTime);
            empTime.set(updatedEmpTime);

        });
    }
    private volatile boolean timerRunning = false; // Flag to indicate whether the timer is running
    private Thread countdownThread; // Thread for the countdown

    private void startTimer() {
        if (!timerRunning) {
            timerRunning = true;
            countdownThread = new Thread(() -> {
                while (!remainingTimeStr.equals("00:00:00") && timerRunning) {
                    try {
                        Thread.sleep(1000); // Sleep for 1 second
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Update UI components on the JavaFX Application thread
                    Platform.runLater(() -> {
                        remainingTimeStr = subtractOneSecondFromTime(remainingTimeStr);
                        helloText.setText(remainingTimeStr);
                    });
                }
                timerRunning = false;
            });
            countdownThread.start();
        }
    }

    private void stopTimer() {
        if (timerRunning) {
            timerRunning = false;
            try {
                countdownThread.join(); // Wait for the countdown thread to finish
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static String subtractOneSecondFromTime(String timeStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalTime time = LocalTime.parse(timeStr, formatter);
            time = time.minusSeconds(1);
            return time.format(formatter);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return "Invalid Time Format";
        }
    }


    public static void main(String[] args) throws IOException {
//        Runtime runtime = Runtime.getRuntime();
//        runtime.exec("echo '1234' | sudo systemctl poweroff");
        SystemTrayMenu systemTrayMenu = new SystemTrayMenu();
        systemTrayMenu.showSystemTray();
        launch(args);
    }
}
