package com.example.bigsoundsclient;

import com.google.gson.JsonObject;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class ArtistsController extends PagedTabController implements Initializable {

    @FXML private TableView<JsonObject> artistsTable;
    @FXML private HBox paginationBar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        artistsTable.getColumns().addAll(
                strCol("ID",          o -> str(o, "id"),            45),
                strCol("Pseudonim",   o -> str(o, "stage_name"),   195),
                numCol("Utwory",      "song_count",                  65),
                numCol("Wydania",     "release_count",               65),
                numCol("Obserwujący", "follower_count",              90),
                numCol("▶ Streamy",   "stream_count",                80),
                followToggleCol("/api/follows/artists/", "/api/follows/artists/", "followed")
        );
        applyStyle(artistsTable);
        paginationBar.getChildren().setAll(buildPaginationBar().getChildren());
    }

    @Override
    protected void loadPage(int page, int limit) {
        async(() -> ApiClient.get("/api/artists?page=" + page + "&limit=" + limit),
              res -> applyPage(res.data()));
    }

    @Override
    protected void updateTable(ObservableList<JsonObject> items) {
        artistsTable.setItems(items);
    }
}
