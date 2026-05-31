module com.example.bigsoundsclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;

    opens com.example.bigsoundsclient to javafx.fxml;
    exports com.example.bigsoundsclient;
    exports com.example.bigsoundsclient.controllers;
    opens com.example.bigsoundsclient.controllers to javafx.fxml;
}
