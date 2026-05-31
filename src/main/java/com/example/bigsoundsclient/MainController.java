package com.example.bigsoundsclient;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class MainController implements Initializable {

    @FXML private Label userLabel;
    @FXML private TabPane tabPane;

    @FXML private SongsController     songsController;
    @FXML private ReleasesController  releasesController;
    @FXML private ArtistsController   artistsController;
    @FXML private PlaylistsController playlistsController;
    @FXML private LikesController     likesController;
    @FXML private FollowsController   followsController;
    @FXML private StatsController     statsController;
    @FXML private HistoryController   historyController;
    @FXML private UsersController     usersController;
    @FXML private AccountController   accountController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUserLabel();

        List<TabController> controllers = List.of(
                songsController, releasesController, artistsController,
                playlistsController, likesController, followsController,
                statsController, historyController, usersController,
                accountController
        );

        tabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            int i = newIdx.intValue();
            if (i >= 0 && i < controllers.size()) {
                controllers.get(i).refresh();
            }
        });
    }

    private void loadUserLabel() {
        CompletableFuture.supplyAsync(() -> ApiClient.get("/api/users/me"))
                .thenAccept(res -> Platform.runLater(() -> {
                    if (res.ok() && res.data().isJsonObject()) {
                        String name = res.data().getAsJsonObject().get("name").getAsString();
                        userLabel.setText("Zalogowany: " + name);
                    }
                }));
    }

    @FXML
    private void handleLogout() {
        AuthState.getInstance().clear();
        try { BigSoundsApplication.showLogin(); } catch (Exception ignored) {}
    }
}
