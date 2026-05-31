package com.example.bigsoundsclient.controllers;

import com.example.bigsoundsclient.ApiClient;
import com.example.bigsoundsclient.AuthState;
import com.example.bigsoundsclient.BigSoundsApplication;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;

public class MainController implements Initializable {

    @FXML private Label userLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        CompletableFuture.supplyAsync(() -> ApiClient.get("/api/users/me"))
                .thenAccept(res -> Platform.runLater(() -> {
                    if (res.ok() && res.data().isJsonObject()) {
                        String name = res.data().getAsJsonObject().get("name").getAsString();
                        userLabel.setText("Zalogowany: " + name);
                    }
                }));
    }

    @FXML
    private void handleLogout() {
        AuthState.getInstance().clear();
        try { BigSoundsApplication.showLogin(); } catch (Exception ignored) {}
    }
}
