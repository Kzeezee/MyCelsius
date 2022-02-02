package firebase;

import java.util.concurrent.ExecutionException;

public interface IFirebaseDB {
    public Long registerUser(String email, String password) throws ExecutionException, InterruptedException;
    public Long loginUser(String email, String password) throws ExecutionException, InterruptedException;
}
