package com.example.bigsoundsclient;

import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class UsersController extends TabController implements Initializable {

    @FXML private TableView<JsonObject> usersTable;
    @FXML private Button refreshUsersBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usersTable.getColumns().addAll(
                strCol("Użytkownik",    o -> str(o, "name"),           200),
                strCol("Dołączył/a",   o -> dateStr(o, "date_joined"), 110),
                strCol("Zgodność",     o -> compatStr(o),               90),
                actionCol("+ Obserwuj", o -> {
                    int id = o.get("id").getAsInt();
                    async(() -> ApiClient.post("/api/follows/users/" + id, null), r -> {});
                }, 110),
                actionCol("🔍 Zgodność", o -> loadCompat(o), 110)
        );
        applyStyle(usersTable);
    }

    @FXML
    private void loadUsers() {
        refreshUsersBtn.setDisable(true);
        async(() -> ApiClient.get("/api/users"), res -> {
            refreshUsersBtn.setDisable(false);
            int myId = AuthState.getInstance().getUserId();
            var all = toList(res.data());
            if (myId >= 0) all.removeIf(o -> o.has("id") && o.get("id").getAsInt() == myId);
            usersTable.setItems(all);
        });
    }

    private void loadCompat(JsonObject user) {
        int id = user.get("id").getAsInt();
        async(() -> ApiClient.get("/api/users/" + id + "/compatibility"), res -> {
            if (!res.ok() || !res.data().isJsonObject()) return;
            JsonObject data = res.data().getAsJsonObject();
            user.addProperty("_compat", data.get("compatibility_score").getAsString());
            showCompatDialog(str(user, "name"), data);
        });
    }

    private void showCompatDialog(String userName, JsonObject data) {
        String score  = data.has("compatibility_score") ?
                String.format("%.0f / 100", data.get("compatibility_score").getAsDouble()) : "—";
        String common = str(data, "common_liked_songs");
        String total  = str(data, "total_liked_songs");
        String diff   = data.has("avg_score_difference") && !data.get("avg_score_difference").isJsonNull()
                ? String.format("%.1f", data.get("avg_score_difference").getAsDouble()) : "—";

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Zgodność z " + userName);
        alert.setHeaderText("Wynik zgodności: " + score);
        alert.setContentText(
                "Wspólne polubienia: " + common + " / " + total + "\n" +
                "Różnica ocen: " + diff
        );
        alert.showAndWait();
    }

    private String compatStr(JsonObject o) {
        return o.has("_compat") ? o.get("_compat").getAsString() + "%" : "—";
    }

    @Override
    public void refresh() { loadUsers(); }
}
