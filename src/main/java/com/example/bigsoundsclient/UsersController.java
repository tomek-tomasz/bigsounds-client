package com.example.bigsoundsclient;

import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class UsersController extends TabController implements Initializable {

    @FXML private TableView<JsonObject> usersTable;
    @FXML private Button refreshUsersBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usersTable.getColumns().addAll(
                strCol("ID",          o -> str(o, "id"),          60),
                strCol("Użytkownik",  o -> str(o, "name"),        240),
                strCol("Dołączył/a",  o -> dateStr(o, "date_joined"), 120),
                actionCol("+ Obserwuj", o -> {
                    int id = o.get("id").getAsInt();
                    async(() -> ApiClient.post("/api/follows/users/" + id, null), r -> {});
                }, 110)
        );
        applyStyle(usersTable);
    }

    @FXML
    private void loadUsers() {
        refreshUsersBtn.setDisable(true);
        async(() -> ApiClient.get("/api/users"), res -> {
            refreshUsersBtn.setDisable(false);
            usersTable.setItems(toList(res.data()));
        });
    }
}
