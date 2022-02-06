package controller;

import exception.InvalidLoginCredentialsException;
import exception.UserDoesNotExistException;
import firebase.FirebaseDB;
import firebase.IFirebaseDB;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Duration;
import model.UserRecord;
import util.StateManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static util.Constants.EMAIL_REGEX_PATTERN;
import static util.MyCelsiusUtils.changeSceneWithRefControl;

public class AuthLogin implements Initializable {

    @FXML
    private TextField loginEmail;
    @FXML
    private PasswordField loginPassword;
    @FXML
    private Button loginButton;
    @FXML
    private Label registerRedirect;
    private final PseudoClass errorClass = PseudoClass.getPseudoClass("error");
    IFirebaseDB firebaseDB = new FirebaseDB();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loginButton.setOnAction(event -> onLoginSubmission());
    }

    // Redirects to register scene
    public void registerRedirect() throws IOException {
        changeSceneWithRefControl(loginEmail, getClass(), "/auth_register.fxml");
    }

    private void onLoginSubmission() {
        if (validateInputs()) {
            //submit to firebase
            String email = loginEmail.getText().trim().toLowerCase();
            String password = loginPassword.getText();
            try {
                UserRecord userRecord = firebaseDB.loginUser(email, password);
                // Set current session to the logged in user
                StateManager.setCurrentUser(userRecord);
                if (StateManager.isLoggedIn()) {
                    if (StateManager.getUserHasOrg()) {
                        // Go dashboard page
                        System.out.println("Login success");
                        changeSceneWithRefControl(loginEmail, getClass(), "/dashboard.fxml");
                    } else {
                        // Redirect to organisation creation page where user will create their initial organisation
                        System.out.println("Login success, heading to organisation creation");
                        changeSceneWithRefControl(loginEmail, getClass(), "/organisation_creation.fxml");
                    }
                }
            } catch (UserDoesNotExistException e) {
                setFieldsWithError("Account does not exist. Please enter a different account or create one.");
            } catch (InvalidLoginCredentialsException e) {
                setFieldsWithError("Invalid credentials. Please enter again.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setFieldsWithError(String errMsg) {
        loginEmail.pseudoClassStateChanged(errorClass, true);
        loginPassword.pseudoClassStateChanged(errorClass, true);
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(500));
        tooltip.setText(errMsg);
        loginEmail.setTooltip(tooltip);
        loginPassword.setTooltip(tooltip);
    }

    // TODO: Update validation to match logging in rather than registering
    private Boolean validateInputs() {
        String emailAddress = loginEmail.getText();
        String password = loginPassword.getText();
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(500));

        Boolean emailValid = false, passwordValid = false;
        if (!emailAddress.matches(EMAIL_REGEX_PATTERN)) {
            loginEmail.pseudoClassStateChanged(errorClass, true);
            tooltip.setText("Email must be valid");
            loginEmail.setTooltip(tooltip);
        } else {
            loginEmail.pseudoClassStateChanged(errorClass, false);
            loginEmail.setTooltip(null);
            emailValid = true;
        }
        if (password.length() < 8) {
            loginPassword.pseudoClassStateChanged(errorClass, true);
            tooltip.setText("Password must be at least 8 characters");
            loginPassword.setTooltip(tooltip);
        } else {
            loginPassword.pseudoClassStateChanged(errorClass, false);
            loginPassword.setTooltip(null);
            passwordValid = true;
        }
        return (emailValid && passwordValid);
    }

}
