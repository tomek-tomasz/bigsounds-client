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
                strCol("ID",      o -> str(o, "id"),            45),
                strCol("Tytuł",   o -> str(o, "title"),        200),
                strCol("Artyści", o -> artists(o),             170),
                durationCol("Czas",    "duration_ms",           65),
                numCol("♥",  "like_count",                      45),
                numCol("▶",  "stream_count",                    45),
                scoreCol("Avg",  "avg_score",                   55),
                scoreCol("Moja", "my_score",                    50),
                likeToggleCol("/api/likes/songs/", "/api/likes/songs/", this::refresh)
        );
        applyStyle(songsTable);
        paginationBar.getChildren().setAll(buildPaginationBar().getChildren());

        songsTable.setRowFactory(tv -> {
            var row = new javafx.scene.control.TableRow<JsonObject>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty())
                    DetailDialog.showSong(row.getItem());
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
}
