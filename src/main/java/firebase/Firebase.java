package firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.FileInputStream;
import java.io.IOException;

public class Firebase {

    public static void init() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            Dotenv dotenv = Dotenv.load();
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                            new FileInputStream(dotenv.get("SERVICE_ACCOUNT_JSON_PATH")))
                    .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();
            FirebaseApp.initializeApp(options);
        }
    }
}