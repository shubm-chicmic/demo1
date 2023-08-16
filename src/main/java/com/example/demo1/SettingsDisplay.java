package com.example.demo1;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SettingsDisplay {

    private double offsetX, offsetY;

    public void showSettingsDisplay(double x, double y, String empTime) {
        Stage settingsStage = new Stage();
        settingsStage.initStyle(StageStyle.TRANSPARENT);

        AnchorPane root = new AnchorPane();

        Text helloText = new Text(empTime);
        helloText.setFont(Font.font("Arial", 15));
        helloText.setFill(Color.WHITE);

        double padding = 5;
        double borderThickness = 1.5;

        StackPane textStackPane = new StackPane();
        textStackPane.getChildren().add(helloText);
        textStackPane.setPadding(new javafx.geometry.Insets(padding));
        textStackPane.setStyle("-fx-border-color: white; -fx-border-width: " + borderThickness + "px; -fx-border-radius: 10px;");

        AnchorPane.setLeftAnchor(textStackPane, padding + borderThickness);
        AnchorPane.setTopAnchor(textStackPane, padding + borderThickness);
        AnchorPane.setBottomAnchor(textStackPane, padding + borderThickness);
        AnchorPane.setRightAnchor(textStackPane, padding + borderThickness);

        root.getChildren().add(textStackPane);

        root.setOnMousePressed(event -> {
            offsetX = event.getSceneX();
            offsetY = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            settingsStage.setX(event.getScreenX() - offsetX);
            settingsStage.setY(event.getScreenY() - offsetY);
        });

        Scene scene = new Scene(root);
        scene.setFill(null);

        settingsStage.setScene(scene);
        settingsStage.setX(x - 50); // Adjust as needed
        settingsStage.setY(y - 50); // Adjust as needed
        settingsStage.showAndWait();
    }
}
