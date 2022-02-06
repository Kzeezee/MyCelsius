package firebase;

import com.google.cloud.Timestamp;
import model.TemperatureRecord;
import model.UserRecord;
import util.Callback;
import util.Verification;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public interface IFirebaseDB {
    public Timestamp registerUser(String email, String password) throws ExecutionException, InterruptedException;
    public UserRecord loginUser(String email, String password) throws ExecutionException, InterruptedException;
    public String createOrganisation(String orgName) throws ExecutionException, InterruptedException;
    public Timestamp submitTemperature(TemperatureRecord temperatureRecord) throws ExecutionException, InterruptedException;
    public Boolean checkUserHasOrganisation(String userId) throws ExecutionException, InterruptedException;
    public Verification verifyValidUserAndOrganisation(Long telegramId, String organisationCode); // Long telegramId, String organisationCode
}
