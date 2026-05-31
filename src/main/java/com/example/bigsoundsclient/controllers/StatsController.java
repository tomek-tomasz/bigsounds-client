package com.example.bigsoundsclient.controllers;

import com.example.bigsoundsclient.ApiClient;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class StatsController extends TabController implements Initializable {

    @FXML private DatePicker statsFrom;
    @FXML private DatePicker statsTo;
    @FXML private Button loadStatsBtn;
    @FXML private HBox statsCards;
    @FXML private TableView<JsonObject> topSongsTable;
    @FXML private TableView<JsonObject> topArtistsTable;
    @FXML private TableView<JsonObject> topReleasesTable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        topSongsTable.getColumns().addAll(
                strCol("#",          o -> str(o, "_rank"),      40),
                strCol("Tytuł",      o -> str(o, "title"),     190),
                strCol("Artyści",    o -> artists(o),          170),
                strCol("Odsłuchań", o -> str(o, "stream_count"), 100),
                strCol("Czas",       o -> fmtHours(o, "total_ms"), 80)
        );
        applyStyle(topSongsTable);

        topArtistsTable.getColumns().addAll(
                strCol("#",               o -> str(o, "_rank"),       40),
                strCol("Artysta",         o -> str(o, "stage_name"), 210),
                strCol("Odsłuchań",      o -> str(o, "stream_count"), 100),
                strCol("Czas",            o -> fmtHours(o, "total_ms"), 80),
                strCol("Unikalne utwory", o -> str(o, "unique_songs"), 120)
        );
        applyStyle(topArtistsTable);

        topReleasesTable.getColumns().addAll(
                strCol("#",         o -> str(o, "_rank"),       40),
                strCol("Tytuł",     o -> str(o, "title"),      180),
                strCol("Artyści",   o -> artists(o),           160),
                strCol("Format",    o -> str(o, "format"),      70),
                strCol("Odsłuchań",o -> str(o, "stream_count"), 100),
                strCol("Czas",      o -> fmtHours(o, "total_ms"), 80)
        );
        applyStyle(topReleasesTable);
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
        addCard("Streamy",          str(o, "stream_count"));
        addCard("Czas słuchania",   formatHoursStr(longVal(o, "total_ms")));
        addCard("Unikalne utwory",  str(o, "unique_songs"));
        addCard("Artyści",          str(o, "unique_artists"));
        addCard("Wydania",          str(o, "unique_releases"));
    }

    private void addCard(String label, String value) {
        VBox card = new VBox(4);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 14 18 14 18; " +
                      "-fx-border-color: #dde1e7; -fx-border-radius: 8;");
        card.setMinWidth(120);
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1db954;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #777;");
        card.getChildren().addAll(val, lbl);
        statsCards.getChildren().add(card);
    }
}
