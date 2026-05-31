package com.example.bigsoundsclient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class PlaylistsController extends TabController implements Initializable {

    @FXML private TableView<JsonObject> playlistsTable;
    @FXML private TableView<JsonObject> playlistSongsTable;
    @FXML private TextField newPlaylistName;
    @FXML private CheckBox newPlaylistPrivate;
    @FXML private Button createPlaylistBtn;
    @FXML private Button refreshPlaylistsBtn;
    @FXML private Label playlistSongsLabel;
    @FXML private TextField addSongIdField;
    @FXML private Button addSongBtn;

    private int currentPlaylistId = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playlistsTable.getColumns().addAll(
                strCol("Nazwa",      o -> str(o, "name"),       220),
                strCol("Utwory",     o -> str(o, "song_count"),  70),
                strCol("Widoczność", o -> bool(o, "is_private") ? "🔒 Prywatna" : "🌐 Publiczna", 120),
                actionCol("Pokaż",   o -> showPlaylistSongs(o),  80),
                actionCol("🗑 Usuń", o -> {
                    int id = o.get("id").getAsInt();
                    async(() -> ApiClient.delete("/api/playlists/" + id), r -> loadPlaylists());
                }, 80)
        );
        applyStyle(playlistsTable);

        playlistSongsTable.getColumns().addAll(
                strCol("Tytuł",   o -> str(o, "title"),          200),
                strCol("Artyści", o -> artists(o),               180),
                strCol("Czas",    o -> fmtMs(o, "duration_ms"),   80),
                strCol("Dodano",  o -> fmtDateTime(o, "added_at"), 150),
                strCol("Przez",   o -> str(o, "added_by"),         110),
                actionCol("Usuń", o -> {
                    if (currentPlaylistId < 0) return;
                    int songId = o.get("id").getAsInt();
                    async(() -> ApiClient.delete("/api/playlists/" + currentPlaylistId + "/songs/" + songId),
                          r -> refreshCurrentPlaylist());
                }, 70)
        );
        applyStyle(playlistSongsTable);
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

    @FXML
    private void addSongToPlaylist() {
        if (currentPlaylistId < 0) return;
        String raw = addSongIdField.getText().trim();
        if (raw.isEmpty()) return;
        int songId;
        try { songId = Integer.parseInt(raw); }
        catch (NumberFormatException e) { addSongIdField.setStyle("-fx-border-color: red;"); return; }
        addSongIdField.setStyle("");
        addSongBtn.setDisable(true);
        async(() -> ApiClient.post("/api/playlists/" + currentPlaylistId + "/songs",
                Map.of("song_id", songId)), res -> {
            addSongBtn.setDisable(false);
            addSongIdField.clear();
            refreshCurrentPlaylist();
        });
    }

    private void showPlaylistSongs(JsonObject playlist) {
        currentPlaylistId = playlist.get("id").getAsInt();
        playlistSongsLabel.setText("Playlista: " + str(playlist, "name"));
        playlistSongsTable.setVisible(true);
        playlistSongsTable.setManaged(true);
        addSongIdField.setVisible(true);
        addSongIdField.setManaged(true);
        addSongBtn.setVisible(true);
        addSongBtn.setManaged(true);
        refreshCurrentPlaylist();
    }

    private void refreshCurrentPlaylist() {
        if (currentPlaylistId < 0) return;
        async(() -> ApiClient.get("/api/playlists/" + currentPlaylistId), res -> {
            JsonObject obj = res.data().isJsonObject() ? res.data().getAsJsonObject() : new JsonObject();
            JsonElement songs = obj.has("songs") ? obj.get("songs") : new com.google.gson.JsonArray();
            playlistSongsTable.setItems(toList(songs));
        });
    }

    private String fmtDateTime(JsonObject o, String key) {
        String s = str(o, key);
        if ("—".equals(s) || s.length() < 19) return s;
        return s.substring(0, 10) + " " + s.substring(11, 19);
    }

    @Override
    public void refresh() { loadPlaylists(); }
}
