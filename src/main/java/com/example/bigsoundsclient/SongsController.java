package com.example.bigsoundsclient;

import com.google.gson.JsonObject;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class SongsController extends PagedTabController implements Initializable {

    @FXML private TableView<JsonObject> songsTable;
    @FXML private HBox paginationBar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        songsTable.getColumns().addAll(
                strCol("ID",         o -> str(o, "id"),             50),
                strCol("Tytuł",      o -> str(o, "title"),          200),
                strCol("Artyści",    o -> artists(o),               170),
                strCol("Czas",       o -> fmtMs(o, "duration_ms"),   70),
                strCol("Avg",        o -> fmtScore(o, "avg_score"),   60),
                strCol("Moja",       o -> fmtScore(o, "my_score"),    55),
                likeToggleCol("/api/likes/songs/", "/api/likes/songs/", this::refresh)
        );
        applyStyle(songsTable);
        paginationBar.getChildren().setAll(buildPaginationBar().getChildren());

        songsTable.setRowFactory(tv -> {
            var row = new javafx.scene.control.TableRow<JsonObject>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    DetailDialog.showSong(row.getItem());
                }
            });
            return row;
        });
    }

    @Override
    protected void loadPage(int page, int limit) {
        async(() -> ApiClient.get("/api/songs?page=" + page + "&limit=" + limit),
              res -> applyPage(res.data()));
    }

    @Override
    protected void updateTable(ObservableList<JsonObject> items) {
        songsTable.setItems(items);
    }

    private String fmtScore(JsonObject o, String key) {
        if (!o.has(key) || o.get(key).isJsonNull()) return "—";
        try { return String.format("%.0f", o.get(key).getAsDouble()); }
        catch (Exception e) { return "—"; }
    }
}
