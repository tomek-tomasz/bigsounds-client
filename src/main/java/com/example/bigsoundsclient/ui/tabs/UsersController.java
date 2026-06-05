package com.example.bigsoundsclient.ui.tabs;

import com.example.bigsoundsclient.api.ApiClient;
import com.example.bigsoundsclient.api.AuthState;
import com.example.bigsoundsclient.ui.base.TabController;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UsersController extends TabController implements Initializable {

    @FXML private TableView<JsonObject> usersTable;
    @FXML private Button refreshUsersBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usersTable.getColumns().addAll(
                strCol("Użytkownik",  o -> str(o, "name"),           200),
                strCol("Dołączył/a", o -> dateStr(o, "date_joined"), 110),
                strCol("Zgodność",   o -> compatStr(o),               90),
                followToggleCol(
                        "/api/follows/users/", "/api/follows/users/",
                        "_followed"),
                actionCol("🔍 Zgodność", o -> loadCompat(o), 110)
        );
        applyStyle(usersTable);
    }

    @FXML
    private void loadUsers() {
        refreshUsersBtn.setDisable(true);
        async(() -> ApiClient.get("/api/users"), resUsers ->
            async(() -> ApiClient.get("/api/follows/users"), resFollowed -> {
                refreshUsersBtn.setDisable(false);
                int myId = AuthState.getInstance().getUserId();

                Set<Integer> followedIds = StreamSupport
                        .stream(resFollowed.data().isJsonArray()
                                ? resFollowed.data().getAsJsonArray().spliterator()
                                : new java.util.ArrayList<com.google.gson.JsonElement>().spliterator(), false)
                        .filter(com.google.gson.JsonElement::isJsonObject)
                        .map(e -> e.getAsJsonObject().get("id").getAsInt())
                        .collect(Collectors.toSet());

                var all = toList(resUsers.data());
                if (myId >= 0) all.removeIf(o -> o.has("id") && o.get("id").getAsInt() == myId);

                all.forEach(o -> o.addProperty("_followed",
                        followedIds.contains(o.get("id").getAsInt())));

                usersTable.setItems(all);
            })
        );
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
        String score  = data.has("compatibility_score")
                ? String.format("%.0f / 100", data.get("compatibility_score").getAsDouble()) : "—";
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
