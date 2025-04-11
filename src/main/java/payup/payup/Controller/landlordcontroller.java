package payup.payup.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import payup.payup.dto.PropertyDto;
import payup.payup.dto.TenantDto;
import payup.payup.mapper.PropertyMapper;
import payup.payup.mapper.TenantMapper;
import payup.payup.model.*;
import payup.payup.service.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for landlord-specific operations. Provides endpoints for managing
 * properties, tenants, and rents, restricted to users with the 'LANDLORD' role.
 */
@RestController
@RequestMapping("/api/landlord")
public class LandlordController {

    private static final Logger logger = LoggerFactory.getLogger(LandlordController.class);

    @Autowired private PropertyService propertyService;
    @Autowired private TenantService tenantService;
    @Autowired private RentService rentService;
    @Autowired private UserService userService;
    @Autowired private PropertyMapper propertyMapper;
    @Autowired private TenantMapper tenantMapper;

    /**
     * Retrieves all properties owned by the authenticated landlord.
     *
     * @return ResponseEntity containing a List of Property objects or 404 if the landlord is not found.
     */
    @GetMapping("/properties")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<?> getProperties() {
        logger.info("Fetching properties for authenticated landlord");
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User landlord = userService.findByEmail(email).orElse(null);
            if (landlord == null) {
                logger.warn("Landlord not found for email: {}", email);
                return ResponseEntity.notFound().build();
            }
            List<Property> properties = propertyService.getPropertiesByLandlord(landlord.getId());
            List<PropertyDto> propertyDtos = properties.stream()
                    .map(propertyMapper::toDto)
                    .collect(Collectors.toList());
            logger.info("Returning {} properties for landlordId={}", propertyDtos.size(), landlord.getId());
            return ResponseEntity.ok(propertyDtos);
        } catch (Exception e) {
            logger.error("Error fetching properties: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Error fetching properties: " + e.getMessage()));
        }
    }

    /**
     * Retrieves all tenants for a specific property owned by the authenticated landlord.
     *
     * @param propertyId The ID of the property.
     * @return ResponseEntity containing a List of Tenant objects or 403/404 if unauthorized or not found.
     */
    @GetMapping("/properties/{propertyId}/tenants")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<?> getTenantsByProperty(@PathVariable Long propertyId) {
        logger.info("Fetching tenants for propertyId={}", propertyId);
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User landlord = userService.findByEmail(email).orElse(null);
        if (landlord == null) {
            logger.warn("Landlord not found for email: {}", email);
            return ResponseEntity.notFound().build();
        }
        if (!propertyService.isPropertyOwner(propertyId, landlord.getId())) {
            logger.warn("Unauthorized access to propertyId={} by landlordId={}", propertyId, landlord.getId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Unauthorized access to property"));
        }
        List<Tenant> tenants = tenantService.getTenantsByProperty(propertyId);
        List<TenantDto> tenantDtos = tenants.stream()
                .map(tenantMapper::toDto) // Use TenantMapper to convert to DTO
                .collect(Collectors.toList());
        logger.info("Returning {} tenants for propertyId={}", tenantDtos.size(), propertyId);
        return ResponseEntity.ok(tenantDtos); // Return DTOs instead of raw entities
    }

    /**
     * Retrieves rent details for a specific tenant under a property owned by the authenticated landlord.
     *
     * @param tenantId The ID of the tenant.
     * @return ResponseEntity containing a List of Rent objects or 404 if not found or unauthorized.
     */
    @GetMapping("/tenants/{tenantId}/rents")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<?> getRentsByTenant(@PathVariable Long tenantId) {
        logger.info("Fetching rents for tenantId={}", tenantId);
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User landlord = userService.findByEmail(email).orElse(null);
        if (landlord == null) {
            logger.warn("Landlord not found for email: {}", email);
            return ResponseEntity.notFound().build();
        }
        Tenant tenant = tenantService.getTenantById(tenantId);
        if (tenant == null || !propertyService.isPropertyOwner(tenant.getProperty().getId(), landlord.getId())) {
            logger.warn("Tenant not found or unauthorized: tenantId={}", tenantId);
            return ResponseEntity.notFound().build();
        }
        List<Rent> rents = rentService.getRentsByTenant(tenantId);
        return ResponseEntity.ok(rents);
    }
}