package payup.payup.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import payup.payup.dto.*;
import payup.payup.mapper.*;
import payup.payup.model.Property;
import payup.payup.model.Rent;
import payup.payup.model.Tenant;
import payup.payup.service.*;


import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for landlord-specific operations.
 * Provides endpoints for managing properties, tenants, and rents with DTO support.
 * All endpoints require LANDLORD role authorization.
 */
@RestController
@RequestMapping("/api/landlord")
public class LandlordController {

    private static final Logger logger = LoggerFactory.getLogger(LandlordController.class);

    private final PropertyService propertyService;
    private final TenantService tenantService;
    private final RentService rentService;
    private static UserService userService;
    private final PropertyMapper propertyMapper;
    private final TenantMapper tenantMapper;
    private final RentMapper rentMapper;

    @Autowired
    public LandlordController(PropertyService propertyService,
                              TenantService tenantService,
                              RentService rentService,
                              UserService userService,
                              PropertyMapper propertyMapper,
                              TenantMapper tenantMapper,
                              RentMapper rentMapper) {
        this.propertyService = propertyService;
        this.tenantService = tenantService;
        this.rentService = rentService;
        this.userService = userService;
        this.propertyMapper = propertyMapper;
        this.tenantMapper = tenantMapper;
        this.rentMapper = rentMapper;
    }

    /**
     * Retrieves all properties owned by the authenticated landlord.
     *
     * @return ResponseEntity containing List of PropertyDto objects or ErrorResponseDto
     */
    @GetMapping("/properties")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<?> getProperties() {
        try {
            logger.info("Fetching properties for authenticated landlord");
            Long landlordId = SecurityUtil.getAuthenticatedUserId();

            List<Property> properties = propertyService.getPropertiesByLandlord(landlordId);
            List<PropertyDto> propertyDtos = properties.stream()
                    .map(propertyMapper::toDto)
                    .collect(Collectors.toList());

            logger.info("Found {} properties for landlordId={}", propertyDtos.size(), landlordId);
            return ResponseEntity.ok(propertyDtos);

        } catch (Exception e) {
            logger.error("Error fetching properties: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto("Properties not found", e.getMessage()));
        }
    }

    /**
     * Retrieves all tenants for a specific property owned by the authenticated landlord.
     *
     * @param propertyId ID of the property
     * @return ResponseEntity containing List of TenantDto objects or ErrorResponseDto
     */
    @GetMapping("/properties/{propertyId}/tenants")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<?> getTenantsByProperty(@PathVariable Long propertyId) {
        try {
            logger.info("Fetching tenants for propertyId={}", propertyId);
            Long landlordId = SecurityUtil.getAuthenticatedUserId();

            // Verify property ownership
            if (!propertyService.isPropertyOwner(propertyId, landlordId)) {
                logger.warn("Unauthorized access to propertyId={} by landlordId={}", propertyId, landlordId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponseDto("Unauthorized", "Access to property denied"));
            }

            List<Tenant> tenants = tenantService.getTenantsByProperty(propertyId);
            List<TenantDto> tenantDtos = tenants.stream()
                    .map(tenantMapper::toDto)
                    .collect(Collectors.toList());

            logger.info("Found {} tenants for propertyId={}", tenantDtos.size(), propertyId);
            return ResponseEntity.ok(tenantDtos);

        } catch (Exception e) {
            logger.error("Error fetching tenants: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto("Tenants not found", e.getMessage()));
        }
    }

    /**
     * Retrieves rent details for a specific tenant under landlord's property.
     *
     * @param tenantId ID of the tenant
     * @return ResponseEntity containing List of RentDto objects or ErrorResponseDto
     */
    @GetMapping("/tenants/{tenantId}/rents")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<?> getRentsByTenant(@PathVariable Long tenantId) {
        try {
            logger.info("Fetching rents for tenantId={}", tenantId);
            Long landlordId = SecurityUtil.getAuthenticatedUserId();

            Tenant tenant = tenantService.getTenantById(tenantId);
            if (tenant == null || !propertyService.isPropertyOwner(tenant.getProperty().getId(), landlordId)) {
                logger.warn("Tenant not found or unauthorized: tenantId={}", tenantId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponseDto("Not found", "Tenant not found or unauthorized"));
            }

            List<Rent> rents = rentService.getRentsByTenant(tenantId);
            List<RentDto> rentDtos = rents.stream()
                    .map(rentMapper::toDto)
                    .collect(Collectors.toList());

            logger.info("Found {} rents for tenantId={}", rentDtos.size(), tenantId);
            return ResponseEntity.ok(rentDtos);

        } catch (Exception e) {
            logger.error("Error fetching rents: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Server error", e.getMessage()));
        }
    }

    /**
     * Utility class for security-related operations
     */
    private static class SecurityUtil {
        public static Long getAuthenticatedUserId() {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            return userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"))
                    .getId();
        }
    }

    /**
     * Data transfer object for error responses
     */
    private static class ErrorResponseDto {
        private final String error;
        private final String message;

        public ErrorResponseDto(String error, String message) {
            this.error = error;
            this.message = message;
        }

        public String getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }
    }
}