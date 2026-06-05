package com.example.bigsoundsclient.ui.tabs;

import com.example.bigsoundsclient.api.ApiClient;
import com.example.bigsoundsclient.ui.base.TabController;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class FeedController extends TabController implements Initializable {

    @FXML private TableView<JsonObject> feedTable;
    @FXML private Button refreshFeedBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        feedTable.getColumns().addAll(
                strCol("Użytkownik",  o -> str(o, "friend_name"),  160),
                strCol("Aktywność",   o -> fmtActivity(o),         140),
                strCol("Utwór/Wydanie", o -> str(o, "item_title"), 260),
                strCol("Data",        o -> fmtDate(o),             140)
        );
        applyStyle(feedTable);
    }

    @FXML
    private void loadFeed() {
        refreshFeedBtn.setDisable(true);
        async(() -> ApiClient.get("/api/follows/feed?limit=50"), res -> {
            refreshFeedBtn.setDisable(false);
            feedTable.setItems(toList(res.data()));
        });
    }

    @Override
    public void refresh() { loadFeed(); }

    private String fmtActivity(JsonObject o) {
        String type = str(o, "activity_type");
        return switch (type) {
            case "song_like"      -> "♥ Polubił utwór";
            case "release_review" -> "★ Ocenił wydanie";
            default               -> type;
        };
    }

    private String fmtDate(JsonObject o) {
        String s = str(o, "activity_date");
        if ("—".equals(s) || s.length() < 19) return s;
        return s.substring(0, 10) + "  " + s.substring(11, 19);
    }
}
