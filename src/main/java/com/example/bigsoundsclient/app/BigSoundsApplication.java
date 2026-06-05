package com.example.bigsoundsclient.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BigSoundsApplication extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.setTitle("BigSounds Client");
        showLogin();
        stage.show();
    }

    public static void showLogin() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                BigSoundsApplication.class.getResource("/com/example/bigsoundsclient/login-view.fxml"));
        primaryStage.setScene(new Scene(loader.load(), 520, 420));
        primaryStage.setResizable(false);
    }

    public static void showRegister() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                BigSoundsApplication.class.getResource("/com/example/bigsoundsclient/register-view.fxml"));
        primaryStage.setScene(new Scene(loader.load(), 520, 520));
        primaryStage.setResizable(false);
    }

    public static void showMain() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                BigSoundsApplication.class.getResource("/com/example/bigsoundsclient/main-view.fxml"));
        primaryStage.setScene(new Scene(loader.load(), 1050, 700));
        primaryStage.setResizable(true);
    }
}
