package com.example.bigsoundsclient;

import com.google.gson.JsonObject;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class SongsController extends PagedTabController implements Initializable {

    @FXML private TableView<JsonObject> songsTable;
    @FXML private HBox paginationBar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TableColumn<JsonObject, Number> idCol     = numCol("ID",      "id",           45);
        TableColumn<JsonObject, String> titleCol  = strCol("Tytuł",   o -> str(o, "title"),    200);
        TableColumn<JsonObject, String> artistCol = strCol("Artyści", o -> artists(o),         170);
        TableColumn<JsonObject, Number> durCol    = durationCol("Czas", "duration_ms",          65);
        TableColumn<JsonObject, Number> likeCol   = numCol("♥",  "like_count",                 45);
        TableColumn<JsonObject, Number> streamCol = numCol("▶",  "stream_count",               45);
        TableColumn<JsonObject, Number> avgCol    = scoreCol("Avg",  "avg_score",              55);
        TableColumn<JsonObject, Number> myCol     = scoreCol("Moja", "my_score",               50);
        TableColumn<JsonObject, String> toggleCol = likeToggleCol(
                "/api/likes/songs/", "/api/likes/songs/", this::reloadCurrentPage);

        songsTable.getColumns().addAll(
                idCol, titleCol, artistCol, durCol, likeCol, streamCol, avgCol, myCol, toggleCol);
        applyStyle(songsTable);
        paginationBar.getChildren().setAll(buildPaginationBar().getChildren());

        registerSort(idCol,     "id");
        registerSort(titleCol,  "title");
        registerSort(durCol,    "duration_ms");
        registerSort(likeCol,   "like_count");
        registerSort(streamCol, "stream_count");
        registerSort(avgCol,    "avg_score");
        makeSortable(songsTable);

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
        async(() -> ApiClient.get("/api/songs" + pageQs(page, limit)),
              res -> applyPage(res.data()));
    }

    @Override
    protected void updateTable(ObservableList<JsonObject> items) {
        setTableItems(songsTable, items);
    }
}
