package payup.payup.exception;

/**
 * Custom exception for scenarios where a rent record is not found in the system.
 * This can be thrown when attempting to retrieve or update rent information that doesn't exist.
 */
public class RentNotFoundException extends RuntimeException {

    public RentNotFoundException(String message) {
        super(message);
    }

    public RentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}