package com.example.bigsoundsclient;

import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class AccountController extends TabController implements Initializable {

    @FXML private VBox profileBox;
    @FXML private TextArea accountResponse;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadProfile();
    }

    private void loadProfile() {
        async(() -> ApiClient.get("/api/users/me"), res -> {
            profileBox.getChildren().clear();
            if (res.ok() && res.data().isJsonObject()) {
                JsonObject o = res.data().getAsJsonObject();
                addRow("ID",         str(o, "id"));
                addRow("Użytkownik", str(o, "name"));
                addRow("Spotify ID", str(o, "streaming_id"));
                addRow("Dołączył/a", dateStr(o, "date_joined"));
            }
        });
    }

    private void addRow(String key, String value) {
        HBox row = new HBox(12);
        Label k = new Label(key + ":");
        k.setStyle("-fx-text-fill: #777; -fx-font-size: 13px; -fx-min-width: 110;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill: #111; -fx-font-size: 13px; -fx-font-weight: bold;");
        row.getChildren().addAll(k, v);
        profileBox.getChildren().add(row);
    }

    @FXML
    private void handleRefreshSpotify() {
        accountResponse.setText("Odświeżanie…");
        async(() -> ApiClient.post("/api/users/spotify/refresh", null),
              res -> accountResponse.setText(res.data().toString()));
    }

    @FXML
    private void handleSyncStreams() {
        accountResponse.setText("Synchronizowanie…");
        async(() -> ApiClient.post("/api/users/sync-streams", null),
              res -> accountResponse.setText(res.data().toString()));
    }
}
