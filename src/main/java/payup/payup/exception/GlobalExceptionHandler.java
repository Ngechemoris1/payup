package payup.payup.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler to manage all exceptions in a centralized manner, 
 * returning appropriate HTTP responses with error details.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles UserNotFoundException, providing a specific error message for when a user is not found.
     *
     * @param ex      The UserNotFoundException being handled.
     * @param request The current web request.
     * @return A ResponseEntity with HTTP status 404 and details about the error.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "User not found");
        body.put("details", request.getDescription(false));

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles AccessDeniedException, returning a 403 Forbidden status when access is denied.
     *
     * @param ex      The AccessDeniedException being handled.
     * @param request The current web request.
     * @return A ResponseEntity with HTTP status 403 and details about the access denial.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "Access Denied");
        body.put("details", request.getDescription(false));

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    /**
     * Catches all other exceptions not specifically handled above, providing a generic error response.
     *
     * @param ex      The general Exception being handled.
     * @param request The current web request.
     * @return A ResponseEntity with HTTP status 500 and details about the error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "An unexpected error occurred");
        body.put("details", request.getDescription(false));

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

    /**
     * Overrides the method for handling validation errors, useful when using @Valid annotations.
     * @param ex The MethodArgumentNotValidException being handled.
    **/