package exception;

public class UserDoesNotExist extends RuntimeException {
    public UserDoesNotExist(String errorMessage) {
        super(errorMessage);
    }
}
