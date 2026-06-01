package com.example.bigsoundsclient;

import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class RecommendationsController extends TabController implements Initializable {

    @FXML private TableView<JsonObject> recsTable;
    @FXML private Button refreshRecsBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        recsTable.getColumns().addAll(
                strCol("Tytuł",         o -> str(o, "title"),          220),
                strCol("Artyści",       o -> artists(o),               200),
                strCol("Czas",          o -> fmtMs(o, "duration_ms"),   80),
                strCol("Avg score",     o -> fmtScore(o),               80),
                strCol("Dopasowanie",   o -> str(o, "recommendation_score"), 90),
                actionCol("♥ Polub", o -> {
                    int id = o.get("id").getAsInt();
                    async(() -> ApiClient.post("/api/likes/songs/" + id, null), r -> {});
                }, 90)
        );
        applyStyle(recsTable);
    }

    @FXML
    private void loadRecommendations() {
        refreshRecsBtn.setDisable(true);
        async(() -> ApiClient.get("/api/recommendations?limit=50"), res -> {
            refreshRecsBtn.setDisable(false);
            recsTable.setItems(toList(res.data()));
        });
    }

    @Override
    public void refresh() { loadRecommendations(); }

    private String fmtScore(JsonObject o) {
        if (!o.has("avg_score") || o.get("avg_score").isJsonNull()) return "—";
        try { return String.format("%.0f", o.get("avg_score").getAsDouble()); }
        catch (Exception e) { return "—"; }
    }
}
