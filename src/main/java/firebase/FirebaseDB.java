package firebase;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import exception.NotUniqueEmailException;
import exception.UserDoesNotExist;
import util.StateManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static util.Constants.INVALID_CREDENTIALS;

// Firestore database
public class FirebaseDB implements IFirebaseDB {
    private Firestore db;

    public FirebaseDB() {
        try {
            Firebase.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.db = FirestoreClient.getFirestore();
    }

    @Override
    public Timestamp registerUser(String email, String password) throws ExecutionException, InterruptedException, NotUniqueEmailException {
        email = email.trim();
        if (checkNotDuplicateEmail(email)) {
            // Acquiring data
            DocumentReference docRef = db.collection("users").document();
            Map<String, Object> data = new HashMap<>();
            data.put("email", email);
            String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
            data.put("password", hashedPassword);

            // Setting the data and checking if successful
            ApiFuture<WriteResult> result = docRef.set(data);
            if (result != null) {
                return result.get().getUpdateTime();
            } else {
                throw new RuntimeException("Unexpected error occurred when retrieving write result from database.");
            }
        } else {
            throw new NotUniqueEmailException("Email already exists: " + email);
        }
    }

    @Override
    public String loginUser(String email, String password) throws ExecutionException, InterruptedException, UserDoesNotExist {
        email = email.trim();

        QueryDocumentSnapshot userDoc = getUser(email);
        if (userDoc != null) {
            String docEmail = userDoc.getString("email");
            String docPassword = userDoc.getString("password");
            if (docEmail.equalsIgnoreCase(email) && BCrypt.verifyer().verify(password.getBytes(), docPassword.getBytes()).verified) {
                // Valid match
                String userId = userDoc.getId();
                StateManager.setCurrentUserId(userId);
                return StateManager.getCurrentUserId();
            } else {
                // Invalid match
                return INVALID_CREDENTIALS;
            }
        } else {
            throw new UserDoesNotExist("User does not exist! User: " + email);
        }
    }

    private QueryDocumentSnapshot getUser(String email) throws ExecutionException, InterruptedException {
        email = email.trim();
        CollectionReference users = db.collection("users");
        // Create a query against the collection.
        Query query = users.whereEqualTo("email", email.trim()).limit(1);
        List<QueryDocumentSnapshot> documentSnapshots = query.get().get().getDocuments();
        if (!documentSnapshots.isEmpty()) {
            return documentSnapshots.get(0);
        } else {
            return null;
        }
    }

    // Check if email already exists in user db. Returns false if duplicate, otherwise true.
    private Boolean checkNotDuplicateEmail(String email) throws ExecutionException, InterruptedException {
        // Create a reference to the cities collection
        CollectionReference users = db.collection("users");
        // Create a query against the collection.
        Query query = users.whereEqualTo("email", email.trim()).limit(1);
        // retrieve  query results asynchronously using query.get()
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        if (querySnapshot.get().getDocuments().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

}
