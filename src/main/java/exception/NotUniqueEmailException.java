package exception;

public class NotUniqueEmailException extends RuntimeException {
    public NotUniqueEmailException(String errorMessage) {
        super(errorMessage);
    }
}
