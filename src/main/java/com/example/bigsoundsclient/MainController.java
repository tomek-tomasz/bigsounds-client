package com.example.bigsoundsclient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class MainController implements Initializable {

    // Top bar
    @FXML private Label userLabel;

    // Songs
    @FXML private TableView<JsonObject> songsTable;
    @FXML private Button refreshSongsBtn;

    // Releases
    @FXML private TableView<JsonObject> releasesTable;
    @FXML private Button refreshReleasesBtn;

    // Artists
    @FXML private TableView<JsonObject> artistsTable;
    @FXML private Button refreshArtistsBtn;

    // Playlists
    @FXML private TableView<JsonObject> playlistsTable;
    @FXML private TextField newPlaylistName;
    @FXML private CheckBox newPlaylistPrivate;
    @FXML private Button createPlaylistBtn;
    @FXML private Button refreshPlaylistsBtn;
    @FXML private Label playlistSongsLabel;
    @FXML private TableView<JsonObject> playlistSongsTable;

    // Likes
    @FXML private TableView<JsonObject> likedSongsTable;
    @FXML private TableView<JsonObject> likedReleasesTable;
    @FXML private Button refreshLikesBtn;

    // Follows
    @FXML private TableView<JsonObject> followedArtistsTable;
    @FXML private TableView<JsonObject> followedUsersTable;
    @FXML private TableView<JsonObject> followersTable;
    @FXML private Button refreshFollowsBtn;

    // Stats
    @FXML private DatePicker statsFrom;
    @FXML private DatePicker statsTo;
    @FXML private Button loadStatsBtn;
    @FXML private HBox statsCards;
    @FXML private TableView<JsonObject> topSongsTable;
    @FXML private TableView<JsonObject> topArtistsTable;
    @FXML private TableView<JsonObject> topReleasesTable;

    // Account
    @FXML private VBox profileBox;
    @FXML private TextArea accountResponse;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupSongsTable();
        setupReleasesTable();
        setupArtistsTable();
        setupPlaylistsTable();
        setupPlaylistSongsTable();
        setupLikedSongsTable();
        setupLikedReleasesTable();
        setupFollowedArtistsTable();
        setupFollowedUsersTable();
        setupFollowersTable();
        setupTopSongsTable();
        setupTopArtistsTable();
        setupTopReleasesTable();
        loadAccount();
    }

    // ─── LOGOUT ──────────────────────────────────────────────────────────────

    @FXML
    private void handleLogout() {
        AuthState.getInstance().clear();
        try { BigSoundsApplication.showLogin(); } catch (Exception ignored) {}
    }

    // ─── SONGS ───────────────────────────────────────────────────────────────

    private void setupSongsTable() {
        songsTable.getColumns().clear();
        songsTable.getColumns().addAll(
                strCol("ID", o -> str(o, "id"), 50),
                strCol("Tytuł", o -> str(o, "title"), 200),
                strCol("Artyści", o -> artists(o), 200),
                strCol("Czas", o -> fmtMs(o, "duration_ms"), 80),
                actionCol("♥ Polub", o -> {
                    int id = o.get("id").getAsInt();
                    async(() -> ApiClient.post("/api/likes/songs/" + id, null), r -> {});
                }, 80)
        );
        applyDarkStyle(songsTable);
    }

    @FXML
    private void loadSongs() {
        refreshSongsBtn.setDisable(true);
        async(() -> ApiClient.get("/api/songs"), res -> {
            refreshSongsBtn.setDisable(false);
            songsTable.setItems(toList(res.data()));
        });
    }

    // ─── RELEASES ────────────────────────────────────────────────────────────

    private void setupReleasesTable() {
        releasesTable.getColumns().clear();
        releasesTable.getColumns().addAll(
                strCol("ID", o -> str(o, "id"), 50),
                strCol("Tytuł", o -> str(o, "title"), 200),
                strCol("Artyści", o -> artists(o), 180),
                strCol("Format", o -> str(o, "format"), 80),
                strCol("Data", o -> dateStr(o, "release_date"), 100),
                actionCol("♥ Polub", o -> {
                    int id = o.get("id").getAsInt();
                    async(() -> ApiClient.post("/api/likes/releases/" + id, null), r -> {});
                }, 80)
        );
        applyDarkStyle(releasesTable);
    }

    @FXML
    private void loadReleases() {
        refreshReleasesBtn.setDisable(true);
        async(() -> ApiClient.get("/api/releases"), res -> {
            refreshReleasesBtn.setDisable(false);
            releasesTable.setItems(toList(res.data()));
        });
    }

    // ─── ARTISTS ─────────────────────────────────────────────────────────────

    private void setupArtistsTable() {
        artistsTable.getColumns().clear();
        artistsTable.getColumns().addAll(
                strCol("ID", o -> str(o, "id"), 50),
                strCol("Pseudonim", o -> str(o, "stage_name"), 220),
                strCol("Gatunek", o -> str(o, "genre"), 120),
                actionCol("+ Obserwuj", o -> {
                    int id = o.get("id").getAsInt();
                    async(() -> ApiClient.post("/api/follows/artists/" + id, null), r -> {});
                }, 100)
        );
        applyDarkStyle(artistsTable);
    }

    @FXML
    private void loadArtists() {
        refreshArtistsBtn.setDisable(true);
        async(() -> ApiClient.get("/api/artists"), res -> {
            refreshArtistsBtn.setDisable(false);
            artistsTable.setItems(toList(res.data()));
        });
    }

    // ─── PLAYLISTS ────────────────────────────────────────────────────────────

    private void setupPlaylistsTable() {
        playlistsTable.getColumns().clear();
        playlistsTable.getColumns().addAll(
                strCol("Nazwa", o -> str(o, "name"), 200),
                strCol("Utwory", o -> str(o, "song_count"), 70),
                strCol("Widoczność", o -> bool(o, "is_private") ? "🔒 Prywatna" : "🌐 Publiczna", 110),
                actionCol("Pokaż", o -> showPlaylistSongs(o), 75),
                actionCol("🗑 Usuń", o -> {
                    int id = o.get("id").getAsInt();
                    async(() -> ApiClient.delete("/api/playlists/" + id), r -> loadPlaylists());
                }, 75)
        );
        applyDarkStyle(playlistsTable);
    }

    @FXML
    private void loadPlaylists() {
        refreshPlaylistsBtn.setDisable(true);
        async(() -> ApiClient.get("/api/playlists/mine"), res -> {
            refreshPlaylistsBtn.setDisable(false);
            playlistsTable.setItems(toList(res.data()));
        });
    }

    @FXML
    private void createPlaylist() {
        String name = newPlaylistName.getText().trim();
        if (name.isEmpty()) return;
        createPlaylistBtn.setDisable(true);
        boolean priv = newPlaylistPrivate.isSelected();
        async(() -> ApiClient.post("/api/playlists",
                Map.of("name", name, "is_private", priv, "cover", "untitled.png")), res -> {
            createPlaylistBtn.setDisable(false);
            newPlaylistName.clear();
            loadPlaylists();
        });
    }

    private void showPlaylistSongs(JsonObject playlist) {
        int id = playlist.get("id").getAsInt();
        String name = str(playlist, "name");
        playlistSongsLabel.setText("Utwory w playliście: " + name);
        playlistSongsTable.setVisible(true);
        playlistSongsTable.setManaged(true);
        async(() -> ApiClient.get("/api/playlists/" + id), res -> {
            JsonObject obj = res.data().isJsonObject() ? res.data().getAsJsonObject() : new JsonObject();
            JsonElement songs = obj.has("songs") ? obj.get("songs") : new JsonArray();
            playlistSongsTable.setItems(toList(songs));
        });
    }

    private void setupPlaylistSongsTable() {
        playlistSongsTable.getColumns().clear();
        playlistSongsTable.getColumns().addAll(
                strCol("Tytuł", o -> str(o, "title"), 200),
                strCol("Artyści", o -> artists(o), 180),
                strCol("Czas", o -> fmtMs(o, "duration_ms"), 80)
        );
        applyDarkStyle(playlistSongsTable);
    }

    // ─── LIKES ───────────────────────────────────────────────────────────────

    private void setupLikedSongsTable() {
        likedSongsTable.getColumns().clear();
        likedSongsTable.getColumns().addAll(
                strCol("Tytuł", o -> str(o, "title"), 200),
                strCol("Artyści", o -> artists(o), 200),
                strCol("Czas", o -> fmtMs(o, "duration_ms"), 80),
                actionCol("💔 Usuń polub.", o -> {
                    int id = o.get("id").getAsInt();
                    async(() -> ApiClient.delete("/api/likes/songs/" + id), r -> loadLikes());
                }, 110)
        );
        applyDarkStyle(likedSongsTable);
    }

    private void setupLikedReleasesTable() {
        likedReleasesTable.getColumns().clear();
        likedReleasesTable.getColumns().addAll(
                strCol("Tytuł", o -> str(o, "title"), 200),
                strCol("Artyści", o -> artists(o), 180),
                strCol("Format", o -> str(o, "format"), 80),
                strCol("Data", o -> dateStr(o, "release_date"), 100),
                actionCol("💔 Usuń polub.", o -> {
                    int id = o.get("id").getAsInt();
                    async(() -> ApiClient.delete("/api/likes/releases/" + id), r -> loadLikes());
                }, 110)
        );
        applyDarkStyle(likedReleasesTable);
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

    // ─── FOLLOWS ─────────────────────────────────────────────────────────────

    private void setupFollowedArtistsTable() {
        followedArtistsTable.getColumns().clear();
        followedArtistsTable.getColumns().addAll(
                strCol("Pseudonim", o -> str(o, "stage_name"), 220),
                strCol("Od", o -> dateStr(o, "date_from"), 120),
                actionCol("Przestań obserwować", o -> {
                    int id = o.get("id").getAsInt();
                    async(() -> ApiClient.delete("/api/follows/artists/" + id), r -> loadFollows());
                }, 160)
        );
        applyDarkStyle(followedArtistsTable);
    }

    private void setupFollowedUsersTable() {
        followedUsersTable.getColumns().clear();
        followedUsersTable.getColumns().addAll(
                strCol("Użytkownik", o -> str(o, "name"), 220),
                strCol("Od", o -> dateStr(o, "date_from"), 120),
                actionCol("Przestań obserwować", o -> {
                    int id = o.get("id").getAsInt();
                    async(() -> ApiClient.delete("/api/follows/users/" + id), r -> loadFollows());
                }, 160)
        );
        applyDarkStyle(followedUsersTable);
    }

    private void setupFollowersTable() {
        followersTable.getColumns().clear();
        followersTable.getColumns().addAll(
                strCol("Użytkownik", o -> str(o, "name"), 220),
                strCol("Od", o -> dateStr(o, "date_from"), 120)
        );
        applyDarkStyle(followersTable);
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

    // ─── STATS ───────────────────────────────────────────────────────────────

    private void setupTopSongsTable() {
        topSongsTable.getColumns().clear();
        topSongsTable.getColumns().addAll(
                strCol("#", o -> str(o, "_rank"), 35),
                strCol("Tytuł", o -> str(o, "title"), 180),
                strCol("Artyści", o -> artists(o), 160),
                strCol("Odtworzenia", o -> str(o, "stream_count"), 100),
                strCol("Czas", o -> fmtHours(o, "total_ms"), 80)
        );
        applyDarkStyle(topSongsTable);
    }

    private void setupTopArtistsTable() {
        topArtistsTable.getColumns().clear();
        topArtistsTable.getColumns().addAll(
                strCol("#", o -> str(o, "_rank"), 35),
                strCol("Artysta", o -> str(o, "stage_name"), 200),
                strCol("Odtworzenia", o -> str(o, "stream_count"), 100),
                strCol("Czas", o -> fmtHours(o, "total_ms"), 80),
                strCol("Unikalne utwory", o -> str(o, "unique_songs"), 110)
        );
        applyDarkStyle(topArtistsTable);
    }

    private void setupTopReleasesTable() {
        topReleasesTable.getColumns().clear();
        topReleasesTable.getColumns().addAll(
                strCol("#", o -> str(o, "_rank"), 35),
                strCol("Tytuł", o -> str(o, "title"), 180),
                strCol("Artyści", o -> artists(o), 150),
                strCol("Format", o -> str(o, "format"), 70),
                strCol("Odtworzenia", o -> str(o, "stream_count"), 100),
                strCol("Czas", o -> fmtHours(o, "total_ms"), 80)
        );
        applyDarkStyle(topReleasesTable);
    }

    @FXML
    private void loadStats() {
        loadStatsBtn.setDisable(true);
        String from = statsFrom.getValue() != null ? statsFrom.getValue().toString() : null;
        String to   = statsTo.getValue()   != null ? statsTo.getValue().toString()   : null;
        String qs   = buildQs(from, to, 10);

        async(() -> ApiClient.get("/api/stats/overview" + qs), rov ->
            async(() -> ApiClient.get("/api/stats/top-songs" + qs), rts ->
                async(() -> ApiClient.get("/api/stats/top-artists" + qs), rta ->
                    async(() -> ApiClient.get("/api/stats/top-releases" + qs), rtr -> {
                        loadStatsBtn.setDisable(false);
                        renderStatCards(rov.data());
                        topSongsTable.setItems(withRank(toList(rts.data())));
                        topArtistsTable.setItems(withRank(toList(rta.data())));
                        topReleasesTable.setItems(withRank(toList(rtr.data())));
                    })
                )
            )
        );
    }

    private void renderStatCards(JsonElement data) {
        statsCards.getChildren().clear();
        if (!data.isJsonObject()) return;
        JsonObject o = data.getAsJsonObject();
        addCard("Streamy",       str(o, "stream_count"));
        addCard("Czas słuchania", formatHoursStr(longVal(o, "total_ms")));
        addCard("Unikalne utwory", str(o, "unique_songs"));
        addCard("Artyści",        str(o, "unique_artists"));
        addCard("Wydania",        str(o, "unique_releases"));
    }

    private void addCard(String label, String value) {
        VBox card = new VBox(4);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 14 18 14 18; " +
                      "-fx-border-color: #dde1e7; -fx-border-radius: 8;");
        card.setMinWidth(110);
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1db954;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #777;");
        card.getChildren().addAll(val, lbl);
        statsCards.getChildren().add(card);
    }

    // ─── ACCOUNT ─────────────────────────────────────────────────────────────

    private void loadAccount() {
        async(() -> ApiClient.get("/api/users/me"), res -> {
            profileBox.getChildren().clear();
            if (res.ok() && res.data().isJsonObject()) {
                JsonObject o = res.data().getAsJsonObject();
                String name = str(o, "name");
                userLabel.setText("Zalogowany: " + name);
                addProfileRow("ID", str(o, "id"));
                addProfileRow("Użytkownik", name);
                addProfileRow("Spotify ID", str(o, "streaming_id"));
                addProfileRow("Dołączył/a", dateStr(o, "date_joined"));
            }
        });
    }

    private void addProfileRow(String key, String value) {
        HBox row = new HBox(12);
        Label k = new Label(key + ":");
        k.setStyle("-fx-text-fill: #777; -fx-font-size: 13px; -fx-min-width: 110;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill: #111; -fx-font-size: 13px; -fx-font-weight: bold;");
        row.getChildren().addAll(k, v);
        profileBox.getChildren().add(row);
    }

    @FXML
    private void handleRefreshSpotify() {
        accountResponse.setText("Odświeżanie…");
        async(() -> ApiClient.post("/api/users/spotify/refresh", null), res ->
            accountResponse.setText(res.data().toString())
        );
    }

    @FXML
    private void handleSyncStreams() {
        accountResponse.setText("Synchronizowanie…");
        async(() -> ApiClient.post("/api/users/sync-streams", null), res ->
            accountResponse.setText(res.data().toString())
        );
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    @FunctionalInterface
    interface Task { ApiClient.ApiResponse run(); }

    @FunctionalInterface
    interface Callback { void accept(ApiClient.ApiResponse res); }

    private void async(Task task, Callback callback) {
        CompletableFuture.supplyAsync(task::run)
                .thenAccept(res -> Platform.runLater(() -> callback.accept(res)));
    }

    private ObservableList<JsonObject> toList(JsonElement el) {
        List<JsonObject> list = new ArrayList<>();
        if (el != null && el.isJsonArray()) {
            for (JsonElement e : el.getAsJsonArray()) {
                if (e.isJsonObject()) list.add(e.getAsJsonObject());
            }
        }
        return FXCollections.observableArrayList(list);
    }

    private ObservableList<JsonObject> withRank(ObservableList<JsonObject> list) {
        for (int i = 0; i < list.size(); i++) {
            list.get(i).addProperty("_rank", i + 1);
        }
        return list;
    }

    @FunctionalInterface
    interface ValFn { String get(JsonObject o); }

    @SuppressWarnings("unchecked")
    private TableColumn<JsonObject, String> strCol(String header, ValFn fn, double width) {
        TableColumn<JsonObject, String> col = new TableColumn<>(header);
        col.setCellValueFactory(cd -> new SimpleStringProperty(fn.get(cd.getValue())));
        col.setPrefWidth(width);
        col.setStyle("-fx-text-fill: #222;");
        return col;
    }

    @SuppressWarnings("unchecked")
    private TableColumn<JsonObject, String> actionCol(String label, java.util.function.Consumer<JsonObject> action, double width) {
        TableColumn<JsonObject, String> col = new TableColumn<>("");
        col.setPrefWidth(width);
        col.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button(label);
            {
                btn.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-cursor: hand; " +
                             "-fx-border-color: #ccc; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 3 10 3 10; -fx-font-size: 11px;");
                btn.setOnAction(e -> {
                    JsonObject item = getTableView().getItems().get(getIndex());
                    if (item != null) action.accept(item);
                });
            }
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setGraphic(empty ? null : btn);
            }
        });
        return col;
    }

    private void applyDarkStyle(TableView<?> tv) {
        tv.setStyle("-fx-background-color: white; -fx-control-inner-background: white; " +
                    "-fx-table-cell-border-color: #eee;");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private String str(JsonObject o, String key) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull()) return "—";
        return o.get(key).getAsString();
    }

    private boolean bool(JsonObject o, String key) {
        if (o == null || !o.has(key)) return false;
        try { return o.get(key).getAsBoolean(); } catch (Exception e) { return false; }
    }

    private long longVal(JsonObject o, String key) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull()) return 0;
        try { return o.get(key).getAsLong(); } catch (Exception e) { return 0; }
    }

    private String artists(JsonObject o) {
        if (o == null || !o.has("artists") || !o.get("artists").isJsonArray()) return "—";
        List<String> names = new ArrayList<>();
        for (JsonElement e : o.get("artists").getAsJsonArray()) {
            names.add(e.isJsonPrimitive() ? e.getAsString() : str(e.getAsJsonObject(), "stage_name"));
        }
        return names.isEmpty() ? "—" : String.join(", ", names);
    }

    private String fmtMs(JsonObject o, String key) {
        long ms = longVal(o, key);
        if (ms <= 0) return "—";
        long min = ms / 60000;
        long sec = (ms % 60000) / 1000;
        return String.format("%d:%02d", min, sec);
    }

    private String fmtHours(JsonObject o, String key) {
        return formatHoursStr(longVal(o, key));
    }

    private String formatHoursStr(long ms) {
        if (ms <= 0) return "0h";
        long hours = ms / 3600000;
        long min   = (ms % 3600000) / 60000;
        return hours > 0 ? hours + "h " + min + "m" : min + "m";
    }

    private String dateStr(JsonObject o, String key) {
        String s = str(o, key);
        if ("—".equals(s) || s.length() < 10) return s;
        return s.substring(0, 10);
    }

    private String buildQs(String from, String to, int limit) {
        List<String> parts = new ArrayList<>();
        parts.add("limit=" + limit);
        if (from != null) parts.add("from=" + from);
        if (to   != null) parts.add("to=" + to);
        return "?" + String.join("&", parts);
    }
}
