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
                strCol("ID",       o -> str(o, "id"),                  50),
                strCol("Tytuł",    o -> str(o, "title"),               200),
                strCol("Artyści",  o -> artists(o),                    180),
                strCol("Format",   o -> str(o, "format"),               80),
                strCol("Data",     o -> dateStr(o, "release_date"),     100),
                actionCol("♥ Polub", o -> {
                    int id = o.get("id").getAsInt();
                    async(() -> ApiClient.post("/api/likes/releases/" + id, null), r -> {});
                }, 90)
        );
        applyStyle(releasesTable);
        paginationBar.getChildren().setAll(buildPaginationBar().getChildren());
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
}
