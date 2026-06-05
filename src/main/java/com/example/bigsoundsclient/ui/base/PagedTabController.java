package com.example.bigsoundsclient.ui.base;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public abstract class PagedTabController extends TabController {

    protected int    currentPage = 1;
    protected int    totalPages  = 1;
    protected int    pageSize    = 25;
    protected String sortField   = null;
    protected String sortDir     = "DESC";

    private Label  pageLabel;
    private Button prevBtn;
    private Button nextBtn;

    private final Map<TableColumn<JsonObject, ?>, String> sortableColumns   = new IdentityHashMap<>();
    private final Map<TableColumn<JsonObject, ?>, String> originalHeaders   = new IdentityHashMap<>();
    private TableColumn<JsonObject, ?> activeSortCol = null;
    private boolean suppressSort = false;
    private boolean keepPage     = false;

    private List<JsonObject> allItems = new ArrayList<>();
    private final ObservableList<JsonObject> backingList = FXCollections.observableArrayList();

    protected abstract void loadPage(int page, int limit);

    @Override
    public void refresh() {
        currentPage = 1;
        sortField   = null;
        sortDir     = "DESC";
        if (activeSortCol != null) {
            String orig = originalHeaders.get(activeSortCol);
            if (orig != null) activeSortCol.setText(orig);
            activeSortCol = null;
        }
        loadPage(1, 0);
    }

    protected void reloadCurrentPage() {
        keepPage = true;
        loadPage(1, 0);
    }

    protected String pageQs(int page, int limit) {
        return "?limit=9999";
    }

    protected void registerSort(TableColumn<JsonObject, ?> col, String apiField) {
        sortableColumns.put(col, apiField);
        originalHeaders.put(col, col.getText());
        col.setSortable(true);
    }

    protected void setTableItems(TableView<JsonObject> table, ObservableList<JsonObject> items) {
        if (table.getItems() != backingList) {
            table.setItems(backingList);
        }
        suppressSort = true;
        try {
            backingList.setAll(items);
        } finally {
            suppressSort = false;
        }
    }

    @SuppressWarnings("unchecked")
    protected void makeSortable(TableView<JsonObject> table) {
        for (TableColumn<JsonObject, ?> col : table.getColumns()) {
            if (!sortableColumns.containsKey(col)) {
                col.setSortable(false);
            }
        }

        table.setSortPolicy(tv -> {
            if (suppressSort) return true;

            if (!tv.getSortOrder().isEmpty()) {
                TableColumn<JsonObject, ?> col =
                        (TableColumn<JsonObject, ?>) tv.getSortOrder().get(0);
                String field = sortableColumns.get(col);
                if (field == null) return true;

                if (field.equals(sortField)) {
                    if ("ASC".equals(sortDir)) {
                        sortDir = "DESC";
                    } else {
                        sortField = null;
                        sortDir   = "DESC";
                    }
                } else {
                    sortField = field;
                    sortDir   = "ASC";
                }
            } else {
                sortField = null;
                sortDir   = "DESC";
            }

            suppressSort = true;
            tv.getSortOrder().clear();
            suppressSort = false;

            if (activeSortCol != null) {
                String orig = originalHeaders.get(activeSortCol);
                if (orig != null) activeSortCol.setText(orig);
                activeSortCol = null;
            }
            if (sortField != null) {
                for (Map.Entry<TableColumn<JsonObject, ?>, String> e : sortableColumns.entrySet()) {
                    if (e.getValue().equals(sortField)) {
                        activeSortCol = e.getKey();
                        String indicator = "ASC".equals(sortDir) ? "  ▲" : "  ▼";
                        activeSortCol.setText(originalHeaders.getOrDefault(activeSortCol, activeSortCol.getText()) + indicator);
                        break;
                    }
                }
            }

            currentPage = 1;
            applySortAndPage();
            return true;
        });
    }

    protected void applyPage(JsonElement response) {
        allItems = new ArrayList<>(toList(response));
        if (!keepPage) currentPage = 1;
        keepPage = false;
        applySortAndPage();
    }

    private void applySortAndPage() {
        List<JsonObject> sorted = new ArrayList<>(allItems);

        if (sortField != null) {
            final String field = sortField;
            final boolean asc  = "ASC".equals(sortDir);
            sorted.sort((a, b) -> {
                String va = rawStr(a, field);
                String vb = rawStr(b, field);
                try {
                    double da = Double.parseDouble(va);
                    double db = Double.parseDouble(vb);
                    return asc ? Double.compare(da, db) : Double.compare(db, da);
                } catch (NumberFormatException e) {
                    int cmp = va.compareToIgnoreCase(vb);
                    return asc ? cmp : -cmp;
                }
            });
        }

        totalPages  = Math.max(1, (sorted.size() + pageSize - 1) / pageSize);
        currentPage = Math.min(currentPage, totalPages);

        int from = (currentPage - 1) * pageSize;
        int to   = Math.min(from + pageSize, sorted.size());
        updateTable(FXCollections.observableArrayList(sorted.subList(from, to)));
        updatePaginationBar();
    }

    private String rawStr(JsonObject o, String field) {
        if (o == null || !o.has(field) || o.get(field).isJsonNull()) return "";
        return o.get(field).getAsString();
    }

    protected abstract void updateTable(ObservableList<JsonObject> items);

    protected HBox buildPaginationBar() {
        prevBtn   = navBtn("◀");
        nextBtn   = navBtn("▶");
        pageLabel = new Label();
        pageLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 13px; -fx-min-width: 90px; -fx-alignment: CENTER;");

        ComboBox<Integer> limitBox = new ComboBox<>();
        limitBox.getItems().addAll(10, 25, 50, 100);
        limitBox.setValue(pageSize);
        limitBox.setStyle("-fx-font-size: 12px;");
        limitBox.setOnAction(e -> {
            pageSize    = limitBox.getValue();
            currentPage = 1;
            applySortAndPage();
        });

        prevBtn.setOnAction(e -> {
            if (currentPage > 1) { currentPage--; applySortAndPage(); }
        });
        nextBtn.setOnAction(e -> {
            if (currentPage < totalPages) { currentPage++; applySortAndPage(); }
        });

        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-padding: 0;");
        Label perPage = new Label("Na stronie:");
        perPage.setStyle("-fx-text-fill: #555; -fx-font-size: 12px;");
        bar.getChildren().addAll(perPage, limitBox, prevBtn, pageLabel, nextBtn);
        updatePaginationBar();
        return bar;
    }

    private void updatePaginationBar() {
        if (pageLabel == null) return;
        pageLabel.setText("Strona " + currentPage + " / " + totalPages);
        if (prevBtn != null) prevBtn.setDisable(currentPage <= 1);
        if (nextBtn != null) nextBtn.setDisable(currentPage >= totalPages);
    }

    private Button navBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #333; " +
                     "-fx-border-color: #ced4da; -fx-border-radius: 5; -fx-background-radius: 5; " +
                     "-fx-padding: 5 12 5 12; -fx-cursor: hand; -fx-font-size: 13px;");
        return btn;
    }
}
