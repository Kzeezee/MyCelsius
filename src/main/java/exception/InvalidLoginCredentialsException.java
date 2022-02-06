package exception;

public class InvalidLoginCredentialsException extends RuntimeException {
    public InvalidLoginCredentialsException(String errorMessage) {
        super(errorMessage);
    }
}
