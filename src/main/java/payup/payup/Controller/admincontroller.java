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
import payup.payup.dto.*;
import payup.payup.exception.UserNotFoundException;
import payup.payup.mapper.*;
import payup.payup.model.*;
import payup.payup.service.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for administrative operations in the PayUp system.
 * Provides endpoints for managing users, properties, tenants, rents, and notifications.
 * All endpoints require ADMIN role authorization and use DTOs for data transfer.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    // Services
    @Autowired private UserService userService;
    @Autowired private PropertyService propertyService;
    @Autowired private TenantService tenantService;
    @Autowired private RentService rentService;
    @Autowired private NotificationService notificationService;

    // Mappers
    @Autowired private UserMapper userMapper;
    @Autowired private PropertyMapper propertyMapper;
    @Autowired private TenantMapper tenantMapper;
    @Autowired private RentMapper rentMapper;
    @Autowired private NotificationMapper notificationMapper;

    /**
     * Retrieves a paginated list of all users in the system.
     *
     * @param pageable Pagination and sorting parameters
     * @return ResponseEntity containing a Page of UserDto objects with HTTP 200 (OK) status
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        logger.info("Fetching all users with pagination: {}", pageable);
        Page<User> users = userService.findAll(pageable);
        Page<UserDto> userDtos = users.map(userMapper::toDto);
        return ResponseEntity.ok(userDtos);
    }

    /**
     * Retrieves a paginated list of users filtered by role.
     *
     * @param role The role to filter users by (case-insensitive)
     * @param pageable Pagination and sorting parameters
     * @return ResponseEntity containing a Page of UserDto objects or error message if role is invalid
     */
    @GetMapping("/users/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role, Pageable pageable) {
        logger.info("Fetching users by role: {}", role);
        try {
            User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
            Page<User> users = userService.findByRole(userRole, pageable);
            Page<UserDto> userDtos = users.map(userMapper::toDto);
            return ResponseEntity.ok(userDtos);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid role provided: {}", role);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role: " + role));
        }
    }

    /**
     * Searches for users based on a query string with pagination.
     *
     * @param query Search term (name, email, or phone)
     * @param pageable Pagination and sorting parameters
     * @return ResponseEntity containing a Page of UserDto objects or error if query is empty
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
        Page<UserDto> userDtos = users.map(userMapper::toDto);
        return ResponseEntity.ok(userDtos);
    }

    /**
     * Creates a new user in the system.
     *
     * @param userDto User details to create
     * @return ResponseEntity containing the created UserDto with HTTP 201 (Created) status
     */
    @PostMapping("/user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        logger.info("Creating user: email={}", userDto.getEmail());
        User user = userMapper.toEntity(userDto);
        User createdUser = userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDto(createdUser));
    }

    /**
     * Updates an existing user's information.
     *
     * @param userDto Updated user details
     * @return ResponseEntity containing the updated UserDto or 404 if not found
     */
    @PutMapping("/user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserDto userDto) {
        logger.info("Updating user: id={}", userDto.getId());
        try {
            User user = userMapper.toEntity(userDto);
            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(userMapper.toDto(updatedUser));
        } catch (UserNotFoundException e) {
            logger.warn("User not found for update: id={}", userDto.getId());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes a user from the system.
     *
     * @param userId ID of the user to delete
     * @return ResponseEntity with success message or 404 if user not found
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
     * @param ownerId ID of the landlord
     * @return ResponseEntity containing List of PropertyDto objects or error message
     */
    @GetMapping("/properties/owner/{ownerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPropertiesByOwner(@PathVariable Long ownerId) {
        logger.info("Fetching properties for ownerId={}", ownerId);
        try {
            List<Property> properties = propertyService.getPropertiesByLandlord(ownerId);
            List<PropertyDto> propertyDtos = properties.stream()
                    .map(propertyMapper::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(propertyDtos);
        } catch (Exception e) {
            logger.error("Error fetching properties for ownerId={}: {}", ownerId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Error fetching properties: " + e.getMessage()));
        }
    }

    /**
     * Retrieves all tenants in the system.
     *
     * @return ResponseEntity containing List of TenantDto objects
     */
    @GetMapping("/tenants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TenantDto>> getAllTenants() {
        logger.info("Fetching all tenants");
        List<Tenant> tenants = tenantService.findAll();
        List<TenantDto> tenantDtos = tenants.stream()
                .map(tenantMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tenantDtos);
    }

    /**
     * Retrieves rent details for a specific tenant.
     *
     * @param tenantId ID of the tenant
     * @return ResponseEntity containing List of RentDto objects or 404 if not found
     */
    @GetMapping("/tenants/{tenantId}/rents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRentsByTenant(@PathVariable Long tenantId) {
        logger.info("Fetching rents for tenantId={}", tenantId);
        try {
            List<Rent> rents = rentService.getRentsByTenant(tenantId);
            List<RentDto> rentDtos = rents.stream()
                    .map(rentMapper::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(rentDtos);
        } catch (Exception e) {
            logger.warn("Error fetching rents for tenantId={}: {}", tenantId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Sends a notification to a specific tenant.
     *
     * @param tenantId ID of the tenant to notify
     * @param message Notification message content
     * @return ResponseEntity with success message and notification ID or 404 if tenant not found
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
        return ResponseEntity.ok(Map.of(
                "message", "Notification sent to tenant: " + tenant.getEmail(),
                "notificationId", notification.getId().toString()
        ));
    }

    /**
     * Sends a notification to all tenants.
     *
     * @param message Notification message content
     * @return ResponseEntity with success message
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