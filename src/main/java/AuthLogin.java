import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static util.Constants.EMAIL_REGEX_PATTERN;

public class AuthLogin implements Initializable {

    @FXML
    private TextField loginEmail;
    @FXML
    private TextField loginPassword;
    @FXML
    private Button loginButton;
    @FXML
    private Label registerRedirect;

    private final PseudoClass errorClass = PseudoClass.getPseudoClass("error");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loginButton.setOnAction(event -> onLoginSubmission());
    }

    // Redirects to register scene
    public void registerRedirect() throws IOException {
        Stage stage = (Stage) loginEmail.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("auth_register.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void onLoginSubmission() {
        System.out.println(validateInputs());
        // Authenticate with firebase
    }

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
