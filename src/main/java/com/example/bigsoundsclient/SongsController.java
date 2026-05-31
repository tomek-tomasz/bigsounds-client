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
                strCol("ID",       o -> str(o, "id"),             50),
                strCol("Tytuł",    o -> str(o, "title"),          220),
                strCol("Artyści",  o -> artists(o),               200),
                strCol("Czas",     o -> fmtMs(o, "duration_ms"),   80),
                actionCol("♥ Polub", o -> {
                    int id = o.get("id").getAsInt();
                    async(() -> ApiClient.post("/api/likes/songs/" + id, null), r -> {});
                }, 90)
        );
        applyStyle(songsTable);
        paginationBar.getChildren().setAll(buildPaginationBar().getChildren());
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
