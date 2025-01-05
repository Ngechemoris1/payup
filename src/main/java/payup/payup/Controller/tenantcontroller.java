package payup.payup.controller;

import payup.payup.model.*;
import payup.payup.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenant")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private RentService rentService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Retrieves the current tenant's profile information.
     * @return ResponseEntity with the tenant's profile details.
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<?> getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Tenant tenant = tenantService.findByEmail(email);
        if (tenant == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tenant);
    }

    /**
     * Updates the current tenant's profile information.
     * @param tenant Tenant object containing updated information.
     * @return ResponseEntity indicating if the update was successful.
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<?> updateProfile(@RequestBody Tenant tenant) {
        Tenant existingTenant = tenantService.getTenantById(tenant.getId());
        String authenticatedEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        
        if (existingTenant == null || !existingTenant.getEmail().equals(authenticatedEmail)) {
            return ResponseEntity.notFound().build();
        }
        existingTenant.setName(tenant.getName());
        existingTenant.setEmail(tenant.getEmail());
        existingTenant.setPhone(tenant.getPhone());
        
        boolean propertyChanged = !existingTenant.getProperty().equals(tenant.getProperty());
        if (propertyChanged) {
            // Additional logic for property change might be needed
        }
        existingTenant.setFloor(tenant.getFloor());
        existingTenant.setRoom(tenant.getRoom());
        
        return ResponseEntity.ok(tenantService.save(existingTenant));
    }

    /**
     * Retrieves the rent dues for the current tenant.
     * @return ResponseEntity with the list of rent dues for the tenant.
     */
    @GetMapping("/rent-dues")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<?> getRentDues() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Tenant tenant = tenantService.findByEmail(email);
        
        if (tenant == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Rent> rentDues = rentService.getRentsByTenant(tenant.getId());
        return ResponseEntity.ok(rentDues);
    }

    /**
     * Allows the tenant to send a notification to the admin.
     * @param message The message to be sent to the admin.
     * @return ResponseEntity indicating the success of the notification.
     */
    @PostMapping("/notify-admin")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<?> notifyAdmin(@RequestBody String message) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Tenant tenant = tenantService.findByEmail(email);
        
        if (tenant == null) {
            return ResponseEntity.notFound().build();
        }
        
        Notification notification = new Notification(message, tenant);
        notificationService.sendNotificationToAdmin(notification);
        
        return ResponseEntity.ok("Notification sent to admin");
    }
}