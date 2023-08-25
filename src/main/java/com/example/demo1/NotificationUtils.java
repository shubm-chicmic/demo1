package com.example.demo1;

import javafx.animation.PauseTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class NotificationUtils {

    static void showNotification(String message) {
        Stage notificationStage = new Stage();
        notificationStage.initStyle(StageStyle.TRANSPARENT); // Use StageStyle.TRANSPARENT to have a completely transparent stage

        VBox notificationBox = new VBox();
        notificationBox.setAlignment(Pos.CENTER);

        Text notificationText = new Text(message);
        notificationText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        notificationText.setFill(Color.WHITE);

        Scene scene = new Scene(notificationBox);
        scene.setFill(Color.TRANSPARENT); // Set the scene's background color to transparent
        notificationStage.setScene(scene);

        notificationBox.getChildren().add(notificationText);
//
//        Bounds mainRootBounds = mainRoot.getScene().getRoot().localToScreen(mainRoot.getScene().getRoot().getBoundsInLocal());

        double notificationX = 1370;
        double notificationY = 780;
//
        notificationStage.setX(notificationX);
        notificationStage.setY(notificationY);
        notificationStage.show();

        // Close the notification stage after a delay
        PauseTransition delay = new PauseTransition(Duration.seconds(3)); // Adjust the delay as needed
        delay.setOnFinished(event -> notificationStage.close());
        delay.play();
    }
}

