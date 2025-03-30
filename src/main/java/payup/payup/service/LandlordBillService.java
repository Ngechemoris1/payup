package payup.payup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import payup.payup.model.LandlordBill;
import payup.payup.model.Property;
import payup.payup.model.User;
import payup.repository.LandlordBillRepository;
import payup.repository.PropertyRepository;
import payup.repository.UserRepository;

import java.util.List;

/**
 * Service class for managing LandlordBill entities within the GoldenProperties system.
 * Provides CRUD operations with entity validation to ensure data integrity.
 */
@Service
public class LandlordBillService {

    private static final Logger logger = LoggerFactory.getLogger(LandlordBillService.class);

    @Autowired
    private LandlordBillRepository landlordBillRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    /**
     * Creates a new landlord bill after validating the associated landlord and property.
     *
     * @param landlordBill The LandlordBill object to persist.
     * @return The saved LandlordBill entity.
     * @throws IllegalArgumentException if the bill details are invalid.
     * @throws RuntimeException if the landlord or property is not found.
     */
    @Transactional
    public LandlordBill createLandlordBill(LandlordBill landlordBill) {
        if (landlordBill == null || landlordBill.getAmount() == null || landlordBill.getAmount() <= 0) {
            throw new IllegalArgumentException("Invalid bill details: amount must be positive");
        }
        User landlord = getUser(landlordBill.getLandlord().getId());
        Property property = getProperty(landlordBill.getProperty().getId());
        landlordBill.setLandlord(landlord);
        landlordBill.setProperty(property);
        LandlordBill savedBill = landlordBillRepository.save(landlordBill);
        logger.info("Created new landlord bill with ID: {}", savedBill.getId());
        return savedBill;
    }

    /**
     * Retrieves all bills associated with a specific landlord.
     *
     * @param landlordId The ID of the landlord.
     * @return A list of LandlordBill entities.
     * @throws RuntimeException if the landlord is not found.
     */
    public List<LandlordBill> getBillsByLandlord(Long landlordId) {
        User landlord = getUser(landlordId);
        return landlordBillRepository.findByLandlord(landlord);
    }

    /**
     * Retrieves all bills associated with a specific property.
     *
     * @param propertyId The ID of the property.
     * @return A list of LandlordBill entities.
     * @throws RuntimeException if the property is not found.
     */
    public List<LandlordBill> getBillsByProperty(Long propertyId) {
        Property property = getProperty(propertyId);
        return landlordBillRepository.findByProperty(property);
    }

    /**
     * Retrieves all bills for a specific landlord and property.
     *
     * @param landlordId The ID of the landlord.
     * @param propertyId The ID of the property.
     * @return A list of LandlordBill entities.
     * @throws RuntimeException if the landlord or property is not found.
     */
    public List<LandlordBill> getBillsByLandlordAndProperty(Long landlordId, Long propertyId) {
        User landlord = getUser(landlordId);
        Property property = getProperty(propertyId);
        return landlordBillRepository.findByLandlordAndProperty(landlord, property);
    }

    /**
     * Deletes a landlord bill by its ID.
     *
     * @param id The ID of the bill to delete.
     * @throws RuntimeException if the bill is not found.
     */
    @Transactional
    public void deleteLandlordBill(Long id) {
        if (!landlordBillRepository.existsById(id)) {
            throw new RuntimeException("Bill not found with ID: " + id);
        }
        landlordBillRepository.deleteById(id);
    }

    // Helper method to fetch a User by ID
    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Landlord not found with ID: " + id));
    }

    // Helper method to fetch a Property by ID
    private Property getProperty(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found with ID: " + id));
    }
}