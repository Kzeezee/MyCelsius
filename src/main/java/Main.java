import bot.MyCelsiusTelegramBot;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            initializeScene(primaryStage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MyCelsiusTelegramBot.setup();
    }

    // Start the application on the login screen first
    private void initializeScene(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("dashboard.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("P1919488 - MyCelsius");
        // Shuts down the telegram bot server as well since otherwise bot will keep running resulting in duplicate instance
        primaryStage.setOnCloseRequest(event -> {
            System.exit(0);
        });
        primaryStage.show();
    }
}
