module com.example.bigsoundsclient {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.bigsoundsclient to javafx.fxml;
    exports com.example.bigsoundsclient;
}