package com.example.bigsoundsclient.controllers;

import com.example.bigsoundsclient.ApiClient;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class ReleasesController extends TabController implements Initializable {

    @FXML private TableView<JsonObject> releasesTable;
    @FXML private Button refreshReleasesBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        releasesTable.getColumns().addAll(
                strCol("ID",       o -> str(o, "id"),                    50),
                strCol("Tytuł",    o -> str(o, "title"),                 200),
                strCol("Artyści",  o -> artists(o),                      180),
                strCol("Format",   o -> str(o, "format"),                 80),
                strCol("Data",     o -> dateStr(o, "release_date"),       100),
                actionCol("♥ Polub", o -> {
                    int id = o.get("id").getAsInt();
                    async(() -> ApiClient.post("/api/likes/releases/" + id, null), r -> {});
                }, 90)
        );
        applyStyle(releasesTable);
    }

    @FXML
    private void loadReleases() {
        refreshReleasesBtn.setDisable(true);
        async(() -> ApiClient.get("/api/releases"), res -> {
            refreshReleasesBtn.setDisable(false);
            releasesTable.setItems(toList(res.data()));
        });
    }
}
