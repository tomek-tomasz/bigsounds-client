package com.example.bigsoundsclient;

import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Map;
import java.util.concurrent.CompletableFuture;


public class LoginController {

    @FXML private TextField nameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginBtn;
    @FXML private Button registerBtn;

    @FXML
    private void handleLogin() {
        String name = nameField.getText().trim();
        String password = passwordField.getText();
        if (name.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Wypełnij oba pola.");
            return;
        }
        setLoading(true);
        CompletableFuture.supplyAsync(() ->
                ApiClient.post("/api/users/login", Map.of("name", name, "password", password))
        ).thenAccept(res -> Platform.runLater(() -> {
            setLoading(false);
            if (res.ok()) {
                try {
                    JsonObject body = res.data().getAsJsonObject();
                    AuthState.getInstance().setToken(body.get("token").getAsString());
                    if (body.has("user") && body.get("user").isJsonObject()) {
                        AuthState.getInstance().setUserId(
                                body.get("user").getAsJsonObject().get("id").getAsInt());
                    }
                } catch (Exception e) {
                    errorLabel.setText("Błąd parsowania tokenu: " + e.getMessage());
                    return;
                }
                try {
                    BigSoundsApplication.showMain();
                } catch (Exception e) {
                    errorLabel.setText("Błąd otwarcia okna: " + e.getMessage());
                }
            } else {
                String msg = "Logowanie nieudane. Status: " + res.status();
                try { msg = res.data().getAsJsonObject().get("error").getAsString(); } catch (Exception ignored) {}
                errorLabel.setText(msg);
            }
        })).exceptionally(e -> {
            Platform.runLater(() -> {
                setLoading(false);
                errorLabel.setText("Błąd połączenia: " + e.getMessage());
            });
            return null;
        });
    }

    @FXML
    private void handleRegister() {
        try {
            BigSoundsApplication.showRegister();
        } catch (Exception e) {
            errorLabel.setText("Błąd otwarcia rejestracji: " + e.getMessage());
        }
    }

    private void setLoading(boolean loading) {
        loginBtn.setDisable(loading);
        registerBtn.setDisable(loading);
    }
}
