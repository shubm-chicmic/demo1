package com.example.demo1;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class HtmlDialog {

    private final WebView webView;
    private final Stage dialogStage;
    private boolean enterPressed = false;
    private String empCode = "";
    private String timeField = "";
    public HtmlDialog(String htmlFilePath) {
        webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.load(getClass().getResource(htmlFilePath).toExternalForm());

        // Create a StackPane to hold the WebView
        StackPane stackPane = new StackPane(webView);
        stackPane.setStyle("-fx-background-color: transparent;"); // Set transparent background

        // Set the background color of the StackPane to transparent
        stackPane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));

        // Create the dialog stage
        dialogStage = new Stage(StageStyle.UNDECORATED);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.setScene(new Scene(stackPane, Region.USE_PREF_SIZE, Region.USE_PREF_SIZE));
    }




    public void close() {
        dialogStage.close();
    }
    // Inside the HtmlDialog class

    public void setDialogSize(double width, double height) {
        dialogStage.setWidth(width);
        dialogStage.setHeight(height);
    }

    public void setDialogContent(Scene content) {
        dialogStage.setScene(content);
    }

    public void setDialogPosition(double x, double y) {
        dialogStage.setX(x);
        dialogStage.setY(y);
    }
    public void setEnterPressed(boolean value) {
        enterPressed = value;
    }
    public boolean isEnterPressed() {
        return enterPressed;
    }

    public String getEmpCode() {
        empCode = webView.getEngine().executeScript("document.getElementById('employeeCode').value").toString();;
        return empCode;
    }

    public String getTimeField() {
        timeField = webView.getEngine().executeScript("document.getElementById('timeField').value").toString();
        return timeField;
    }
    private void setupEnterKeyPressHandling() {
        webView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                enterPressed = true;
                event.consume();
                close();
            }
        });
    }

    public void showAndWait() {
        setupEnterKeyPressHandling();
        dialogStage.showAndWait();
    }


}
