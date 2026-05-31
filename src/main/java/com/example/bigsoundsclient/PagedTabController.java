package com.example.bigsoundsclient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public abstract class PagedTabController extends TabController {

    protected int currentPage = 1;
    protected int totalPages  = 1;
    protected int pageSize    = 25;

    private Label pageLabel;
    private Button prevBtn;
    private Button nextBtn;

    protected abstract void loadPage(int page, int limit);

    @Override
    public void refresh() {
        currentPage = 1;
        loadPage(currentPage, pageSize);
    }

    protected void applyPage(JsonElement response) {
        ObservableList<JsonObject> items = toList(response);
        if (response != null && response.isJsonObject()) {
            JsonObject meta = response.getAsJsonObject();
            currentPage = meta.has("page")  ? meta.get("page").getAsInt()  : 1;
            totalPages  = meta.has("pages") ? meta.get("pages").getAsInt() : 1;
        }
        updateTable(items);
        updatePaginationBar();
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
            loadPage(currentPage, pageSize);
        });

        prevBtn.setOnAction(e -> {
            if (currentPage > 1) loadPage(--currentPage, pageSize);
        });
        nextBtn.setOnAction(e -> {
            if (currentPage < totalPages) loadPage(++currentPage, pageSize);
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
