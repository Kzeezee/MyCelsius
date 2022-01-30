import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

import static util.Constants.EMAIL_REGEX_PATTERN;

public class AuthRegister {

    @FXML
    private TextField registerEmail;
    @FXML
    private TextField registerPassword;
    @FXML
    private TextField registerConfirmPass;
    @FXML
    private Button registerButton;
    @FXML
    private Label loginRedirect;

    private final PseudoClass errorClass = PseudoClass.getPseudoClass("error");

    // Redirects to login scene
    public void loginRedirect() throws IOException {
        Stage stage = (Stage) registerEmail.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("auth_login.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private Boolean validateInputs() {
        String emailAddress = registerEmail.getText();
        String password = registerPassword.getText();
        String confirmPass = registerConfirmPass.getText();
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(500));

        Boolean emailValid = false, passwordValid = false;
        if (!emailAddress.matches(EMAIL_REGEX_PATTERN)) {
            registerEmail.pseudoClassStateChanged(errorClass, true);
            tooltip.setText("Email must be valid");
            registerEmail.setTooltip(tooltip);
        } else {
            registerEmail.pseudoClassStateChanged(errorClass, false);
            registerEmail.setTooltip(null);
            emailValid = true;
        }
        if (password.length() < 8) {
            registerPassword.pseudoClassStateChanged(errorClass, true);
            tooltip.setText("Password must be at least 8 characters");
            registerPassword.setTooltip(tooltip);
        } else {
            registerPassword.pseudoClassStateChanged(errorClass, false);
            registerPassword.setTooltip(null);
            passwordValid = true;
        }
        return (emailValid && passwordValid);
    }
}
