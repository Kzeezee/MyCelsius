package firebase;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.hash.Bcrypt;
import com.google.firebase.cloud.FirestoreClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

// Firestore database
public class FirebaseDB implements IFirebaseDB {
    private Firestore db = FirestoreClient.getFirestore();

    @Override
    public Long registerUser(String email, String password) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("users").document();
        // Add document data with an additional field ("middle")
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        data.put("password", hashedPassword);

        ApiFuture<WriteResult> result = docRef.set(data);
        return result.get().getUpdateTime().getSeconds();
    }

    @Override
    public Long loginUser(String email, String password) throws ExecutionException, InterruptedException {
//        ApiFuture<QuerySnapshot> query = db.collection("users").getId();
        return null;
    }


}
