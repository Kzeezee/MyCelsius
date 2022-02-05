package firebase;

import com.google.cloud.Timestamp;

import java.util.concurrent.ExecutionException;

public interface IFirebaseDB {
    public Timestamp registerUser(String email, String password) throws ExecutionException, InterruptedException;
    public String loginUser(String email, String password) throws ExecutionException, InterruptedException;
}
