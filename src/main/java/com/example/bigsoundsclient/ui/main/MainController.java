package com.example.bigsoundsclient.ui.main;

import com.example.bigsoundsclient.api.ApiClient;
import com.example.bigsoundsclient.api.AuthState;
import com.example.bigsoundsclient.app.BigSoundsApplication;
import com.example.bigsoundsclient.ui.base.TabController;
import com.example.bigsoundsclient.ui.tabs.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class MainController implements Initializable {

    @FXML private Label   userLabel;
    @FXML private TabPane tabPane;

    @FXML private SongsController     songsController;
    @FXML private ReleasesController  releasesController;
    @FXML private ArtistsController   artistsController;
    @FXML private PlaylistsController playlistsController;
    @FXML private LikesController     likesController;
    @FXML private FollowsController   followsController;
    @FXML private StatsController     statsController;
    @FXML private HistoryController   historyController;
    @FXML private UsersController           usersController;
    @FXML private RecommendationsController recommendationsController;
    @FXML private FeedController            feedController;
    @FXML private AccountController         accountController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUserLabel();
        setupTabRefresh();
    }

    private void setupTabRefresh() {
        List<TabController> controllers = new ArrayList<>();
        controllers.add(songsController);
        controllers.add(releasesController);
        controllers.add(artistsController);
        controllers.add(playlistsController);
        controllers.add(likesController);
        controllers.add(followsController);
        controllers.add(statsController);
        controllers.add(historyController);
        controllers.add(usersController);
        controllers.add(recommendationsController);
        controllers.add(feedController);
        controllers.add(accountController);

        tabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            int i = newIdx.intValue();
            if (i >= 0 && i < controllers.size()) {
                TabController ctrl = controllers.get(i);
                if (ctrl != null) ctrl.refresh();
            }
        });

        Platform.runLater(() -> {
            int i = tabPane.getSelectionModel().getSelectedIndex();
            if (i >= 0 && i < controllers.size()) {
                TabController ctrl = controllers.get(i);
                if (ctrl != null) ctrl.refresh();
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
