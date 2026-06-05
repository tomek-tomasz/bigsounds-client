module com.example.bigsoundsclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires java.desktop;
    requires com.google.gson;

    opens com.example.bigsoundsclient.app      to javafx.fxml;
    opens com.example.bigsoundsclient.api      to javafx.fxml;
    opens com.example.bigsoundsclient.ui.base  to javafx.fxml;
    opens com.example.bigsoundsclient.ui.auth  to javafx.fxml;
    opens com.example.bigsoundsclient.ui.main  to javafx.fxml;
    opens com.example.bigsoundsclient.ui.tabs  to javafx.fxml;

    exports com.example.bigsoundsclient.app;
    exports com.example.bigsoundsclient.api;
    exports com.example.bigsoundsclient.ui.base;
    exports com.example.bigsoundsclient.ui.auth;
    exports com.example.bigsoundsclient.ui.main;
    exports com.example.bigsoundsclient.ui.tabs;
}
