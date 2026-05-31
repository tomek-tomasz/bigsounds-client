package com.example.bigsoundsclient.controllers;

import com.example.bigsoundsclient.ApiClient;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class HistoryController extends TabController implements Initializable {

    @FXML private TableView<JsonObject> historyTable;
    @FXML private Button refreshHistoryBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        historyTable.getColumns().addAll(
                strCol("Data i czas",  o -> fmtDateTime(o, "stream_timestamp"), 160),
                strCol("Utwór",        o -> str(o, "song_title"),               200),
                strCol("Artyści",      o -> streamArtists(o),                   180),
                strCol("Wydanie",      o -> str(o, "release_title"),             180),
                strCol("Format",       o -> str(o, "release_format"),             80)
        );
        applyStyle(historyTable);
    }

    @FXML
    private void loadHistory() {
        refreshHistoryBtn.setDisable(true);
        async(() -> ApiClient.get("/api/streams"), res -> {
            refreshHistoryBtn.setDisable(false);
            historyTable.setItems(toList(res.data()));
        });
    }

    private String fmtDateTime(JsonObject o, String key) {
        String s = str(o, key);
        if ("—".equals(s) || s.length() < 19) return s;
        return s.substring(0, 10) + "  " + s.substring(11, 19);
    }

    private String streamArtists(JsonObject o) {
        if (!o.has("artists") || !o.get("artists").isJsonArray()) return "—";
        var list = new java.util.ArrayList<String>();
        for (var e : o.get("artists").getAsJsonArray()) {
            if (e.isJsonPrimitive()) list.add(e.getAsString());
        }
        return list.isEmpty() ? "—" : String.join(", ", list);
    }

    @Override
    public void refresh() { loadHistory(); }
}
