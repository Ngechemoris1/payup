package payup.payup.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import payup.payup.dto.*;
import payup.payup.mapper.PropertyMapper;
import payup.payup.model.Property;
import payup.payup.service.PropertyService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing property operations.
 * Provides CRUD endpoints for properties with ADMIN role authorization.
 */
@RestController
@RequestMapping("/api/admin/property")
public class PropertyController {

    private static final Logger logger = LoggerFactory.getLogger(PropertyController.class);

    private final PropertyService propertyService;
    private final PropertyMapper propertyMapper;

    @Autowired
    public PropertyController(PropertyService propertyService, PropertyMapper propertyMapper) {
        this.propertyService = propertyService;
        this.propertyMapper = propertyMapper;
    }

    /**
     * Creates a new property with the specified details
     * @param request Property creation request DTO
     * @return ResponseEntity with created PropertyDto or ErrorResponseDto
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProperty(@Valid @RequestBody PropertyCreateRequestDto request) {
        try {
            logger.info("Creating property: {}", request.getName());

            Property createdProperty = propertyService.createProperty(
                    propertyMapper.toEntity(request),
                    request.getLandlordId(),
                    request.getNumberOfFloors()
            );

            PropertyDto responseDto = propertyMapper.toDto(createdProperty);
            logger.info("Property created successfully with ID: {}", createdProperty.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);

        } catch (Exception e) {
            logger.error("Failed to create property: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseDto("Property creation failed", e.getMessage()));
        }
    }

    /**
     * Updates an existing property
     * @param propertyId ID of property to update
     * @param request Property update request DTO
     * @return ResponseEntity with updated PropertyDto or ErrorResponseDto
     */
    @PutMapping("/{propertyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProperty(
            @PathVariable Long propertyId,
            @Valid @RequestBody PropertyUpdateRequestDto request) {

        try {
            logger.info("Updating property ID: {}", propertyId);

            Property property = propertyMapper.toEntity(request);
            Property updatedProperty = propertyService.updateProperty(propertyId, property);

            PropertyDto responseDto = propertyMapper.toDto(updatedProperty);
            logger.info("Property {} updated successfully", propertyId);
            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            logger.error("Failed to update property {}: {}", propertyId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseDto("Property update failed", e.getMessage()));
        }
    }

    /**
     * Deletes a property by ID
     * @param propertyId ID of property to delete
     * @return ResponseEntity with success message or ErrorResponseDto
     */
    @DeleteMapping("/{propertyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProperty(@PathVariable Long propertyId) {
        try {
            logger.info("Deleting property ID: {}", propertyId);
            propertyService.deleteProperty(propertyId);

            logger.info("Property {} deleted successfully", propertyId);
            return ResponseEntity.ok(new BasicResponseDto("Property deleted successfully"));

        } catch (Exception e) {
            logger.error("Failed to delete property {}: {}", propertyId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseDto("Property deletion failed", e.getMessage()));
        }
    }

    /**
     * Gets all properties for a specific landlord
     * @param landlordId ID of landlord
     * @return ResponseEntity with List of PropertyDto or ErrorResponseDto
     */
    @GetMapping("/landlord/{landlordId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPropertiesByLandlord(@PathVariable Long landlordId) {
        try {
            logger.info("Fetching properties for landlord ID: {}", landlordId);
            List<Property> properties = propertyService.getPropertiesByLandlord(landlordId);

            List<PropertyDto> responseDtos = properties.stream()
                    .map(propertyMapper::toDto)
                    .collect(Collectors.toList());

            logger.info("Found {} properties for landlord {}", responseDtos.size(), landlordId);
            return ResponseEntity.ok(responseDtos);

        } catch (Exception e) {
            logger.error("Failed to fetch properties for landlord {}: {}", landlordId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseDto("Failed to fetch properties", e.getMessage()));
        }
    }

    /**
     * Gets all properties in the system
     * @return ResponseEntity with List of PropertyDto or ErrorResponseDto
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllProperties() {
        try {
            logger.info("Fetching all properties");
            List<Property> properties = propertyService.findAll();

            List<PropertyDto> responseDtos = properties.stream()
                    .map(propertyMapper::toDto)
                    .collect(Collectors.toList());

            logger.info("Found {} properties", responseDtos.size());
            return ResponseEntity.ok(responseDtos);

        } catch (Exception e) {
            logger.error("Failed to fetch properties: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseDto("Failed to fetch properties", e.getMessage()));
        }
    }
}