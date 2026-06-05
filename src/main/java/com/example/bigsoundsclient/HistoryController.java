package com.example.bigsoundsclient;

import com.google.gson.JsonObject;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class HistoryController extends PagedTabController implements Initializable {

    @FXML private TableView<JsonObject> historyTable;
    @FXML private HBox paginationBar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        historyTable.getColumns().addAll(
                strCol("Data i czas", o -> fmtDateTime(o, "stream_timestamp"), 160),
                strCol("Utwór",       o -> str(o, "song_title"),               200),
                strCol("Artyści",     o -> streamArtists(o),                   180),
                strCol("Wydanie",     o -> str(o, "release_title"),             180),
                strCol("Format",      o -> str(o, "release_format"),             80)
        );
        applyStyle(historyTable);
        paginationBar.getChildren().setAll(buildPaginationBar().getChildren());
    }

    @Override
    protected void loadPage(int page, int limit) {
        async(() -> ApiClient.get("/api/streams" + pageQs(page, limit)),
              res -> applyPage(res.data()));
    }

    @Override
    protected void updateTable(ObservableList<JsonObject> items) {
        setTableItems(historyTable, items);
    }

    private String fmtDateTime(JsonObject o, String key) {
        String s = str(o, key);
        if ("—".equals(s) || s.length() < 19) return s;
        return s.substring(0, 10) + "  " + s.substring(11, 19);
    }

    private String streamArtists(JsonObject o) {
        if (!o.has("artists") || !o.get("artists").isJsonArray()) return "—";
        ArrayList<String> list = new ArrayList<>();
        for (var e : o.get("artists").getAsJsonArray()) {
            if (e.isJsonPrimitive()) list.add(e.getAsString());
        }
        return list.isEmpty() ? "—" : String.join(", ", list);
    }
}
