package payup.payup.controller;

import payup.payup.exception.UserNotFoundException;
import payup.payup.model.*;
import payup.payup.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private RentService rentService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Retrieves all users in the system.
     * @return ResponseEntity with a list of all users.
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    /**
     * Creates a new user in the system.
     * @param user User object to create.
     * @return ResponseEntity with the created user or an error message.
     */
    @PostMapping("/user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.registerUser(user));
    }

    /**
     * Updates an existing user's information.
     * @param user User object with updated information.
     * @return ResponseEntity indicating success or failure of the update.
     */
    @PutMapping("/user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        try {
            return ResponseEntity.ok(userService.updateUser(user));
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes a user from the system.
     * @param userId ID of the user to delete.
     * @return ResponseEntity indicating if the deletion was successful.
     */
    @DeleteMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok("User deleted successfully");
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retrieves all properties in the system.
     * @return ResponseEntity with a list of all properties.
     */
    @GetMapping("/properties")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllProperties() {
        return ResponseEntity.ok(propertyService.findAll());
    }

    /**
     * Retrieves all tenants in the system.
     * @return ResponseEntity with a list of all tenants.
     */
    @GetMapping("/tenants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllTenants() {
        return ResponseEntity.ok(tenantService.findAll());
    }

    /**
     * Gets rent information for a specific tenant.
     * @param tenantId ID of the tenant whose rents are to be fetched.
     * @return ResponseEntity with the tenant's rent history or not found if the tenant doesn't exist.
     */
    @GetMapping("/tenants/{tenantId}/rents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRentsByTenant(@PathVariable Long tenantId) {
        try {
            return ResponseEntity.ok(rentService.getRentsByTenant(tenantId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Sends a notification to a specific tenant from the admin.
     * @param tenantId ID of the tenant to notify.
     * @param message The message to send as a notification.
     * @return ResponseEntity indicating if the notification was sent successfully.
     */
    @PostMapping("/notify/tenant/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> notifyTenant(@PathVariable Long tenantId, @RequestBody String message) {
        Tenant tenant = tenantService.getTenantById(tenantId);
        if (tenant == null) {
            return ResponseEntity.notFound().build();
        }
        
        Notification notification = notificationService.sendNotificationToTenant(tenant, message);
        return ResponseEntity.ok("Notification sent to tenant: " + tenant.getEmail());
    }

    /**
     * Sends a notification to all tenants from the admin.
     * @param message The message to be sent to all tenants.
     * @return ResponseEntity indicating if the notifications were sent successfully.
     */
    @PostMapping("/notify/all-tenants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> notifyAllTenants(@RequestBody String message) {
        notificationService.sendNotificationToAllTenants(message);
        return ResponseEntity.ok("Notification sent to all tenants");
    }
}