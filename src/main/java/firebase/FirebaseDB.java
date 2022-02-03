package firebase;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

// Firestore database
public class FirebaseDB implements IFirebaseDB {
    private Firestore db = FirestoreClient.getFirestore();

    @Override
    public Timestamp registerUser(String email, String password) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("users").document();
        // Add document data with an additional field ("middle")
        Map<String, Object> data = new HashMap<>();
        data.put("email", email.trim());
        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        data.put("password", hashedPassword);

        ApiFuture<WriteResult> result = docRef.set(data);
        return result.get().getUpdateTime();
    }

    @Override
    public Long loginUser(String email, String password) throws ExecutionException, InterruptedException {
        // Create a reference to the cities collection
        CollectionReference users = db.collection("users");
        // Create a query against the collection.
        Query query = users.whereEqualTo("email", email.trim());
        // retrieve  query results asynchronously using query.get()
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        // Query user with email and validate correct email and password
        // For loop, though it should only expect one
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            String docEmail = document.getString("email");
            String docPassword = document.getString("password");
//            if (docEmail.equalsIgnoreCase(email) && )

        }
        return null;
    }

    private Boolean checkEmailAlreadyExists(String email) {
        return false;
    }

}
