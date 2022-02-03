package exception;

public class NotUniqueEmailException extends RuntimeException {
    public NotUniqueEmailException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
