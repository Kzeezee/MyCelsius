package controller;

import firebase.FirebaseDB;
import firebase.IFirebaseDB;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import util.StateManager;

import static util.MyCelsiusUtils.changeSceneWithRefControl;

public class OrganisationCreation {

    @FXML
    TextField createOrgName;
    IFirebaseDB firebaseDB = new FirebaseDB();

    public void onCreateOrg() {
        // Create new org record and put into firebase
        try {
            String orgCode = firebaseDB.createOrganisation(createOrgName.getText());
            if (orgCode != null || orgCode.isEmpty()) {
                StateManager.setCurrentOrg(orgCode);
                // Move to success page
                changeSceneWithRefControl(createOrgName, getClass(), "/organisation_creation_success.fxml");
            } else {
                throw new RuntimeException("Something went wrong when generating organisation code. Code: " + orgCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
