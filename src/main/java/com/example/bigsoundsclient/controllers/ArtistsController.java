package com.example.bigsoundsclient.controllers;

import com.example.bigsoundsclient.ApiClient;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class ArtistsController extends TabController implements Initializable {

    @FXML private TableView<JsonObject> artistsTable;
    @FXML private Button refreshArtistsBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        artistsTable.getColumns().addAll(
                strCol("ID",         o -> str(o, "id"),          50),
                strCol("Pseudonim",  o -> str(o, "stage_name"),  240),
                strCol("Gatunek",    o -> str(o, "genre"),       140),
                actionCol("+ Obserwuj", o -> {
                    int id = o.get("id").getAsInt();
                    async(() -> ApiClient.post("/api/follows/artists/" + id, null), r -> {});
                }, 110)
        );
        applyStyle(artistsTable);
    }

    @FXML
    private void loadArtists() {
        refreshArtistsBtn.setDisable(true);
        async(() -> ApiClient.get("/api/artists"), res -> {
            refreshArtistsBtn.setDisable(false);
            artistsTable.setItems(toList(res.data()));
        });
    }
}
