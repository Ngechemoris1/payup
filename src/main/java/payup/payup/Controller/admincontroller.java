package payup.payup.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import payup.payup.exception.UserNotFoundException;
import payup.payup.model.*;
import payup.payup.service.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for administrative operations. Provides endpoints for managing users,
 * properties, tenants, rents, and notifications. All endpoints are restricted to users
 * with the 'ADMIN' role.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired private UserService userService;
    @Autowired private PropertyService propertyService;
    @Autowired private TenantService tenantService;
    @Autowired private RentService rentService;
    @Autowired private NotificationService notificationService;

    /**
     * Retrieves a paginated list of all users in the system.
     *
     * @param pageable Pagination and sorting parameters.
     * @return ResponseEntity containing a Page of User objects with HTTP 200 (OK) status.
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {
        logger.info("Fetching all users with pagination: {}", pageable);
        Page<User> users = userService.findAll(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Retrieves a paginated list of users filtered by role.
     *
     * @param role     The role to filter users by (e.g., "ADMIN", "LANDLORD", "TENANT").
     * @param pageable Pagination and sorting parameters.
     * @return ResponseEntity containing a Page of User objects or an error message if the role is invalid.
     */
    @GetMapping("/users/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role, Pageable pageable) {
        logger.info("Fetching users by role: {}", role);
        try {
            User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
            Page<User> users = userService.findByRole(userRole, pageable);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid role provided: {}", role);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role: " + role));
        }
    }

    /**
     * Searches for users based on a query string with pagination.
     *
     * @param query    The search term (e.g., name, email).
     * @param pageable Pagination and sorting parameters.
     * @return ResponseEntity containing a Page of User objects or an error if the query is invalid.
     */
    @GetMapping("/users/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> searchUsers(@RequestParam String query, Pageable pageable) {
        logger.info("Searching users with query: {}", query);
        if (query == null || query.trim().isEmpty()) {
            logger.warn("Empty search query received");
            return ResponseEntity.badRequest().body(Map.of("error", "Search query cannot be empty"));
        }
        Page<User> users = userService.searchUsers(query, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Creates a new user in the system.
     *
     * @param user The user details to create.
     * @return ResponseEntity containing the created User object with HTTP 201 (Created) status.
     */
    @PostMapping("/user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        logger.info("Creating user: email={}", user.getEmail());
        User createdUser = userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Updates an existing user's information.
     *
     * @param user The updated user details.
     * @return ResponseEntity containing the updated User object or 404 if not found.
     */
    @PutMapping("/user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@Valid @RequestBody User user) {
        logger.info("Updating user: id={}", user.getId());
        try {
            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(updatedUser);
        } catch (UserNotFoundException e) {
            logger.warn("User not found for update: id={}", user.getId());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes a user from the system.
     *
     * @param userId The ID of the user to delete.
     * @return ResponseEntity with a success message or 404 if the user is not found.
     */
    @DeleteMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        logger.info("Deleting user: id={}", userId);
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (UserNotFoundException e) {
            logger.warn("User not found for deletion: id={}", userId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retrieves all properties owned by a specific landlord.
     *
     * @param ownerId The ID of the landlord.
     * @return ResponseEntity containing a List of Property objects or an error message.
     */
    @GetMapping("/properties/owner/{ownerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPropertiesByOwner(@PathVariable Long ownerId) {
        logger.info("Fetching properties for ownerId={}", ownerId);
        try {
            List<Property> properties = propertyService.getPropertiesByLandlord(ownerId);
            return ResponseEntity.ok(properties);
        } catch (Exception e) {
            logger.error("Error fetching properties for ownerId={}: {}", ownerId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Error fetching properties: " + e.getMessage()));
        }
    }

    /**
     * Retrieves all tenants in the system.
     *
     * @return ResponseEntity containing a List of Tenant objects.
     */
    @GetMapping("/tenants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Tenant>> getAllTenants() {
        logger.info("Fetching all tenants");
        List<Tenant> tenants = tenantService.findAll();
        return ResponseEntity.ok(tenants);
    }

    /**
     * Retrieves rent details for a specific tenant.
     *
     * @param tenantId The ID of the tenant.
     * @return ResponseEntity containing a List of Rent objects or 404 if not found.
     */
    @GetMapping("/tenants/{tenantId}/rents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRentsByTenant(@PathVariable Long tenantId) {
        logger.info("Fetching rents for tenantId={}", tenantId);
        try {
            List<Rent> rents = rentService.getRentsByTenant(tenantId);
            return ResponseEntity.ok(rents);
        } catch (Exception e) {
            logger.warn("Error fetching rents for tenantId={}: {}", tenantId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Sends a notification to a specific tenant.
     *
     * @param tenantId The ID of the tenant to notify.
     * @param message  The notification message.
     * @return ResponseEntity with a success message or 404 if the tenant is not found.
     */
    @PostMapping("/notify/tenant/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> notifyTenant(@PathVariable Long tenantId, @RequestBody String message) {
        logger.info("Sending notification to tenantId={}", tenantId);
        Tenant tenant = tenantService.getTenantById(tenantId);
        if (tenant == null) {
            logger.warn("Tenant not found: id={}", tenantId);
            return ResponseEntity.notFound().build();
        }
        User adminUser = userService.getCurrentAdminUser();
        Notification notification = notificationService.sendNotificationToTenant(tenant, message, adminUser);
        return ResponseEntity.ok(Map.of("message", "Notification sent to tenant: " + tenant.getEmail(), "notificationId", notification.getId().toString()));
    }

    /**
     * Sends a notification to all tenants.
     *
     * @param message The notification message.
     * @return ResponseEntity with a success message.
     */
    @PostMapping("/notify/all-tenants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> notifyAllTenants(@RequestBody String message) {
        logger.info("Sending notification to all tenants");
        User adminUser = userService.getCurrentAdminUser();
        notificationService.sendNotificationToAllTenants(message, adminUser);
        return ResponseEntity.ok(Map.of("message", "Notification sent to all tenants"));
    }
}