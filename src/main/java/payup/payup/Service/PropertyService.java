package payup.payup.service;

import payup.payup.model.Property;
import payup.payup.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    /**
     * Adds a new property to the system.
     * 
     * @param property The property to add.
     * @return The saved property object.
     */
    @Transactional
    public Property addProperty(Property property) {
        return propertyRepository.save(property);
    }

    /**
     * Updates an existing property.
     * 
     * @param property The property with updated information.
     * @return The updated property object.
     */
    @Transactional
    public Property save(Property property) {
        return propertyRepository.save(property);
    }

    /**
     * Retrieves all properties for a specific landlord.
     * 
     * @param landlordId The ID of the landlord.
     * @return A list of properties owned by the landlord.
     */
    public List<Property> getPropertiesByLandlord(Long landlordId) {
        return propertyRepository.findByOwnerId(landlordId);
    }

    /**
     * Finds a property by its ID.
     * 
     * @param propertyId The ID of the property to fetch.
     * @return The property if found, null otherwise.
     */
    public Property getPropertyById(Long propertyId) {
        return propertyRepository.findById(propertyId).orElse(null);
    }

    /**
     * Checks if a property belongs to a specific landlord.
     * 
     * @param propertyId The ID of the property to check.
     * @param ownerId The ID of the landlord to verify against.
     * @return true if the landlord owns the property, false otherwise.
     */
    public boolean isPropertyOwner(Long propertyId, Long ownerId) {
        return propertyRepository.existsByIdAndOwnerId(propertyId, ownerId);
    }
}