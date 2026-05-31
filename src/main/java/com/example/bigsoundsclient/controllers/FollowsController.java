package com.example.bigsoundsclient.controllers;

import com.example.bigsoundsclient.ApiClient;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class FollowsController extends TabController implements Initializable {

    @FXML private TableView<JsonObject> followedArtistsTable;
    @FXML private TableView<JsonObject> followedUsersTable;
    @FXML private TableView<JsonObject> followersTable;
    @FXML private Button refreshFollowsBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        followedArtistsTable.getColumns().addAll(
                strCol("Pseudonim", o -> str(o, "stage_name"), 240),
                strCol("Od",        o -> dateStr(o, "date_from"), 130),
                actionCol("Przestań obserwować", o -> {
                    int id = o.get("id").getAsInt();
                    async(() -> ApiClient.delete("/api/follows/artists/" + id), r -> loadFollows());
                }, 170)
        );
        applyStyle(followedArtistsTable);

        followedUsersTable.getColumns().addAll(
                strCol("Użytkownik", o -> str(o, "name"),         240),
                strCol("Od",         o -> dateStr(o, "date_from"), 130),
                actionCol("Przestań obserwować", o -> {
                    int id = o.get("id").getAsInt();
                    async(() -> ApiClient.delete("/api/follows/users/" + id), r -> loadFollows());
                }, 170)
        );
        applyStyle(followedUsersTable);

        followersTable.getColumns().addAll(
                strCol("Użytkownik", o -> str(o, "name"),          260),
                strCol("Od",         o -> dateStr(o, "date_from"), 130)
        );
        applyStyle(followersTable);
    }

    @FXML
    private void loadFollows() {
        refreshFollowsBtn.setDisable(true);
        async(() -> ApiClient.get("/api/follows/artists"), ra ->
            async(() -> ApiClient.get("/api/follows/users"), ru ->
                async(() -> ApiClient.get("/api/follows/followers"), rf -> {
                    refreshFollowsBtn.setDisable(false);
                    followedArtistsTable.setItems(toList(ra.data()));
                    followedUsersTable.setItems(toList(ru.data()));
                    followersTable.setItems(toList(rf.data()));
                })
            )
        );
    }
}
