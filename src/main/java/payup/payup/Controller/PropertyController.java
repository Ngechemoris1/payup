package payup.payup.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import payup.payup.model.Property;
import payup.payup.model.User;
import payup.payup.service.PropertyService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    @PostMapping("/property")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProperty(@RequestBody Map<String, Object> request) {
        try {
            Property property = new Property();
            property.setName((String) request.get("name"));
            property.setType((String) request.get("type"));
            property.setLocation((String) request.get("location"));
            property.setUnits(Integer.parseInt(request.get("units").toString()));

            // New parameter for number of floors
            int numberOfFloors = request.containsKey("numberOfFloors")
                    ? Integer.parseInt(request.get("numberOfFloors").toString())
                    : 1; // Default to 1 floor if not specified

            Long landlordId = Long.valueOf(request.get("landlordId").toString());
            Property createdProperty = propertyService.createProperty(property, landlordId, numberOfFloors);
            return ResponseEntity.ok(createdProperty);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating property: " + e.getMessage());
        }
    }

    @PutMapping("/property/{propertyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProperty(@PathVariable Long propertyId, @RequestBody Map<String, Object> request) {
        try {
            Property existingProperty = propertyService.findById(propertyId)
                    .orElseThrow(() -> new IllegalArgumentException("Property not found with ID: " + propertyId));
            existingProperty.setName((String) request.get("name"));
            existingProperty.setType((String) request.get("type"));
            existingProperty.setLocation((String) request.get("location"));
            existingProperty.setUnits(Integer.parseInt(request.get("units").toString()));
            Long landlordId = Long.valueOf(request.get("landlordId").toString());
            User landlord = new User();
            landlord.setId(landlordId);
            existingProperty.setOwner(landlord);
            Property updatedProperty = propertyService.updateProperty(propertyId, existingProperty);
            return ResponseEntity.ok(updatedProperty);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating property: " + e.getMessage());
        }
    }

    @DeleteMapping("/property/{propertyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProperty(@PathVariable Long propertyId) {
        try {
            propertyService.deleteProperty(propertyId);
            return ResponseEntity.ok(Map.of("message", "Property deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting property: " + e.getMessage());
        }
    }

    @GetMapping("/properties/landlord/{landlordId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPropertiesByLandlord(@PathVariable Long landlordId) {
        List<Property> properties = propertyService.getPropertiesByLandlord(landlordId);
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/properties")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Property>> getAllProperties() {
        List<Property> properties = propertyService.findAll();
        return ResponseEntity.ok(properties);
    }
}