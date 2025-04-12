package payup.payup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import payup.payup.model.LandlordBill;
import payup.payup.model.Property;
import payup.payup.model.User;
import payup.repository.LandlordBillRepository;
import payup.repository.PropertyRepository;
import payup.repository.UserRepository;


import java.util.List;

@Service
public class LandlordBillService {

    private static final Logger logger = LoggerFactory.getLogger(LandlordBillService.class);

    @Autowired
    private LandlordBillRepository landlordBillRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Transactional
    public LandlordBill createLandlordBill(LandlordBill landlordBill) {
        if (landlordBill == null || landlordBill.getAmount() == null || landlordBill.getAmount() <= 0) {
            throw new IllegalArgumentException("Invalid bill details: amount must be positive");
        }

        // Fetch landlord using landlordId if landlord is null
        User landlord;
        if (landlordBill.getLandlord() != null) {
            landlord = getUser(landlordBill.getLandlord().getId());
        } else if (landlordBill.getLandlordId() != null) {
            landlord = getUser(landlordBill.getLandlordId());
        } else {
            throw new IllegalArgumentException("Landlord ID must be provided");
        }

        // Fetch property using propertyId if property is null
        Property property;
        if (landlordBill.getProperty() != null) {
            property = getProperty(landlordBill.getProperty().getId());
        } else if (landlordBill.getPropertyId() != null) {
            property = getProperty(landlordBill.getPropertyId());
        } else {
            throw new IllegalArgumentException("Property ID must be provided");
        }

        landlordBill.setLandlord(landlord);
        landlordBill.setProperty(property);

        LandlordBill savedBill = landlordBillRepository.save(landlordBill);
        logger.info("Created new landlord bill with ID: {}", savedBill.getId());
        return savedBill;
    }

    public List<LandlordBill> getBillsByLandlord(Long landlordId) {
        User landlord = getUser(landlordId);
        return landlordBillRepository.findByLandlord(landlord);
    }

    public List<LandlordBill> getBillsByProperty(Long propertyId) {
        Property property = getProperty(propertyId);
        return landlordBillRepository.findByProperty(property);
    }

    public List<LandlordBill> getBillsByLandlordAndProperty(Long landlordId, Long propertyId) {
        User landlord = getUser(landlordId);
        Property property = getProperty(propertyId);
        return landlordBillRepository.findByLandlordAndProperty(landlord, property);
    }

    @Transactional
    public void deleteLandlordBill(Long id) {
        if (!landlordBillRepository.existsById(id)) {
            throw new RuntimeException("Bill not found with ID: " + id);
        }
        landlordBillRepository.deleteById(id);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Landlord not found with ID: " + id));
    }

    private Property getProperty(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found with ID: " + id));
    }
}