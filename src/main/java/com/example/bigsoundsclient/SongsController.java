package com.example.bigsoundsclient;

import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class SongsController extends TabController implements Initializable {

    @FXML private TableView<JsonObject> songsTable;
    @FXML private Button refreshSongsBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        songsTable.getColumns().addAll(
                strCol("ID",       o -> str(o, "id"),          50),
                strCol("Tytuł",    o -> str(o, "title"),       220),
                strCol("Artyści",  o -> artists(o),            200),
                strCol("Czas",     o -> fmtMs(o, "duration_ms"), 80),
                actionCol("♥ Polub", o -> {
                    int id = o.get("id").getAsInt();
                    async(() -> ApiClient.post("/api/likes/songs/" + id, null), r -> {});
                }, 90)
        );
        applyStyle(songsTable);
    }

    @FXML
    private void loadSongs() {
        refreshSongsBtn.setDisable(true);
        async(() -> ApiClient.get("/api/songs"), res -> {
            refreshSongsBtn.setDisable(false);
            songsTable.setItems(toList(res.data()));
        });
    }
}
