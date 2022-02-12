import bot.MyCelsiusTelegramBot;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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
        Parent root = FXMLLoader.load(getClass().getResource("auth_login.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image("/assets/MyCelsiusLogo.png"));
        primaryStage.setTitle("MyCelsius by Goh Xuan Yu Oliver(P1919488)");
        // Shuts down the telegram bot server as well since otherwise bot will keep running resulting in duplicate instance
        primaryStage.setOnCloseRequest(event -> {
            System.exit(0);
        });
        primaryStage.show();
    }
}
