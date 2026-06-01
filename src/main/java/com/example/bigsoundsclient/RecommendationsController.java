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
                strCol("Tytuł",    o -> str(o, "title"),          260),
                strCol("Artyści",  o -> artists(o),               220),
                durationCol("Czas","duration_ms",                  75),
                scoreCol("Avg",    "avg_score",                    60)
        );
        applyStyle(recsTable);

        recsTable.setRowFactory(tv -> {
            var row = new javafx.scene.control.TableRow<JsonObject>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    DetailDialog.showSong(row.getItem());
                }
            });
            return row;
        });
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
}
