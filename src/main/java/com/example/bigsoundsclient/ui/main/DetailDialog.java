package com.example.bigsoundsclient.ui.main;

import com.example.bigsoundsclient.api.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DetailDialog {

    public static void showSong(JsonObject song) {
        int id = song.get("id").getAsInt();
        show("Szczegóły utworu",
             buildBaseInfo(song, true),
             "/api/reviews/songs/" + id,
             id);
    }

    public static void showRelease(JsonObject release) {
        int id = release.get("id").getAsInt();
        show("Szczegóły wydania",
             buildBaseInfo(release, false),
             "/api/reviews/releases/" + id,
             id);
    }

    private static void show(String title, VBox baseInfo,
                              String reviewsEndpoint, int itemId) {
        Stage dialog = new Stage();
        dialog.setTitle(title);
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox content = new VBox(14);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: #f0f2f5;");
        content.getChildren().add(baseInfo);

        content.getChildren().add(buildRatingSection(reviewsEndpoint, () -> {
            int idx = content.getChildren().size() - 1;
            refreshReviews(content, idx, reviewsEndpoint);
        }));

        Label reviewsTitle = new Label("Recenzje");
        reviewsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        content.getChildren().add(reviewsTitle);

        VBox reviewsBox = new VBox(8);
        reviewsBox.setStyle(cardStyle());
        reviewsBox.getChildren().add(loadingLabel());
        content.getChildren().add(reviewsBox);

        loadReviews(reviewsBox, reviewsEndpoint);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #f0f2f5; -fx-background: #f0f2f5;");

        dialog.setScene(new Scene(scroll, 560, 580));
        dialog.show();
    }

    private static VBox buildRatingSection(String reviewsEndpoint, Runnable onSuccess) {
        VBox box = new VBox(10);
        box.setStyle(cardStyle());

        Label title = new Label("Wystaw ocenę");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Spinner<Integer> spinner = new Spinner<>(1, 100, 50);
        spinner.setEditable(true);
        spinner.setPrefWidth(90);
        spinner.getEditor().setStyle("-fx-font-size: 13px;");

        Button rateBtn = new Button("Oceń");
        rateBtn.setStyle("-fx-background-color: #1db954; -fx-text-fill: white; -fx-font-weight: bold; " +
                         "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 6 18 6 18; -fx-cursor: hand;");

        Label feedback = new Label("");
        feedback.setStyle("-fx-font-size: 12px;");

        rateBtn.setOnAction(e -> {
            int score;
            try {
                String text = spinner.getEditor().getText().trim();
                score = Integer.parseInt(text);
                if (score < 1 || score > 100) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                feedback.setStyle("-fx-text-fill: #c0392b; -fx-font-size: 12px;");
                feedback.setText("Podaj liczbę od 1 do 100.");
                return;
            }

            rateBtn.setDisable(true);
            feedback.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");
            feedback.setText("Wysyłanie…");

            final int finalScore = score;
            CompletableFuture.supplyAsync(
                    () -> ApiClient.post(reviewsEndpoint, Map.of("score", finalScore))
            ).thenAccept(res -> Platform.runLater(() -> {
                rateBtn.setDisable(false);
                if (res.ok()) {
                    feedback.setStyle("-fx-text-fill: #1db954; -fx-font-size: 12px;");
                    feedback.setText("✓ Ocena wystawiona: " + finalScore + " / 100");
                    onSuccess.run();
                } else {
                    feedback.setStyle("-fx-text-fill: #c0392b; -fx-font-size: 12px;");
                    String msg = "Błąd.";
                    try { msg = res.data().getAsJsonObject().get("error").getAsString(); }
                    catch (Exception ignored) {}
                    feedback.setText(msg);
                }
            }));
        });

        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        Label scaleLbl = new Label("Skala 1–100:");
        scaleLbl.setStyle("-fx-text-fill: #555; -fx-font-size: 13px;");
        controls.getChildren().addAll(scaleLbl, spinner, rateBtn);

        box.getChildren().addAll(title, controls, feedback);
        return box;
    }

    private static void loadReviews(VBox reviewsBox, String reviewsEndpoint) {
        CompletableFuture.supplyAsync(() -> ApiClient.get(reviewsEndpoint))
                .thenAccept(res -> Platform.runLater(() -> {
                    reviewsBox.getChildren().clear();
                    renderReviews(reviewsBox, res);
                }));
    }

    private static void refreshReviews(VBox content, int reviewsBoxIdx, String reviewsEndpoint) {
        if (reviewsBoxIdx < 0 || reviewsBoxIdx >= content.getChildren().size()) return;
        if (!(content.getChildren().get(reviewsBoxIdx) instanceof VBox reviewsBox)) return;
        reviewsBox.getChildren().clear();
        reviewsBox.getChildren().add(loadingLabel());
        loadReviews(reviewsBox, reviewsEndpoint);
    }

    private static void renderReviews(VBox box, ApiClient.ApiResponse res) {
        if (!res.ok() || !res.data().isJsonObject()) {
            box.getChildren().add(label("Brak danych.", "#888"));
            return;
        }
        JsonObject data = res.data().getAsJsonObject();
        double avg = data.has("avg_score") && !data.get("avg_score").isJsonNull()
                ? data.get("avg_score").getAsDouble() : -1;
        int count = data.has("review_count") ? data.get("review_count").getAsInt() : 0;

        HBox meta = new HBox(12);
        meta.setAlignment(Pos.CENTER_LEFT);
        meta.getChildren().addAll(
                statCard("Średnia ocena", avg >= 0 ? String.format("%.1f / 100", avg) : "—"),
                statCard("Liczba ocen",   String.valueOf(count))
        );
        box.getChildren().add(meta);

        if (data.has("reviews") && data.get("reviews").isJsonArray()) {
            JsonArray reviews = data.get("reviews").getAsJsonArray();
            if (reviews.isEmpty()) {
                box.getChildren().add(label("Brak ocen.", "#888"));
            } else {
                for (JsonElement e : reviews) {
                    box.getChildren().add(reviewRow(e.getAsJsonObject()));
                }
            }
        }
    }

    private static VBox buildBaseInfo(JsonObject o, boolean isSong) {
        VBox box = new VBox(10);
        box.setStyle(cardStyle());

        Label title = new Label(strVal(o, "title"));
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a; -fx-wrap-text: true;");
        box.getChildren().add(title);
        box.getChildren().add(infoRow("Artyści", artistsStr(o)));

        if (isSong) {
            box.getChildren().add(infoRow("Czas", fmtMs(o)));
        } else {
            box.getChildren().addAll(
                    infoRow("Format", strVal(o, "format")),
                    infoRow("Data",   dateStr(o, "release_date"))
            );
        }
        box.getChildren().addAll(
                infoRow("Polubień",     countStr(o, "like_count")),
                infoRow("Streamów",     countStr(o, "stream_count")),
                infoRow("Twoja ocena",  myScore(o)),
                infoRow("Polubiony",    likedStr(o))
        );
        return box;
    }

    private static HBox infoRow(String key, String value) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Label k = new Label(key + ":");
        k.setStyle("-fx-text-fill: #777; -fx-font-size: 13px; -fx-min-width: 100;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill: #111; -fx-font-size: 13px; -fx-font-weight: bold; -fx-wrap-text: true;");
        row.getChildren().addAll(k, v);
        return row;
    }

    private static HBox reviewRow(JsonObject r) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 0, 6, 0));
        row.setStyle("-fx-border-color: transparent transparent #eee transparent; -fx-border-width: 0 0 1 0;");

        String score = r.has("score") && !r.get("score").isJsonNull()
                ? r.get("score").getAsInt() + " / 100" : "—";
        String user  = r.has("username") ? r.get("username").getAsString() : "—";
        String date  = r.has("date_added") && r.get("date_added").getAsString().length() >= 10
                ? r.get("date_added").getAsString().substring(0, 10) : "—";

        Label scoreLbl = new Label(score);
        scoreLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1db954; -fx-min-width: 80;");
        Label userLbl  = new Label(user);
        userLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333; -fx-min-width: 130;");
        Label dateLbl  = new Label(date);
        dateLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

        row.getChildren().addAll(scoreLbl, userLbl, dateLbl);
        return row;
    }

    private static VBox statCard(String label, String value) {
        VBox card = new VBox(3);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; " +
                      "-fx-border-color: #dde1e7; -fx-border-radius: 8; -fx-padding: 10 18 10 18;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1db954;");
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 11px; -fx-text-fill: #777;");
        card.getChildren().addAll(v, l);
        return card;
    }

    private static Label label(String text, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 13px;");
        return l;
    }

    private static Label loadingLabel() {
        return label("Ładowanie…", "#888");
    }

    private static String cardStyle() {
        return "-fx-background-color: white; -fx-background-radius: 8; " +
               "-fx-border-color: #dde1e7; -fx-border-radius: 8; -fx-padding: 16;";
    }

    private static String strVal(JsonObject o, String key) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull()) return "—";
        return o.get(key).getAsString();
    }

    private static String artistsStr(JsonObject o) {
        if (!o.has("artists") || !o.get("artists").isJsonArray()) return "—";
        ArrayList<String> list = new ArrayList<>();
        for (JsonElement e : o.get("artists").getAsJsonArray()) {
            list.add(e.isJsonPrimitive() ? e.getAsString() : strVal(e.getAsJsonObject(), "stage_name"));
        }
        return list.isEmpty() ? "—" : String.join(", ", list);
    }

    private static String fmtMs(JsonObject o) {
        if (!o.has("duration_ms") || o.get("duration_ms").isJsonNull()) return "—";
        long ms = o.get("duration_ms").getAsLong();
        return String.format("%d:%02d", ms / 60000, (ms % 60000) / 1000);
    }

    private static String countStr(JsonObject o, String key) {
        if (!o.has(key) || o.get(key).isJsonNull()) return "—";
        try { return String.format("%,d", o.get(key).getAsLong()); }
        catch (Exception e) { return "—"; }
    }

    private static String myScore(JsonObject o) {
        if (!o.has("my_score") || o.get("my_score").isJsonNull()) return "—";
        return o.get("my_score").getAsInt() + " / 100";
    }

    private static String likedStr(JsonObject o) {
        if (!o.has("liked")) return "—";
        return o.get("liked").getAsBoolean() ? "✓ Tak" : "Nie";
    }

    private static String dateStr(JsonObject o, String key) {
        String s = strVal(o, key);
        return ("—".equals(s) || s.length() < 10) ? s : s.substring(0, 10);
    }
}
