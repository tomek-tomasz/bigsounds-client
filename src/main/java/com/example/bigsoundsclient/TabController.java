package com.example.bigsoundsclient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class TabController {

    public void refresh() {}


    // ─── ASYNC ───────────────────────────────────────────────────────────────

    @FunctionalInterface
    public interface Task { ApiClient.ApiResponse run(); }

    @FunctionalInterface
    public interface Callback { void accept(ApiClient.ApiResponse res); }

    protected void async(Task task, Callback callback) {
        CompletableFuture.supplyAsync(task::run)
                .thenAccept(res -> Platform.runLater(() -> callback.accept(res)));
    }

    // ─── TABLE HELPERS ───────────────────────────────────────────────────────

    // Sortuje numerycznie (double), wyświetla "—" dla null
    @SuppressWarnings("unchecked")
    protected TableColumn<JsonObject, Number> scoreCol(String header, String jsonKey, double width) {
        TableColumn<JsonObject, Number> col = new TableColumn<>(header);
        col.setCellValueFactory(cd -> {
            JsonObject o = cd.getValue();
            if (!o.has(jsonKey) || o.get(jsonKey).isJsonNull()) return new SimpleDoubleProperty(-1);
            try { return new SimpleDoubleProperty(o.get(jsonKey).getAsDouble()); }
            catch (Exception e) { return new SimpleDoubleProperty(-1); }
        });
        col.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Number n, boolean empty) {
                super.updateItem(n, empty);
                setText(empty || n == null || n.doubleValue() < 0 ? null
                        : String.format("%.0f", n.doubleValue()));
            }
        });
        col.setPrefWidth(width);
        return col;
    }

    // Sortuje po ms (numerycznie), wyświetla M:SS
    @SuppressWarnings("unchecked")
    protected TableColumn<JsonObject, Number> durationCol(String header, String msKey, double width) {
        TableColumn<JsonObject, Number> col = new TableColumn<>(header);
        col.setCellValueFactory(cd -> {
            JsonObject o = cd.getValue();
            if (!o.has(msKey) || o.get(msKey).isJsonNull()) return new SimpleLongProperty(-1);
            try { return new SimpleLongProperty(o.get(msKey).getAsLong()); }
            catch (Exception e) { return new SimpleLongProperty(-1); }
        });
        col.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Number n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null || n.longValue() < 0) { setText(null); return; }
                long ms = n.longValue();
                setText(String.format("%d:%02d", ms / 60000, (ms % 60000) / 1000));
            }
        });
        col.setPrefWidth(width);
        return col;
    }

    // Sortuje ms numerycznie, wyświetla "Xh Ym"
    @SuppressWarnings("unchecked")
    protected TableColumn<JsonObject, Number> hoursCol(String header, String msKey, double width) {
        TableColumn<JsonObject, Number> col = new TableColumn<>(header);
        col.setCellValueFactory(cd -> {
            JsonObject o = cd.getValue();
            if (!o.has(msKey) || o.get(msKey).isJsonNull()) return new SimpleLongProperty(-1);
            try { return new SimpleLongProperty(o.get(msKey).getAsLong()); }
            catch (Exception e) { return new SimpleLongProperty(-1); }
        });
        col.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Number n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null || n.longValue() < 0) { setText(null); return; }
                long ms = n.longValue();
                long h = ms / 3600000, m = (ms % 3600000) / 60000;
                setText(h > 0 ? h + "h " + m + "m" : m + "m");
            }
        });
        col.setPrefWidth(width);
        return col;
    }

    @FunctionalInterface
    protected interface ValFn { String get(JsonObject o); }

    @SuppressWarnings("unchecked")
    protected TableColumn<JsonObject, String> strCol(String header, ValFn fn, double width) {
        TableColumn<JsonObject, String> col = new TableColumn<>(header);
        col.setCellValueFactory(cd -> new SimpleStringProperty(fn.get(cd.getValue())));
        col.setPrefWidth(width);
        return col;
    }

    @SuppressWarnings("unchecked")
    protected TableColumn<JsonObject, Number> numCol(String header, String jsonKey, double width) {
        TableColumn<JsonObject, Number> col = new TableColumn<>(header);
        col.setCellValueFactory(cd -> {
            JsonObject o = cd.getValue();
            long val = (!o.has(jsonKey) || o.get(jsonKey).isJsonNull()) ? 0L
                       : o.get(jsonKey).getAsLong();
            return new SimpleLongProperty(val);
        });
        col.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Number n, boolean empty) {
                super.updateItem(n, empty);
                setText(empty || n == null ? null : String.valueOf(n.longValue()));
            }
        });
        col.setPrefWidth(width);
        return col;
    }

    @SuppressWarnings("unchecked")
    protected TableColumn<JsonObject, String> actionCol(String label, Consumer<JsonObject> action, double width) {
        TableColumn<JsonObject, String> col = new TableColumn<>("");
        col.setPrefWidth(width);
        col.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button(label);
            {
                btn.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-cursor: hand; " +
                             "-fx-border-color: #ccc; -fx-border-radius: 4; -fx-background-radius: 4; " +
                             "-fx-padding: 3 10 3 10; -fx-font-size: 11px;");
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

    @SuppressWarnings("unchecked")
    protected TableColumn<JsonObject, String> likeToggleCol(
            String likeEndpoint, String unlikeEndpoint, Runnable onReload) {
        TableColumn<JsonObject, String> col = new TableColumn<>("");
        col.setPrefWidth(115);
        col.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button();
            {
                btn.setStyle("-fx-cursor: hand; -fx-border-radius: 4; -fx-background-radius: 4; " +
                             "-fx-padding: 3 10 3 10; -fx-font-size: 11px;");
                btn.setOnAction(e -> {
                    JsonObject item = getTableView().getItems().get(getIndex());
                    if (item == null) return;
                    boolean liked = item.has("liked") && item.get("liked").getAsBoolean();
                    String endpoint = liked ? unlikeEndpoint + item.get("id").getAsInt()
                                            :   likeEndpoint + item.get("id").getAsInt();
                    String method = liked ? "DELETE" : "POST";
                    btn.setDisable(true);
                    async(() -> "DELETE".equals(method)
                                    ? ApiClient.delete(endpoint)
                                    : ApiClient.post(endpoint, null),
                         r -> { btn.setDisable(false); onReload.run(); });
                });
            }
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty) { setGraphic(null); return; }
                JsonObject item = getTableView().getItems().get(getIndex());
                boolean liked = item != null && item.has("liked") && item.get("liked").getAsBoolean();
                btn.setText(liked ? "💔 Przestań lubić" : "♥ Polub");
                btn.setStyle("-fx-cursor: hand; -fx-border-radius: 4; -fx-background-radius: 4; " +
                             "-fx-padding: 3 10 3 10; -fx-font-size: 11px; " +
                             (liked ? "-fx-background-color: #fde8e8; -fx-text-fill: #c0392b; -fx-border-color: #f5b7b1;"
                                    : "-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-border-color: #ccc;"));
                setGraphic(btn);
            }
        });
        return col;
    }

    @SuppressWarnings("unchecked")
    protected TableColumn<JsonObject, String> followToggleCol(
            String followEndpoint, String unfollowEndpoint,
            String followedField) {
        TableColumn<JsonObject, String> col = new TableColumn<>("");
        col.setPrefWidth(130);
        col.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button();
            {
                btn.setStyle("-fx-cursor: hand; -fx-border-radius: 4; -fx-background-radius: 4; " +
                             "-fx-padding: 3 10 3 10; -fx-font-size: 11px;");
                btn.setOnAction(e -> {
                    JsonObject item = getTableView().getItems().get(getIndex());
                    if (item == null) return;
                    boolean followed = item.has(followedField) && item.get(followedField).getAsBoolean();
                    String endpoint = followed
                            ? unfollowEndpoint + item.get("id").getAsInt()
                            :   followEndpoint + item.get("id").getAsInt();
                    btn.setDisable(true);
                    async(() -> followed ? ApiClient.delete(endpoint) : ApiClient.post(endpoint, null),
                         r -> {
                             if (r.ok()) {
                                 item.addProperty(followedField, !followed);
                                 getTableView().refresh();
                             }
                             btn.setDisable(false);
                         });
                });
            }
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty) { setGraphic(null); return; }
                JsonObject item = getTableView().getItems().get(getIndex());
                boolean followed = item != null && item.has(followedField)
                                   && item.get(followedField).getAsBoolean();
                btn.setText(followed ? "✓ Obserwujesz" : "+ Obserwuj");
                btn.setStyle("-fx-cursor: hand; -fx-border-radius: 4; -fx-background-radius: 4; " +
                             "-fx-padding: 3 10 3 10; -fx-font-size: 11px; " +
                             (followed
                                ? "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-border-color: #a5d6a7;"
                                : "-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-border-color: #ccc;"));
                setGraphic(btn);
            }
        });
        return col;
    }

    protected void applyStyle(TableView<?> tv) {
        tv.setStyle("-fx-background-color: white; -fx-control-inner-background: white; " +
                    "-fx-table-cell-border-color: #eee;");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    // ─── JSON HELPERS ────────────────────────────────────────────────────────

    protected ObservableList<JsonObject> toList(JsonElement el) {
        if (el == null) return FXCollections.observableArrayList();
        JsonElement array = el;
        if (el.isJsonObject() && el.getAsJsonObject().has("data")) {
            array = el.getAsJsonObject().get("data");
        }
        List<JsonObject> list = new ArrayList<>();
        if (array.isJsonArray()) {
            for (JsonElement e : array.getAsJsonArray()) {
                if (e.isJsonObject()) list.add(e.getAsJsonObject());
            }
        }
        return FXCollections.observableArrayList(list);
    }

    protected ObservableList<JsonObject> withRank(ObservableList<JsonObject> list) {
        for (int i = 0; i < list.size(); i++) list.get(i).addProperty("_rank", i + 1);
        return list;
    }

    protected String str(JsonObject o, String key) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull()) return "—";
        return o.get(key).getAsString();
    }

    protected boolean bool(JsonObject o, String key) {
        if (o == null || !o.has(key)) return false;
        try { return o.get(key).getAsBoolean(); } catch (Exception e) { return false; }
    }

    protected long longVal(JsonObject o, String key) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull()) return 0;
        try { return o.get(key).getAsLong(); } catch (Exception e) { return 0; }
    }

    protected String artists(JsonObject o) {
        if (o == null || !o.has("artists") || !o.get("artists").isJsonArray()) return "—";
        List<String> names = new ArrayList<>();
        for (JsonElement e : o.get("artists").getAsJsonArray()) {
            names.add(e.isJsonPrimitive() ? e.getAsString() : str(e.getAsJsonObject(), "stage_name"));
        }
        return names.isEmpty() ? "—" : String.join(", ", names);
    }

    protected String fmtMs(JsonObject o, String key) {
        long ms = longVal(o, key);
        if (ms <= 0) return "—";
        long min = ms / 60000, sec = (ms % 60000) / 1000;
        return String.format("%d:%02d", min, sec);
    }

    protected String fmtHours(JsonObject o, String key) {
        return formatHoursStr(longVal(o, key));
    }

    protected String formatHoursStr(long ms) {
        if (ms <= 0) return "0h";
        long hours = ms / 3600000, min = (ms % 3600000) / 60000;
        return hours > 0 ? hours + "h " + min + "m" : min + "m";
    }

    protected String dateStr(JsonObject o, String key) {
        String s = str(o, key);
        return ("—".equals(s) || s.length() < 10) ? s : s.substring(0, 10);
    }

    protected String buildQs(String from, String to, int limit) {
        List<String> parts = new ArrayList<>();
        parts.add("limit=" + limit);
        if (from != null) parts.add("from=" + from);
        if (to   != null) parts.add("to=" + to);
        return "?" + String.join("&", parts);
    }
}
