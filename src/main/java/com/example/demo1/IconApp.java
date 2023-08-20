package com.example.demo1;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class IconApp extends Application {
    private Stage htmlViewStage;
    private WebView webView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Create ImageView for the icon
        ImageView iconImageView = new ImageView(new Image("icon.png"));
        iconImageView.setFitWidth(50);
        iconImageView.setFitHeight(50);

        // Create layout for the icon
        StackPane iconLayout = new StackPane(iconImageView);
        iconLayout.setStyle("-fx-background-color: transparent;");
        iconLayout.setOnMouseClicked(event -> openHtmlView());

        // Set up the scene for the icon
        Scene iconScene = new Scene(iconLayout, 50, 50);

        // Set up the stage for the icon
        primaryStage.setScene(iconScene);
        primaryStage.setTitle("Icon");
        primaryStage.show();

        // Load HTML content into the WebView
        webView = new WebView();
        webView.getEngine().loadContent("<html><body><form>"
                + "Student Name: <input type='text' name='studentName'><br>"
                + "<input type='submit' value='Submit'>"
                + "</form></body></html>");

        // Set up the stage for the HTML view
        htmlViewStage = new Stage();
        htmlViewStage.setTitle("HTML View");
        htmlViewStage.setScene(new Scene(webView, 400, 300));

        // Close HTML view on pressing Enter
        htmlViewStage.getScene().setOnKeyPressed(event -> {
            if (event.getCode().getName().equals("Enter")) {
                closeHtmlView();
            }
        });
    }

    private void openHtmlView() {
        htmlViewStage.show();
        webView.setVisible(true);
    }

    private void closeHtmlView() {
        htmlViewStage.hide();
        webView.setVisible(false);
    }
}
