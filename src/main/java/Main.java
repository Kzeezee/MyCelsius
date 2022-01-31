import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Reading environment variables
        // Set up telegram bot
        // Set up Firebase admin sdk

        initializeScene(primaryStage);
        MyCelsiusTelegramBot.setup();
        try {
            Firebase.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Start the application on the login screen first
    private void initializeScene(Stage primaryStage) {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("auth_login.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
