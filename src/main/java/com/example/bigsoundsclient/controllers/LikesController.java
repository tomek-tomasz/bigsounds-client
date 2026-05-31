package com.example.bigsoundsclient.controllers;

import com.example.bigsoundsclient.ApiClient;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class LikesController extends TabController implements Initializable {

    @FXML private TableView<JsonObject> likedSongsTable;
    @FXML private TableView<JsonObject> likedReleasesTable;
    @FXML private Button refreshLikesBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        likedSongsTable.getColumns().addAll(
                strCol("Tytuł",    o -> str(o, "title"),          220),
                strCol("Artyści",  o -> artists(o),               200),
                strCol("Czas",     o -> fmtMs(o, "duration_ms"),   80),
                actionCol("💔 Usuń", o -> {
                    int id = o.get("id").getAsInt();
                    async(() -> ApiClient.delete("/api/likes/songs/" + id), r -> loadLikes());
                }, 90)
        );
        applyStyle(likedSongsTable);

        likedReleasesTable.getColumns().addAll(
                strCol("Tytuł",    o -> str(o, "title"),           200),
                strCol("Artyści",  o -> artists(o),                180),
                strCol("Format",   o -> str(o, "format"),           80),
                strCol("Data",     o -> dateStr(o, "release_date"), 100),
                actionCol("💔 Usuń", o -> {
                    int id = o.get("id").getAsInt();
                    async(() -> ApiClient.delete("/api/likes/releases/" + id), r -> loadLikes());
                }, 90)
        );
        applyStyle(likedReleasesTable);
    }

    @FXML
    private void loadLikes() {
        refreshLikesBtn.setDisable(true);
        async(() -> ApiClient.get("/api/likes/songs"), rs ->
            async(() -> ApiClient.get("/api/likes/releases"), rr -> {
                refreshLikesBtn.setDisable(false);
                likedSongsTable.setItems(toList(rs.data()));
                likedReleasesTable.setItems(toList(rr.data()));
            })
        );
    }

    @Override
    public void refresh() { loadLikes(); }
}
