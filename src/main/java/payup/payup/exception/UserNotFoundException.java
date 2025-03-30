package payup.payup.exception;

/**
 * Custom exception to be thrown when a user is not found in the system.
 * This helps in distinguishing between general exceptions and specific user-related errors.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}