package com.example.demo1;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
public class FlipBox extends StackPane {
    private Rectangle front;
    private Rectangle back;

    public FlipBox(String text) {
        // Create front rectangle with text
        front = createRectangle(text);
        front.setRotate(0);

        // Create back rectangle with "Hello World" text
        back = createRectangle("Hello World");
        back.setRotate(-180);
        back.setVisible(false); // Hide the back content initially

        // Add front and back rectangles to the StackPane
        getChildren().addAll(front, back);
    }

    private Rectangle createRectangle(String text) {
        Rectangle rectangle = new Rectangle(200, 100, Color.LIGHTGRAY);
        Text content = new Text(text);
        content.setFont(Font.font("Arial", 15));
        StackPane.setAlignment(content, Pos.CENTER);
        getChildren().add(content); // Add the content directly to the StackPane
        return rectangle;
    }

    public void flip() {
        ScaleTransition scaleFront = new ScaleTransition(Duration.seconds(1), front);
        scaleFront.setToX(0);
        scaleFront.setInterpolator(Interpolator.LINEAR);

        ScaleTransition scaleBack = new ScaleTransition(Duration.seconds(1), back);
        scaleBack.setToX(1);
        scaleBack.setInterpolator(Interpolator.LINEAR);

        scaleFront.setOnFinished(event -> {
            front.setVisible(false);
            back.setVisible(true);
            scaleBack.play();
        });

        scaleBack.setOnFinished(event -> {
            back.setVisible(false);
            front.setVisible(true);
        });

        scaleFront.play();
    }

    // Method to explicitly set the visibility of the back content
    public void setBackVisible(boolean visible) {
        back.setVisible(visible);
    }
}
