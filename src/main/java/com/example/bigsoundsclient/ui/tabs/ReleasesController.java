package com.example.bigsoundsclient.ui.tabs;

import com.example.bigsoundsclient.api.ApiClient;
import com.example.bigsoundsclient.ui.base.PagedTabController;
import com.example.bigsoundsclient.ui.main.DetailDialog;
import com.google.gson.JsonObject;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class ReleasesController extends PagedTabController implements Initializable {

    @FXML private TableView<JsonObject> releasesTable;
    @FXML private HBox paginationBar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TableColumn<JsonObject, Number> idCol     = numCol("ID",      "id",                          45);
        TableColumn<JsonObject, String> titleCol  = strCol("Tytuł",   o -> str(o, "title"),         180);
        TableColumn<JsonObject, String> artistCol = strCol("Artyści", o -> artists(o),              150);
        TableColumn<JsonObject, String> formatCol = strCol("Format",  o -> str(o, "format"),         70);
        TableColumn<JsonObject, String> dateCol   = strCol("Data",    o -> dateStr(o, "release_date"), 90);
        TableColumn<JsonObject, Number> likeCol   = numCol("♥",  "like_count",                       45);
        TableColumn<JsonObject, Number> streamCol = numCol("▶",  "stream_count",                     45);
        TableColumn<JsonObject, Number> avgCol    = scoreCol("Avg",  "avg_score",                    55);
        TableColumn<JsonObject, Number> myCol     = scoreCol("Moja", "my_score",                     50);
        TableColumn<JsonObject, String> toggleCol = likeToggleCol(
                "/api/likes/releases/", "/api/likes/releases/", this::reloadCurrentPage);

        releasesTable.getColumns().addAll(
                idCol, titleCol, artistCol, formatCol, dateCol,
                likeCol, streamCol, avgCol, myCol, toggleCol);
        applyStyle(releasesTable);
        paginationBar.getChildren().setAll(buildPaginationBar().getChildren());

        registerSort(idCol,     "id");
        registerSort(titleCol,  "title");
        registerSort(formatCol, "format");
        registerSort(dateCol,   "release_date");
        registerSort(likeCol,   "like_count");
        registerSort(streamCol, "stream_count");
        registerSort(avgCol,    "avg_score");
        makeSortable(releasesTable);

        releasesTable.setRowFactory(tv -> {
            var row = new javafx.scene.control.TableRow<JsonObject>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty())
                    DetailDialog.showRelease(row.getItem());
            });
            return row;
        });
    }

    @Override
    protected void loadPage(int page, int limit) {
        async(() -> ApiClient.get("/api/releases" + pageQs(page, limit)),
              res -> applyPage(res.data()));
    }

    @Override
    protected void updateTable(ObservableList<JsonObject> items) {
        setTableItems(releasesTable, items);
    }
}
