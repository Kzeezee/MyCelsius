package controller;

import com.google.cloud.Timestamp;
import exception.NotUniqueEmailException;
import firebase.FirebaseDB;
import firebase.IFirebaseDB;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import util.MyCelsiusUtils;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static util.Constants.EMAIL_REGEX_PATTERN;
import static util.MyCelsiusUtils.changeSceneWithRefControl;

public class AuthRegister {

    @FXML
    private TextField registerEmail;
    @FXML
    private PasswordField registerPassword;
    @FXML
    private PasswordField registerConfirmPass;
    @FXML
    private Button registerButton;
    @FXML
    private Label loginRedirect;
    IFirebaseDB firebaseDB = new FirebaseDB();

    private final PseudoClass errorClass = PseudoClass.getPseudoClass("error");

    // Redirects to login scene
    public void loginRedirect() throws IOException {
        changeSceneWithRefControl(registerEmail, getClass(), "/auth_login.fxml");
    }

    public void onRegisterSubmission() {
        if (validateInputs()) {
            //submit to firebase
            String email = registerEmail.getText().trim().toLowerCase();
            String password = registerPassword.getText();
            try {
                Timestamp timestamp = firebaseDB.registerUser(email, password);
                if (timestamp != null) {
                    System.out.println(timestamp);
                    System.out.println("Registration success");
                    Dialog registrationSuccessDialog = createRegistrationSuccessDialog();
                    registrationSuccessDialog.show();
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (NotUniqueEmailException e) {
                handleExistingEmail(e.getMessage());
            }
        }
    }

    private void handleExistingEmail(String errMsg) {
        registerEmail.pseudoClassStateChanged(errorClass, true);
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(100));
        tooltip.setText(errMsg);
        registerEmail.setTooltip(tooltip);
    }

    // True if validation has pass
    private Boolean validateInputs() {
        String emailAddress = registerEmail.getText();
        String password = registerPassword.getText();
        String confirmPass = registerConfirmPass.getText();
        Tooltip emailTooltip = new Tooltip(), passwordTooltip = new Tooltip();
        emailTooltip.setShowDelay(Duration.millis(100));
        passwordTooltip.setShowDelay(Duration.millis(100));

        Boolean emailValid = false, passwordValid = false;
        if (!emailAddress.matches(EMAIL_REGEX_PATTERN)) {
            registerEmail.pseudoClassStateChanged(errorClass, true);
            emailTooltip.setText("Email must be valid");
            registerEmail.setTooltip(emailTooltip);
        } else {
            registerEmail.pseudoClassStateChanged(errorClass, false);
            registerEmail.setTooltip(null);
            emailValid = true;
        }
        if (password.length() < 8) {
            registerPassword.pseudoClassStateChanged(errorClass, true);
            passwordTooltip.setText("Password must be at least 8 characters");
            registerPassword.setTooltip(passwordTooltip);
        } else if (!password.equals(confirmPass)) {
            registerPassword.pseudoClassStateChanged(errorClass, true);
            registerConfirmPass.pseudoClassStateChanged(errorClass, true);
            passwordTooltip.setText("Passwords must match");
            registerPassword.setTooltip(passwordTooltip);
            registerConfirmPass.setTooltip(passwordTooltip);
        } else {
            registerPassword.pseudoClassStateChanged(errorClass, false);
            registerPassword.setTooltip(null);
            registerConfirmPass.pseudoClassStateChanged(errorClass, false);
            registerConfirmPass.setTooltip(null);
            passwordValid = true;
        }
        System.out.println((emailValid && passwordValid));
        return (emailValid && passwordValid);
    }

    private Dialog createRegistrationSuccessDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderText("Success");
        dialog.setContentText("Account has been successfully registered. Please login again to access the application.");
        ButtonType btn = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(btn);
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(btn);
        okBtn.setOnAction(event -> {
            try {
                MyCelsiusUtils.changeSceneWithRefControl(registerEmail, getClass(), "/auth_login.fxml");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return dialog;
    }
}
