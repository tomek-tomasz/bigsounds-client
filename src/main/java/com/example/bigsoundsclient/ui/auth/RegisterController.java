package com.example.bigsoundsclient.ui.auth;

import com.example.bigsoundsclient.api.ApiClient;
import com.example.bigsoundsclient.api.SpotifyCallbackServer;
import com.example.bigsoundsclient.app.BigSoundsApplication;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.awt.Desktop;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label statusLabel;
    @FXML private Button connectBtn;
    @FXML private Button backBtn;

    private SpotifyCallbackServer callbackServer;

    @FXML
    private void handleConnect() {
        String name = nameField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (name.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showError("Wypełnij wszystkie pola.");
            return;
        }
        if (name.length() < 3) {
            showError("Nazwa użytkownika musi mieć co najmniej 3 znaki.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Hasła nie są zgodne.");
            return;
        }

        setLoading(true);
        showInfo("Otwieranie Spotify w przeglądarce... Autoryzuj dostęp i wróć.");

        callbackServer = new SpotifyCallbackServer(
            code -> completeRegistration(name, password, code),
            () -> Platform.runLater(() -> {
                showError("Autoryzacja Spotify nieudana lub minął czas oczekiwania (2 min).");
                setLoading(false);
            })
        );
        Thread serverThread = new Thread(callbackServer, "spotify-callback-server");
        serverThread.setDaemon(true);
        serverThread.start();

        SpotifyCallbackServer serverRef = callbackServer;
        CompletableFuture.runAsync(() -> {
            try {
                serverRef.waitUntilReady();
                Desktop.getDesktop().browse(new URI(ApiClient.BASE_URL + "/api/users/spotify/connect"));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Nie można otworzyć przeglądarki: " + e.getMessage());
                    setLoading(false);
                    serverRef.stop();
                });
            }
        });
    }

    private void completeRegistration(String name, String password, String spotifyCode) {
        Platform.runLater(() -> showInfo("Rejestrowanie konta..."));

        ApiClient.ApiResponse res = ApiClient.post("/api/users/register",
                Map.of("name", name, "password", password, "spotifyCode", spotifyCode));

        Platform.runLater(() -> {
            setLoading(false);
            if (res.ok()) {
                showSuccess("Konto utworzone! Możesz się teraz zalogować.");
                connectBtn.setText("Zarejestrowano ✓");
                connectBtn.setDisable(true);
            } else {
                String msg = "Rejestracja nieudana.";
                try { msg = res.data().getAsJsonObject().get("error").getAsString(); } catch (Exception ignored) {}
                showError(msg);
            }
        });
    }

    @FXML
    private void handleBack() {
        if (callbackServer != null) callbackServer.stop();
        try {
            BigSoundsApplication.showLogin();
        } catch (Exception e) {
            showError("Błąd nawigacji: " + e.getMessage());
        }
    }

    private void setLoading(boolean loading) {
        connectBtn.setDisable(loading);
        nameField.setDisable(loading);
        passwordField.setDisable(loading);
        confirmPasswordField.setDisable(loading);
    }

    private void showError(String msg) {
        statusLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 12px;");
        statusLabel.setText(msg);
    }

    private void showInfo(String msg) {
        statusLabel.setStyle("-fx-text-fill: #2b6cb0; -fx-font-size: 12px;");
        statusLabel.setText(msg);
    }

    private void showSuccess(String msg) {
        statusLabel.setStyle("-fx-text-fill: #276749; -fx-font-size: 12px;");
        statusLabel.setText(msg);
    }
}
