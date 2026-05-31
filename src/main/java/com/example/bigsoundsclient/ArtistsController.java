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
                strCol("ID",        o -> str(o, "id"),          50),
                strCol("Pseudonim", o -> str(o, "stage_name"), 240),
                strCol("Gatunek",   o -> str(o, "genre"),      140),
                actionCol("+ Obserwuj", o -> {
                    int id = o.get("id").getAsInt();
                    async(() -> ApiClient.post("/api/follows/artists/" + id, null), r -> {});
                }, 110)
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
