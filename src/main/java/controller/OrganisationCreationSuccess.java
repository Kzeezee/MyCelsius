package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import util.StateManager;

import java.io.IOException;

import static util.MyCelsiusUtils.changeSceneWithRefControl;

public class OrganisationCreationSuccess {
    @FXML
    TextField createOrgSuccessCode;

    @FXML
    public void initialize() {
        createOrgSuccessCode.setText(StateManager.getCurrentOrg());
        createOrgSuccessCode.setEditable(false);
    }

    public void onConfirm() {
        // Redirect user to dashboard
        try {
            changeSceneWithRefControl(createOrgSuccessCode, getClass(), "/dashboard.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
