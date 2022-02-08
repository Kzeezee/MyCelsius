package firebase;

import com.google.cloud.Timestamp;
import model.MemberRecord;
import model.TemperatureRecord;
import model.UserRecord;
import util.Verification;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface IFirebaseDB {
    // Auth
    public Timestamp registerUser(String email, String password) throws ExecutionException, InterruptedException;
    public UserRecord loginUser(String email, String password) throws ExecutionException, InterruptedException;
    // First-time setup
    public String createOrganisation(String orgName) throws ExecutionException, InterruptedException;

    // Common executions
    public Timestamp submitTemperature(TemperatureRecord temperatureRecord) throws ExecutionException, InterruptedException;
    public List<MemberRecord> getMemberRecords(String organisationCode) throws ExecutionException, InterruptedException;
    public Timestamp addMember(String organisationCode, MemberRecord memberRecord) throws ExecutionException, InterruptedException;
    public Timestamp deleteMember(String organisationCode, MemberRecord memberRecord) throws ExecutionException, InterruptedException;

    // Validation and utility
    public String checkUserHasOrganisation(String userId) throws ExecutionException, InterruptedException;
    public Verification verifyValidUserAndOrganisation(Long telegramId, String organisationCode); // Long telegramId, String organisationCode
}
