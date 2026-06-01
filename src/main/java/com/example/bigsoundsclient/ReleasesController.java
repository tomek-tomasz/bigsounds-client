package com.example.bigsoundsclient;

import com.google.gson.JsonObject;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class ReleasesController extends PagedTabController implements Initializable {

    @FXML private TableView<JsonObject> releasesTable;
    @FXML private HBox paginationBar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        releasesTable.getColumns().addAll(
                strCol("ID",         o -> str(o, "id"),                  50),
                strCol("Tytuł",      o -> str(o, "title"),               180),
                strCol("Artyści",    o -> artists(o),                    150),
                strCol("Format",     o -> str(o, "format"),               70),
                strCol("Data",       o -> dateStr(o, "release_date"),      90),
                strCol("Avg",        o -> fmtScore(o, "avg_score"),         60),
                strCol("Moja",       o -> fmtScore(o, "my_score"),          55),
                likeToggleCol("/api/likes/releases/", "/api/likes/releases/", this::refresh)
        );
        applyStyle(releasesTable);
        paginationBar.getChildren().setAll(buildPaginationBar().getChildren());

        releasesTable.setRowFactory(tv -> {
            var row = new javafx.scene.control.TableRow<JsonObject>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    DetailDialog.showRelease(row.getItem());
                }
            });
            return row;
        });
    }

    @Override
    protected void loadPage(int page, int limit) {
        async(() -> ApiClient.get("/api/releases?page=" + page + "&limit=" + limit),
              res -> applyPage(res.data()));
    }

    @Override
    protected void updateTable(ObservableList<JsonObject> items) {
        releasesTable.setItems(items);
    }

    private String fmtScore(JsonObject o, String key) {
        if (!o.has(key) || o.get(key).isJsonNull()) return "—";
        try { return String.format("%.0f", o.get(key).getAsDouble()); }
        catch (Exception e) { return "—"; }
    }
}
