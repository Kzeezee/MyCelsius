package controller;

import firebase.FirebaseDB;
import firebase.IFirebaseDB;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import util.StateManager;

import static util.Constants.EMAIL_REGEX_PATTERN;
import static util.MyCelsiusUtils.changeSceneWithRefControl;

public class OrganisationCreation {

    @FXML
    TextField createOrgName;
    private final PseudoClass errorClass = PseudoClass.getPseudoClass("error");
    IFirebaseDB firebaseDB = new FirebaseDB();

    public void onCreateOrg() {
        if (validateInputs()) {
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

    private Boolean validateInputs() {
        String orgName = createOrgName.getText().trim();
        Tooltip orgNameTooltip = new Tooltip();
        orgNameTooltip.setShowDelay(Duration.millis(100));

        Boolean orgNameValid = false;
        if (orgName.isEmpty()) {
            createOrgName.pseudoClassStateChanged(errorClass, true);
            orgNameTooltip.setText("Organisation name cannot be empty");
            createOrgName.setTooltip(orgNameTooltip);
        } else {
            createOrgName.pseudoClassStateChanged(errorClass, false);
            createOrgName.setTooltip(null);
            orgNameValid = true;
        }
        return orgNameValid;
    }
}
