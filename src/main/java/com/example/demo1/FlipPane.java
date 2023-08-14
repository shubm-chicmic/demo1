package com.example.demo1;
import javafx.animation.*;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class FlipPane extends javafx.scene.layout.StackPane {
    private static final Duration DURATION = Duration.millis(500);
    private final RotateTransition flip1, flip2;
    private final Interpolator interpolator;

    public FlipPane(Node front, Node back) {
        this(front, back, Interpolator.LINEAR);
    }

    public FlipPane(Node front, Node back, Interpolator interpolator) {
        this.interpolator = interpolator;

        getChildren().addAll(front, back);

        front.setOpacity(1.0);
        back.setOpacity(0.0);

        // Setup the flip animations
        flip1 = createFlipTransition(this, front, back, 0, 90);
        flip2 = createFlipTransition(this, back, front, -90, 0);
    }

    public void flip() {
        final boolean isFrontVisible = flip1.getNode().isVisible();

        final RotateTransition flipOut;
        final RotateTransition flipIn;

        if (isFrontVisible) {
            flipOut = flip1;
            flipIn = flip2;
        } else {
            flipOut = flip2;
            flipIn = flip1;
        }

        flipOut.setOnFinished(actionEvent -> {
            flipIn.play();
            flipIn.getNode().toFront();
        });

        flipOut.play();
    }

    private RotateTransition createFlipTransition(FlipPane flipPane, Node front, Node back, double fromAngle, double toAngle) {
        final RotateTransition flip = new RotateTransition(DURATION, flipPane);
        flip.setInterpolator(interpolator);
        flip.setAxis(Rotate.Y_AXIS);
        flip.setFromAngle(fromAngle);
        flip.setToAngle(toAngle);
        flip.setRate(1);
        flip.setDelay(Duration.ZERO);
        flip.setNode(flipPane);

        flip.setOnFinished(actionEvent -> {
            if (toAngle == 0) {
                back.setOpacity(0.0);
                front.setOpacity(1.0);
            } else {
                front.setOpacity(0.0);
                back.setOpacity(1.0);
            }
        });

        return flip;
    }

}

