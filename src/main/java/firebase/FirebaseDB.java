package firebase;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import exception.InvalidLoginCredentialsException;
import exception.NotUniqueEmailException;
import exception.UserDoesNotExistException;
import model.MemberRecord;
import model.TemperatureRecord;
import model.UserRecord;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import util.StateManager;
import util.Verification;

import java.io.IOException;
import java.util.*;
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
            return writeResult(docRef, data);
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
            String organisationCode = checkUserHasOrganisation(docId);
            Boolean hasOrg = false;
            if (organisationCode != null) {
                hasOrg = true;
            }
            if (docEmail.equalsIgnoreCase(email) && BCrypt.verifyer().verify(password.getBytes(), docPassword.getBytes()).verified) { // Validate email and password
                // Valid match
                UserRecord userRecord = new UserRecord(docId, docEmail, organisationCode, hasOrg);
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
        if (temperatureRecord.getIsMember()) {
            // Submit as member
            // Querying for member information first
            CollectionReference orgMembers = db.collection(ORGANISATION_COLLECTION).document(temperatureRecord.getOrganisationCode()).collection(MEMBERS_COLLECTION);
            Query getMemberQuery = orgMembers.whereEqualTo("telegramId", temperatureRecord.getTelegramId()).limit(1);
            List<QueryDocumentSnapshot> documentSnapshots = getMemberQuery.get().get().getDocuments();

            // Double check to verify legitimate member of organisation
            if (!documentSnapshots.isEmpty()) {
                // Save the temperature submission
                QueryDocumentSnapshot memberDocRef = documentSnapshots.get(0);
                String name = memberDocRef.getString("name");
                DocumentReference memberTemperatureDocRef = db.collection(TEMPERATURE_COLLECTION).document();

                Map<String, Object> data = new HashMap<>();
                data.put("organisationCode", temperatureRecord.getOrganisationCode());
                data.put("name", name);
                data.put("telegramId", temperatureRecord.getTelegramId());
                data.put("temperature", temperatureRecord.getTemperature());
                data.put("submissionDate", temperatureRecord.getSubmissionDate());
                data.put("isMember", temperatureRecord.getIsMember());

                return writeResult(memberTemperatureDocRef, data);
            } else {
                throw new RuntimeException("No matching member from organisation yet received temperature submission request for members");
            }
        } else {
            // Submit as guest
            DocumentReference guestTemperatureDocRef = db.collection(TEMPERATURE_COLLECTION).document();

            Map<String, Object> data = new HashMap<>();
            data.put("organisationCode", temperatureRecord.getOrganisationCode());
            data.put("name", temperatureRecord.getName()); // In the case for guest, we will use their username
            data.put("telegramId", temperatureRecord.getTelegramId());
            data.put("temperature", temperatureRecord.getTemperature());
            data.put("submissionDate", temperatureRecord.getSubmissionDate());
            data.put("isMember", temperatureRecord.getIsMember());

            return writeResult(guestTemperatureDocRef, data);
        }
    }

    @Override
    public List<MemberRecord> getMemberRecords(String organisationCode) throws ExecutionException, InterruptedException {
        CollectionReference members = db.collection(ORGANISATION_COLLECTION).document(organisationCode).collection(MEMBERS_COLLECTION);
        List<QueryDocumentSnapshot> documentSnapshots = members.get().get().getDocuments();

        return mapMemberRecordsList(documentSnapshots);
    }

    @Override
    public List<TemperatureRecord> getMemberTemperatureRecords(String organisationCode, Timestamp currentDate, Integer historyInDays) throws ExecutionException, InterruptedException {
        CollectionReference temperatures = db.collection(TEMPERATURE_COLLECTION);
        Query query = temperatures
                .whereEqualTo("isMember", true)
                .whereGreaterThanOrEqualTo("submissionDate", Timestamp.of(DateUtils.addDays(currentDate.toDate(), -historyInDays)))
                .whereLessThan("submissionDate", currentDate)
                .orderBy("submissionDate", Query.Direction.DESCENDING);
        List<QueryDocumentSnapshot> documentSnapshots = query.get().get().getDocuments();

        return mapTemperatureRecordsList(documentSnapshots);
    }

    @Override
    public List<TemperatureRecord> getGuestTemperatureRecords(String organisationCode, Timestamp date) throws ExecutionException, InterruptedException {
        CollectionReference temperatures = db.collection(TEMPERATURE_COLLECTION);
        Query query = temperatures
                .whereEqualTo("isMember", false)
                .whereGreaterThanOrEqualTo("submissionDate", date)
                .whereLessThan("submissionDate", DateUtils.addDays(date.toDate(), 1))
                .orderBy("submissionDate");
        List<QueryDocumentSnapshot> documentSnapshots = query.get().get().getDocuments();

        return mapTemperatureRecordsList(documentSnapshots);
    }

    @Override
    public Timestamp addMember(String organisationCode, MemberRecord memberRecord) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection(ORGANISATION_COLLECTION).document(organisationCode).collection(MEMBERS_COLLECTION).document();
        Map<String, Object> data = new HashMap<>();
        data.put("name", memberRecord.getName());
        data.put("identifier", memberRecord.getIdentifier());
        data.put("telegramId", memberRecord.getTelegramId());

        return writeResult(docRef, data);
    }

    @Override
    public Timestamp deleteMember(String organisationCode, MemberRecord memberRecord) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection(ORGANISATION_COLLECTION).document(organisationCode).collection(MEMBERS_COLLECTION).document(memberRecord.getId());
        ApiFuture<WriteResult> result = docRef.delete();
        if (result != null) {
            return result.get().getUpdateTime();
        } else {
            throw new RuntimeException("Unexpected error occurred when retrieving write result from database.");
        }
    }

    @Override
    public String checkUserHasOrganisation(String userId) throws InterruptedException, ExecutionException {
        CollectionReference organisations = db.collection(ORGANISATION_COLLECTION);
        Query query = organisations.whereArrayContains("admins", userId).limit(1);
        List<QueryDocumentSnapshot> documentSnapshots = query.get().get().getDocuments();
        if (!documentSnapshots.isEmpty()) {
            QueryDocumentSnapshot organisation = documentSnapshots.get(0);
            return organisation.getId();
        } else {
            return null;
        }
    }

    @Override
    public Verification verifyValidOrganisation(String organisationCode) {
        DocumentReference organisationRef = db.collection(ORGANISATION_COLLECTION).document(organisationCode);
        if (organisationRef != null) {
            return new Verification(true, null); // Found a match of the organisation
        } else {
            return new Verification(false, "No such organisation exists.");
        }
    }

    @Override
    public Verification verifyValidUserAndOrganisation(Long telegramId, String organisationCode) {
        DocumentReference organisationRef = db.collection(ORGANISATION_COLLECTION).document(organisationCode);
        if (organisationRef != null) {
            CollectionReference members = organisationRef.collection(MEMBERS_COLLECTION);
            Query query = members.whereEqualTo("telegramId", telegramId.toString()).limit(1);
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
            return new Verification(false, "No such organisation exists."); // No such organisation exists
        }
    }


    /*
     * Private utility methods
    */
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
        DocumentReference orgDocumentReference = orgs.document(orgCode);
        DocumentSnapshot orgDocument = orgDocumentReference.get().get();
        if (!orgDocument.exists()) {
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

    private List<MemberRecord> mapMemberRecordsList(List<QueryDocumentSnapshot> documentSnapshots) {
        List<MemberRecord> records = new ArrayList<>();
        for (QueryDocumentSnapshot documentSnapshot : documentSnapshots) {
            MemberRecord memberRecord = new MemberRecord();
            memberRecord.setId(documentSnapshot.getId());
            memberRecord.setName(documentSnapshot.getString("name"));
            memberRecord.setIdentifier(documentSnapshot.getString("identifier"));
            memberRecord.setTelegramId(documentSnapshot.getString("telegramId"));
            records.add(memberRecord);
        }
        return records;
    }

    private List<TemperatureRecord> mapTemperatureRecordsList(List<QueryDocumentSnapshot> documentSnapshots) {
        List<TemperatureRecord> temperatureRecords = new ArrayList<>();
        for (QueryDocumentSnapshot documentSnapshot : documentSnapshots) {
            TemperatureRecord temperatureRecord = new TemperatureRecord();
            temperatureRecord.setId(documentSnapshot.getId());
            temperatureRecord.setOrganisationCode(documentSnapshot.getString("organisationCode"));
            temperatureRecord.setName(documentSnapshot.getString("name"));
            temperatureRecord.setTelegramId(documentSnapshot.getString("telegramId"));
            temperatureRecord.setTemperature(documentSnapshot.getDouble("temperature"));
            temperatureRecord.setSubmissionDate(documentSnapshot.getTimestamp("submissionDate"));
            temperatureRecord.setIsMember(documentSnapshot.getBoolean("isMember"));
            temperatureRecords.add(temperatureRecord);
        }
        return temperatureRecords;
    }

    private Timestamp writeResult(DocumentReference documentReference, Map<String, Object> data) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> result = documentReference.set(data);
        if (result != null) {
            return result.get().getUpdateTime();
        } else {
            throw new RuntimeException("Unexpected error occurred when retrieving write result from database.");
        }
    }

}
