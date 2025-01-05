package payup.payup.exception;

/**
 * Custom exception for scenarios where a tenant is not found in the system.
 * This can be thrown when attempting operations on a tenant that does not exist.
 */
public class TenantNotFoundException extends RuntimeException {

    public TenantNotFoundException(String message) {
        super(message);
    }

    public TenantNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}