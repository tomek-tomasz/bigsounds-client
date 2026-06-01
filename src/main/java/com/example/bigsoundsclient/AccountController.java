package com.example.bigsoundsclient;

import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class AccountController extends TabController implements Initializable {

    @FXML private VBox profileBox;
    @FXML private TextArea accountResponse;
    @FXML private TableView<JsonObject> reviewsTable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reviewsTable.getColumns().addAll(
                strCol("Typ",     o -> fmtType(o),        80),
                strCol("Tytuł",   o -> str(o, "item_title"), 230),
                scoreCol("Ocena", "score", 90),
                strCol("Data",    o -> dateStr(o, "date_added"), 100),
                actionCol("🗑 Usuń", o -> deleteReview(o), 80)
        );
        applyStyle(reviewsTable);
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
        loadReviews();
    }

    private void loadReviews() {
        async(() -> ApiClient.get("/api/reviews/mine"),
              res -> reviewsTable.setItems(toList(res.data())));
    }

    private void deleteReview(JsonObject review) {
        String type   = str(review, "type");
        int    itemId = review.get("item_id").getAsInt();
        String endpoint = "song".equals(type)
                ? "/api/reviews/songs/"   + itemId
                : "/api/reviews/releases/" + itemId;
        async(() -> ApiClient.delete(endpoint), r -> loadReviews());
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

    private String fmtType(JsonObject o) {
        return "song".equals(str(o, "type")) ? "🎵 Utwór" : "💿 Wydanie";
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

    @Override
    public void refresh() { loadProfile(); }
}
