package payup.payup.exception;

/**
 * Custom exception for scenarios where a property is not found in the system.
 * This can be thrown when trying to manage or retrieve information for a non-existent property.
 */
public class PropertyNotFoundException extends RuntimeException {

    public PropertyNotFoundException(String message) {
        super(message);
    }

    public PropertyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}