module com.example.demo1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.desktop; // Add this line to require the java.desktop module
//    requires htmlunit; // Add the HtmlUnit library here

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires org.jsoup;
    requires org.json;

    opens com.example.demo1 to javafx.fxml;
    exports com.example.demo1;
}