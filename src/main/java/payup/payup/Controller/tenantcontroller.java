package payup.payup.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import payup.payup.dto.*;
import payup.payup.exception.ResourceNotFoundException;
import payup.payup.mapper.RentMapper;
import payup.payup.mapper.TenantMapper;
import payup.payup.model.*;
import payup.payup.service.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for managing tenant-related operations in the PayUp system.
 * Provides endpoints for creating, retrieving, updating, and deleting tenants,
 * as well as tenant-specific actions like viewing rent dues and notifying admins.
 * Secured with role-based access control using Spring Security.
 */
@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private static final Logger logger = LoggerFactory.getLogger(TenantController.class);

    private final TenantService tenantService;
    private final UserService userService;
    private final PropertyService propertyService;
    private final RoomService roomService;
    private final TenantMapper tenantMapper;
    private final RentService rentService;
    private final NotificationService notificationService;
    private final RentMapper rentMapper;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Constructs a TenantController with required service and mapper dependencies.
     *
     * @param tenantService The service for tenant operations.
     * @param userService The service for user operations.
     * @param propertyService The service for property operations.
     * @param roomService The service for room operations.
     * @param tenantMapper The mapper for converting between Tenant entities and DTOs.
     * @param rentService The service for rent operations.
     * @param notificationService The service for notification operations.
     * @param rentMapper The mapper for converting between Rent entities and DTOs.
     */
    @Autowired
    public TenantController(TenantService tenantService,
                            UserService userService,
                            PropertyService propertyService,
                            RoomService roomService,
                            TenantMapper tenantMapper,
                            RentService rentService,
                            NotificationService notificationService,
                            PasswordEncoder passwordEncoder,
                            RentMapper rentMapper) {
        this.tenantService = tenantService;
        this.userService = userService;
        this.propertyService = propertyService;
        this.roomService = roomService;
        this.tenantMapper = tenantMapper;
        this.rentService = rentService;
        this.notificationService = notificationService;
        this.rentMapper = rentMapper;
        this.passwordEncoder = (BCryptPasswordEncoder) passwordEncoder;
    }

    /**
     * Creates a new tenant in the system. Requires ADMIN role.
     *
     * @param tenantCreateDto The DTO containing tenant creation data.
     * @return ResponseEntity with the created TenantDto or an error response.
     */


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTenant(@Valid @RequestBody TenantCreateDto tenantCreateDto) {
        logger.info("Creating new tenant: {}", tenantCreateDto.getEmail());
        logger.debug("Request DTO: {}", tenantCreateDto);
        try {
            User user;
            if (tenantCreateDto.getUserId() != null) {
                user = userService.findById(tenantCreateDto.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + tenantCreateDto.getUserId()));
            } else {
                if (tenantCreateDto.getPassword() == null || tenantCreateDto.getPassword().trim().isEmpty()) {
                    throw new IllegalArgumentException("Password is required when creating a new user for the tenant");
                }
                User newUser = new User();
                newUser.setEmail(tenantCreateDto.getEmail());
                newUser.setName(tenantCreateDto.getName());
                newUser.setPhone(tenantCreateDto.getPhone());
                newUser.setPassword(tenantCreateDto.getPassword());
                newUser.setRole(User.UserRole.TENANT);
                user = userService.registerUser(newUser);
            }

            Tenant tenant = tenantMapper.toEntity(tenantCreateDto);
            logger.debug("Tenant after mapping: {}", tenant);

            Property property = propertyService.getPropertyById(tenantCreateDto.getPropertyId());
            if (property == null) {
                throw new ResourceNotFoundException("Property not found with ID: " + tenantCreateDto.getPropertyId());
            }
            tenant.setProperty(property);
            logger.debug("Tenant after setting property: {}", tenant);

            Room room = roomService.getRoomById(tenantCreateDto.getRoomId());
            if (room == null) {
                throw new ResourceNotFoundException("Room not found with ID: " + tenantCreateDto.getRoomId());
            }
            room.setOccupied(true);  // Set to true
            roomService.updateRoom(room);  // Update existing room instead of adding
            tenant.setRoom(room);
            logger.debug("Tenant after setting room: {}", tenant);

            tenant.setUser(user);
            logger.debug("Tenant before save: {}", tenant);

            if (tenant.getBalance() == null) {
                tenant.setBalance(0.0);
                logger.warn("Balance was null, set to 0.0");
            }
            if (tenant.getProperty() == null || tenant.getRoom() == null || tenant.getUser() == null) {
                throw new IllegalStateException("Tenant has null required fields: property=" + tenant.getProperty() + ", room=" + tenant.getRoom() + ", user=" + tenant.getUser());
            }

            Tenant createdTenant = tenantService.save(tenant);
            TenantDto responseDto = tenantMapper.toDto(createdTenant);

            logger.info("Tenant created successfully: id={}", createdTenant.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (Exception e) {
            logger.error("Unexpected error creating tenant: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Tenant creation failed", "An unexpected error occurred"));
        }
    }

    /**
     * Retrieves a tenant by their ID. Open to authenticated users with appropriate permissions.
     *
     * @param id The ID of the tenant to retrieve.
     * @return ResponseEntity with the TenantDto or an error response.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTenant(@PathVariable Long id) {
        logger.info("Retrieving tenant with ID: {}", id);
        try {
            Tenant tenant = tenantService.getTenantById(id);
            if (tenant == null) {
                throw new ResourceNotFoundException("Tenant not found with ID: " + id);
            }
            return ResponseEntity.ok(tenantMapper.toDto(tenant));
        } catch (ResourceNotFoundException e) {
            logger.warn("Tenant not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto("Tenant not found", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error retrieving tenant: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Tenant retrieval failed", "An unexpected error occurred"));
        }
    }

    /**
     * Retrieves the profile of the currently authenticated tenant. Requires TENANT role.
     *
     * @return ResponseEntity with the TenantDto or an error response.
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<?> getProfile() {
        logger.info("Retrieving profile for current tenant");
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Tenant tenant = tenantService.findByEmail(email);
            if (tenant == null) {
                throw new ResourceNotFoundException("Tenant not found with email: " + email);
            }
            return ResponseEntity.ok(tenantMapper.toDto(tenant));
        } catch (ResourceNotFoundException e) {
            logger.warn("Tenant profile not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto("Tenant profile not found", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error retrieving tenant profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Profile retrieval failed", "An unexpected error occurred"));
        }
    }

    /**
     * Updates the profile of the currently authenticated tenant. Requires TENANT role.
     *
     * @param tenantDto The DTO containing updated tenant data.
     * @return ResponseEntity with the updated TenantDto or an error response.
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody TenantDto tenantDto) {
        logger.info("Updating profile for current tenant");
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Tenant existingTenant = tenantService.findByEmail(email);
            if (existingTenant == null) {
                throw new ResourceNotFoundException("Tenant not found with email: " + email);
            }

            if (!existingTenant.getId().equals(tenantDto.getId())) {
                logger.warn("Unauthorized attempt to update tenant ID: {} by email: {}", tenantDto.getId(), email);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Unauthorized access"));
            }

            Tenant updatedTenant = tenantService.save(existingTenant);
            return ResponseEntity.ok(tenantMapper.toDto(updatedTenant));

        } catch (ResourceNotFoundException e) {
            logger.warn("Tenant not found for update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto("Tenant not found", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating tenant profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Profile update failed", "An unexpected error occurred"));
        }
    }

    /**
     * Retrieves rent dues for the currently authenticated tenant. Requires TENANT role.
     *
     * @return ResponseEntity with a list of RentDtos or an error response.
     */
    @GetMapping("/rent-dues")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<?> getRentDues() {
        logger.info("Retrieving rent dues for current tenant");
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Tenant tenant = tenantService.findByEmail(email);
            if (tenant == null) {
                throw new ResourceNotFoundException("Tenant not found with email: " + email);
            }

            List<RentDto> rentDtos = rentService.getRentsByTenant(tenant.getId()).stream()
                    .map(rentMapper::toDto)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(rentDtos);

        } catch (ResourceNotFoundException e) {
            logger.warn("Tenant not found for rent dues: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto("Tenant not found", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error retrieving rent dues: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Rent dues retrieval failed", "An unexpected error occurred"));
        }
    }

    /**
     * Sends a notification from the current tenant to the admin. Requires TENANT role.
     *
     * @param message The message content to send.
     * @return ResponseEntity with a success message or an error response.
     */
    @PostMapping("/notify-admin")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<?> notifyAdmin(@RequestBody String message) {
        logger.info("Tenant sending notification to admin");
        try {
            if (message == null || message.trim().isEmpty()) {
                logger.warn("Empty notification message received");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Message cannot be empty"));
            }

            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Tenant tenant = tenantService.findByEmail(email);
            if (tenant == null) {
                throw new ResourceNotFoundException("Tenant not found with email: " + email);
            }

            notificationService.sendNotificationToAdmin(new Notification(message, tenant));
            return ResponseEntity.ok(Map.of("message", "Notification sent successfully"));

        } catch (ResourceNotFoundException e) {
            logger.warn("Tenant not found for notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto("Tenant not found", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error sending notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Notification failed", "An unexpected error occurred"));
        }
    }

    /**
     * Updates a tenant by ID. Requires ADMIN role.
     *
     * @param id The ID of the tenant to update.
     * @param tenantDto The DTO containing updated tenant data.
     * @return ResponseEntity with the updated TenantDto or an error response.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateTenant(@PathVariable Long id, @Valid @RequestBody TenantDto tenantDto) {
        logger.info("Updating tenant with ID: {}", id);
        try {
            Tenant existingTenant = tenantService.getTenantById(id);
            if (existingTenant == null) {
                throw new ResourceNotFoundException("Tenant not found with ID: " + id);
            }

            existingTenant.setFloor(tenantDto.getFloor());

            Tenant updatedTenant = tenantService.save(existingTenant);
            return ResponseEntity.ok(tenantMapper.toDto(updatedTenant));

        } catch (ResourceNotFoundException e) {
            logger.warn("Tenant not found for update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto("Tenant not found", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating tenant: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Tenant update failed", "An unexpected error occurred"));
        }
    }

    /**
     * Deletes a tenant by ID. Requires ADMIN role.
     *
     * @param id The ID of the tenant to delete.
     * @return ResponseEntity with a success message or an error response.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteTenant(@PathVariable Long id) {
        logger.info("Deleting tenant with ID: {}", id);
        try {
            Tenant tenant = tenantService.getTenantById(id);
            if (tenant == null) {
                throw new ResourceNotFoundException("Tenant not found with ID: " + id);
            }

            tenantService.delete(tenant.getId());
            return ResponseEntity.ok(Map.of("message", "Tenant deleted successfully"));
        } catch (ResourceNotFoundException e) {
            logger.warn("Tenant not found for deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto("Tenant not found", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error deleting tenant: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Tenant deletion failed", "An unexpected error occurred"));
        }
    }

    /**
     * Retrieves all tenants in the system. Requires ADMIN role.
     *
     * @return ResponseEntity with a list of TenantDtos.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TenantDto>> getAllTenants() {
        logger.info("Retrieving all tenants");
        try {
            List<Tenant> tenants = tenantService.findAll();
            List<TenantDto> tenantDtos = tenants.stream()
                    .map(tenantMapper::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(tenantDtos);
        } catch (Exception e) {
            logger.error("Error retrieving all tenants: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve tenants", e); // Handled by global exception handler if present
        }
    }

    /**
     * Data transfer object for error responses.
     */
    public static class ErrorResponseDto {
        private String error;
        private String details;

        /**
         * Constructs an ErrorResponseDto with error and details.
         *
         * @param error The error message.
         * @param details Additional details about the error.
         */
        public ErrorResponseDto(String error, String details) {
            this.error = error;
            this.details = details;
        }

        public String getError() { return error; }
        public String getDetails() { return details; }
    }
}