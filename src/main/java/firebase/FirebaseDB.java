package firebase;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import exception.InvalidLoginCredentialsException;
import exception.NotUniqueEmailException;
import exception.UserDoesNotExistException;
import model.TemperatureRecord;
import model.UserRecord;
import org.apache.commons.lang3.RandomStringUtils;
import util.StateManager;
import util.Verification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static util.Constants.*;

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
            DocumentReference docRef = db.collection(USER_COLLECTION).document();
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
    public UserRecord loginUser(String email, String password) throws ExecutionException, InterruptedException, InvalidLoginCredentialsException, UserDoesNotExistException {
        email = email.trim();

        QueryDocumentSnapshot userDoc = getUser(email);
        if (userDoc != null) {
            String docEmail = userDoc.getString("email");
            String docPassword = userDoc.getString("password");
            String docId = userDoc.getId();
            Boolean hasOrg = checkUserHasOrganisation(docId);
            if (docEmail.equalsIgnoreCase(email) && BCrypt.verifyer().verify(password.getBytes(), docPassword.getBytes()).verified) { // Validate email and password
                // Valid match
                UserRecord userRecord = new UserRecord(docId, docEmail, hasOrg);
                return userRecord;
            } else {
                // Invalid match and hence invalid credentials
                throw new InvalidLoginCredentialsException("Invalid credentials");
            }
        } else {
            throw new UserDoesNotExistException("User does not exist! User: " + email);
        }
    }

    @Override
    public String createOrganisation(String orgName) throws ExecutionException, InterruptedException {
        orgName = orgName.trim();

        // To ensure unique org code is generated
        String generatedOrgCode;
        do {
            generatedOrgCode = RandomStringUtils.random(6, true, true);
        } while (!checkNotDuplicateOrgCode(generatedOrgCode));

        // Input the organisation details
        DocumentReference orgDocRef = db.collection(ORGANISATION_COLLECTION).document(generatedOrgCode);
        Map<String, Object> data = new HashMap<>();
        data.put("name", orgName);
        List<String> admins = new ArrayList<>();
        admins.add(StateManager.getCurrentUserId());
        data.put("admins", admins);

        // Setting the data and checking if successful
        ApiFuture<WriteResult> result = orgDocRef.set(data);
        if (result != null) {
            return generatedOrgCode;
        } else {
            throw new RuntimeException("Unexpected error occurred when retrieving write result from database.");
        }
    }

    @Override
    public Timestamp submitTemperature(TemperatureRecord temperatureRecord) throws ExecutionException, InterruptedException {

        return null;
    }

    @Override
    public Boolean checkUserHasOrganisation(String userId) throws InterruptedException, ExecutionException {
        CollectionReference organisations = db.collection(ORGANISATION_COLLECTION);
        Query query = organisations.whereArrayContains("admins", userId);
        List<QueryDocumentSnapshot> documentSnapshots = query.get().get().getDocuments();
        if (!documentSnapshots.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Verification verifyValidUserAndOrganisation(Long telegramId, String organisationCode) {
        DocumentReference organisationRef = db.collection(ORGANISATION_COLLECTION).document(organisationCode);
        if (organisationRef != null) {
            CollectionReference members = organisationRef.collection(MEMBERS_COLLECTION);
            Query query = members.whereEqualTo("telegramId", telegramId).limit(1);
            List<QueryDocumentSnapshot> documentSnapshots;
            try {
                documentSnapshots = query.get().get().getDocuments();
                if (!documentSnapshots.isEmpty()) {
                    return new Verification(true, null); // Found the user
                } else {
                    return new Verification(false, "Not a valid member of the organisation."); // No such user exists
                }
            } catch (Exception e) {
                throw new RuntimeException("Something went wrong when retrieving data from database.");
            }
        } else {
            return new Verification(false, "Invalid organisation code."); // No such organisation exists
        }
    }


    private QueryDocumentSnapshot getUser(String email) throws ExecutionException, InterruptedException {
        email = email.trim();
        CollectionReference users = db.collection(USER_COLLECTION);
        // Create a query against the collection.
        Query query = users.whereEqualTo("email", email.trim()).limit(1);
        List<QueryDocumentSnapshot> documentSnapshots = query.get().get().getDocuments();
        if (!documentSnapshots.isEmpty()) {
            return documentSnapshots.get(0);
        } else {
            return null;
        }
    }

    private Boolean checkNotDuplicateOrgCode(String orgCode) throws ExecutionException, InterruptedException {
        CollectionReference orgs = db.collection(ORGANISATION_COLLECTION);
        Query query = orgs.whereEqualTo("code", orgCode.trim()).limit(1);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        if (querySnapshot.get().getDocuments().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    // Check if email already exists in user db. Returns false if duplicate, otherwise true.
    private Boolean checkNotDuplicateEmail(String email) throws ExecutionException, InterruptedException {
        // Create a reference to the cities collection
        CollectionReference users = db.collection(USER_COLLECTION);
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
