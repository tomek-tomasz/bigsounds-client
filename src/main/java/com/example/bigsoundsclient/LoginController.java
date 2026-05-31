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
                    String token = res.data().getAsJsonObject().get("token").getAsString();
                    AuthState.getInstance().setToken(token);
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
        }));
    }

    @FXML
    private void handleRegister() {
        String name = nameField.getText().trim();
        String password = passwordField.getText();
        if (name.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Wypełnij oba pola.");
            return;
        }
        setLoading(true);
        CompletableFuture.supplyAsync(() ->
                ApiClient.post("/api/users/register", Map.of("name", name, "password", password, "spotifyCode", ""))
        ).thenAccept(res -> Platform.runLater(() -> {
            setLoading(false);
            if (res.ok()) {
                errorLabel.setStyle("-fx-text-fill: green;");
                errorLabel.setText("Zarejestrowano! Możesz się zalogować.");
            } else {
                errorLabel.setStyle("-fx-text-fill: #e53e3e;");
                String msg = "Rejestracja nieudana.";
                try { msg = res.data().getAsJsonObject().get("error").getAsString(); } catch (Exception ignored) {}
                errorLabel.setText(msg);
            }
        }));
    }

    private void setLoading(boolean loading) {
        loginBtn.setDisable(loading);
        registerBtn.setDisable(loading);
    }
}
