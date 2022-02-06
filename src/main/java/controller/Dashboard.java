package controller;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import util.StateManager;

import java.io.IOException;

import static util.MyCelsiusUtils.changeSceneWithRefControl;

public class Dashboard {

    @FXML
    private ImageView menuLogout;
    @FXML
    private BorderPane contentContainer;

    @FXML
    public void initialize() {
        // Init with home tab
        try {
            homeTab();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void homeTab() throws IOException {
        contentContainer.setCenter(new TabHome());
    }

    public void addMemberTab() throws IOException {
        contentContainer.setCenter(new TabAddMember());
    }

    public void logout() throws IOException {
        StateManager.logout();
        changeSceneWithRefControl(menuLogout, getClass(), "/auth_login.fxml");
    }
}
