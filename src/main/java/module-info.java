module com.example.bigsoundsclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;

    opens com.example.bigsoundsclient to javafx.fxml;
    exports com.example.bigsoundsclient;
}
