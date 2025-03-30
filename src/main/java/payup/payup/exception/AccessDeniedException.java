package payup.payup.exception;

/**
 * Custom exception to handle scenarios where a user tries to access resources or perform actions 
 * they are not authorized for.
 */
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}