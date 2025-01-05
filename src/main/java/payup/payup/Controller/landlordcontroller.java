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
@RequestMapping("/api/landlord")
public class LandlordController {

    @Autowired 
    private PropertyService propertyService;

    @Autowired 
    private TenantService tenantService;

    @Autowired 
    private RentService rentService;

    /**
     * Retrieves all properties managed by the logged-in landlord.
     * @return ResponseEntity with the landlord's properties.
     */
    @GetMapping("/properties")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<?> getProperties() {
        User landlord = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(propertyService.getPropertiesByLandlord(landlord.getId()));
    }

    /**
     * Gets all tenants for a specific property managed by the landlord.
     * @param propertyId ID of the property to fetch tenants for.
     * @return ResponseEntity with the list of tenants for the property.
     */
    @GetMapping("/properties/{propertyId}/tenants")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<?> getTenantsByProperty(@PathVariable Long propertyId) {
        User landlord = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!propertyService.isPropertyOwner(propertyId, landlord.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(tenantService.getTenantsByProperty(propertyId));
    }

    /**
     * Retrieves rent information for a specific tenant under the landlord's property.
     * @param tenantId ID of the tenant whose rent details are needed.
     * @return ResponseEntity with the tenant's rent information or not found if the tenant doesn't exist.
     */
    @GetMapping("/tenants/{tenantId}/rents")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<?> getRentsByTenant(@PathVariable Long tenantId) {
        User landlord = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Tenant tenant = tenantService.getTenantById(tenantId);
        if (tenant == null || !propertyService.isPropertyOwner(tenant.getProperty().getId(), landlord.getId())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rentService.getRentsByTenant(tenantId));
    }
}