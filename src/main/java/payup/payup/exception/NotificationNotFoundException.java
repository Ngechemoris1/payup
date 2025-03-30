package payup.payup.exception;

/**
 * Custom exception for scenarios where a notification is not found in the system.
 * This can be thrown when trying to access or manage notifications that do not exist.
 */
public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(String message) {
        super(message);
    }

    public NotificationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}