package com.example.bigsoundsclient.ui.tabs;

import com.example.bigsoundsclient.api.ApiClient;
import com.example.bigsoundsclient.ui.base.PagedTabController;
import com.google.gson.JsonObject;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class ArtistsController extends PagedTabController implements Initializable {

    @FXML private TableView<JsonObject> artistsTable;
    @FXML private HBox paginationBar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TableColumn<JsonObject, Number> idCol         = numCol("ID",           "id",              45);
        TableColumn<JsonObject, String> nameCol       = strCol("Pseudonim",    o -> str(o, "stage_name"), 195);
        TableColumn<JsonObject, Number> songsCol      = numCol("Utwory",       "song_count",       65);
        TableColumn<JsonObject, Number> releasesCol   = numCol("Wydania",      "release_count",    65);
        TableColumn<JsonObject, Number> followersCol  = numCol("Obserwujący",  "follower_count",   90);
        TableColumn<JsonObject, Number> streamsCol    = numCol("▶ Streamy",    "stream_count",     80);
        TableColumn<JsonObject, String> followToggle  = followToggleCol(
                "/api/follows/artists/", "/api/follows/artists/", "followed");

        artistsTable.getColumns().addAll(
                idCol, nameCol, songsCol, releasesCol, followersCol, streamsCol, followToggle);
        applyStyle(artistsTable);
        paginationBar.getChildren().setAll(buildPaginationBar().getChildren());

        registerSort(idCol,        "id");
        registerSort(nameCol,      "stage_name");
        registerSort(songsCol,     "song_count");
        registerSort(releasesCol,  "release_count");
        registerSort(followersCol, "follower_count");
        registerSort(streamsCol,   "stream_count");
        makeSortable(artistsTable);
    }

    @Override
    protected void loadPage(int page, int limit) {
        async(() -> ApiClient.get("/api/artists" + pageQs(page, limit)),
              res -> applyPage(res.data()));
    }

    @Override
    protected void updateTable(ObservableList<JsonObject> items) {
        setTableItems(artistsTable, items);
    }
}
