package payup.payup.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import payup.payup.model.Notification;
import payup.payup.model.Rent;
import payup.payup.model.Tenant;
import payup.payup.service.NotificationService;
import payup.payup.service.RentService;
import payup.payup.service.TenantService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing tenant-related operations in the PayUp system. Provides endpoints
 * for tenants to access and update their profiles, view rent dues, send notifications to admins,
 * and for admins to perform CRUD operations on tenant records. Access is restricted based on
 * user roles ('TENANT' or 'ADMIN').
 */
@RestController
@RequestMapping("/api/tenant")
public class TenantController {

    private static final Logger logger = LoggerFactory.getLogger(TenantController.class);

    @Autowired
    private TenantService tenantService;

    @Autowired
    private RentService rentService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Retrieves the profile of the currently authenticated tenant.
     *
     * @return ResponseEntity containing the Tenant object with HTTP 200 (OK) status, or 404 if not found.
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<?> getProfile() {
        logger.info("Fetching profile for authenticated tenant");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Tenant tenant = tenantService.findByEmail(email);
        if (tenant == null) {
            logger.warn("Tenant not found for email: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Tenant not found"));
        }
        logger.debug("Profile retrieved for tenant: id={}", tenant.getId());
        return ResponseEntity.ok(tenant);
    }

    /**
     * Updates the profile of the currently authenticated tenant.
     *
     * @param updatedTenant The updated tenant details.
     * @return ResponseEntity containing the updated Tenant object with HTTP 200 (OK) status, or 404/403 if unauthorized.
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody Tenant updatedTenant) {
        logger.info("Updating profile for tenant: id={}", updatedTenant.getId());
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Tenant existingTenant = tenantService.findByEmail(email);

        if (existingTenant == null || !existingTenant.getId().equals(updatedTenant.getId())) {
            logger.warn("Unauthorized update attempt or tenant not found: id={}, email={}", updatedTenant.getId(), email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Unauthorized or tenant not found"));
        }

        existingTenant.setName(updatedTenant.getName());
        existingTenant.setEmail(updatedTenant.getEmail());
        existingTenant.setPhone(updatedTenant.getPhone());
        // Property, floor, and room changes are restricted to admins; tenants cannot modify these
        Tenant savedTenant = tenantService.save(existingTenant);
        logger.debug("Profile updated for tenant: id={}", savedTenant.getId());
        return ResponseEntity.ok(savedTenant);
    }

    /**
     * Retrieves the rent dues for the currently authenticated tenant.
     *
     * @return ResponseEntity containing a List of Rent objects with HTTP 200 (OK) status, or 404 if tenant not found.
     */
    @GetMapping("/rent-dues")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<?> getRentDues() {
        logger.info("Fetching rent dues for authenticated tenant");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Tenant tenant = tenantService.findByEmail(email);

        if (tenant == null) {
            logger.warn("Tenant not found for email: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Tenant not found"));
        }

        List<Rent> rentDues = rentService.getRentsByTenant(tenant.getId());
        logger.debug("Retrieved {} rent dues for tenant: id={}", rentDues.size(), tenant.getId());
        return ResponseEntity.ok(rentDues);
    }

    /**
     * Sends a notification from the authenticated tenant to the admin.
     *
     * @param message The message content to send.
     * @return ResponseEntity with a success message and HTTP 200 (OK) status, or 404 if tenant not found.
     */
    @PostMapping("/notify-admin")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<Map<String, String>> notifyAdmin(@RequestBody String message) {
        logger.info("Sending notification to admin from authenticated tenant");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Tenant tenant = tenantService.findByEmail(email);

        if (tenant == null) {
            logger.warn("Tenant not found for email: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Tenant not found"));
        }

        if (message == null || message.trim().isEmpty()) {
            logger.warn("Notification message is empty for tenant: id={}", tenant.getId());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Message cannot be empty"));
        }

        Notification notification = new Notification(message, tenant);
        notificationService.sendNotificationToAdmin(notification);
        logger.debug("Notification sent to admin from tenant: id={}", tenant.getId());
        return ResponseEntity.ok(Map.of("message", "Notification sent to admin"));
    }

    /**
     * Creates a new tenant in the system (admin-only).
     *
     * @param tenant The tenant details to create.
     * @return ResponseEntity containing the created Tenant object with HTTP 201 (Created) status.
     */
    @PostMapping("/tenants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Tenant> createTenant(@Valid @RequestBody Tenant tenant) {
        logger.info("Creating new tenant: email={}", tenant.getEmail());
        Tenant createdTenant = tenantService.save(tenant);
        logger.debug("Tenant created: id={}", createdTenant.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTenant);
    }

    /**
     * Updates an existing tenant's information (admin-only).
     *
     * @param id     The ID of the tenant to update.
     * @param tenant The updated tenant details.
     * @return ResponseEntity containing the updated Tenant object with HTTP 200 (OK) status, or 404 if not found.
     */
    @PutMapping("/tenants/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateTenant(@PathVariable Long id, @Valid @RequestBody Tenant tenant) {
        logger.info("Updating tenant: id={}", id);
        Tenant existingTenant = tenantService.getTenantById(id);

        if (existingTenant == null) {
            logger.warn("Tenant not found: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Tenant not found"));
        }

        existingTenant.setName(tenant.getName());
        existingTenant.setEmail(tenant.getEmail());
        existingTenant.setPhone(tenant.getPhone());
        existingTenant.setProperty(tenant.getProperty());
        existingTenant.setFloor(tenant.getFloor());
        existingTenant.setRoom(tenant.getRoom());

        Tenant updatedTenant = tenantService.save(existingTenant);
        logger.debug("Tenant updated: id={}", updatedTenant.getId());
        return ResponseEntity.ok(updatedTenant);
    }

    /**
     * Deletes a tenant from the system (admin-only).
     *
     * @param id The ID of the tenant to delete.
     * @return ResponseEntity with a success message and HTTP 200 (OK) status, or 404 if not found.
     */
    @DeleteMapping("/tenants/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteTenant(@PathVariable Long id) {
        logger.info("Deleting tenant: id={}", id);
        Tenant tenant = tenantService.getTenantById(id);

        if (tenant == null) {
            logger.warn("Tenant not found: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Tenant not found"));
        }

        tenantService.delete(id);
        logger.debug("Tenant deleted: id={}", id);
        return ResponseEntity.ok(Map.of("message", "Tenant deleted successfully"));
    }

    /**
     * Retrieves all tenants in the system (admin-only).
     *
     * @return ResponseEntity containing a List of Tenant objects with HTTP 200 (OK) status.
     */
    @GetMapping("/tenants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Tenant>> getAllTenants() {
        logger.info("Fetching all tenants");
        List<Tenant> tenants = tenantService.findAll();
        logger.debug("Retrieved {} tenants", tenants.size());
        return ResponseEntity.ok(tenants);
    }
}