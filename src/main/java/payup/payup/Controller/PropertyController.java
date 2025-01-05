package payup.payup.controller;

import payup.payup.model.Property;
import payup.payup.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/property")
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    /**
     * Adds a new property to the landlord's portfolio.
     * @param property Property object to add.
     * @return ResponseEntity with the created property or an error if the operation fails.
     */
    @PostMapping
    public ResponseEntity<?> addProperty(@RequestBody Property property) {
        User landlord = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        property.setOwner(landlord);
        return ResponseEntity.ok(propertyService.addProperty(property));
    }

    /**
     * Updates an existing property.
     * @param propertyId ID of the property to update.
     * @param property Updated property details.
     * @return ResponseEntity indicating success or failure of the update.
     */
    @PutMapping("/{propertyId}")
    public ResponseEntity<?> updateProperty(@PathVariable Long propertyId, @RequestBody Property property) {
        Property existingProperty = propertyService.getPropertyById(propertyId);
        if (existingProperty == null || !existingProperty.getOwner().getId().equals(getLandlordId())) {
            return ResponseEntity.notFound().build();
        }
        existingProperty.setName(property.getName());
        // Update other fields as needed
        return ResponseEntity.ok(propertyService.save(existingProperty));
    }

    /**
     * Helper method to get the landlord's ID from the security context.
     * @return The landlord's ID.
     */
    private Long getLandlordId() {
        User landlord = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return landlord.getId();
    }
}