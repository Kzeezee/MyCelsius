package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.regex.Pattern;

public class MyCelsiusUtils {
    private static Pattern orgCodePattern = Pattern.compile("^[a-zA-Z0-9]*$");

    // Changes the current scene obtained with a control reference to the specified fxml path.
    public static void changeSceneWithRefControl(Node refControl, Class context, String fxmlPath) throws IOException {
        Stage stage = (Stage) refControl.getScene().getWindow();
        Parent root = FXMLLoader.load(context.getResource(fxmlPath));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static Boolean isValidOrganisationCode(String organisationCode) {
        return orgCodePattern.matcher(organisationCode).find();
    }
}
