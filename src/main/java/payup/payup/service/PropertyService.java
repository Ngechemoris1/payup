package payup.payup.service;

import payup.payup.exception.UserNotFoundException;
import payup.payup.model.Floor;
import payup.payup.model.Property;
import payup.payup.model.Room;
import payup.payup.model.User;
import payup.repository.FloorRepository;
import payup.repository.PropertyRepository;
import payup.repository.RoomRepository;
import payup.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class responsible for managing property-related operations in the PayUp system.
 * Provides methods to create, update, delete, and retrieve properties, ensuring proper
 * association with landlords and handling database transactions.
 */
@Service
public class PropertyService {

    private static final Logger logger = LoggerFactory.getLogger(PropertyService.class);

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FloorRepository floorRepository;

    @Autowired
    private RoomRepository roomRepository;

    /**
     * Creates a new property and assigns it to a specified landlord, including floors and rooms.
     *
     * @param property       The Property object containing details such as name, type, location, and units.
     * @param landlordId     The ID of the landlord (User with LANDLORD role) to whom the property will be assigned.
     * @param numberOfFloors
     * @return The created Property object with an assigned ID, floors, rooms, and updated timestamps.
     * @throws UserNotFoundException    If no landlord exists with the specified ID.
     * @throws IllegalArgumentException If the user with the specified ID is not a landlord.
     */
    @Transactional
    public Property createProperty(Property property, Long landlordId, int numberOfFloors) throws UserNotFoundException {
        logger.info("Creating property with {} floors for landlord ID: {}", numberOfFloors, landlordId);

        User landlord = userRepository.findById(landlordId)
                .orElseThrow(() -> new UserNotFoundException("Landlord not found with ID: " + landlordId));
        if (!landlord.getRole().equals(User.UserRole.LANDLORD)) {
            throw new IllegalArgumentException("User with ID " + landlordId + " is not a landlord");
        }

        if (numberOfFloors < 1) {
            throw new IllegalArgumentException("Property must have at least 1 floor");
        }

        if (property.getUnits() < numberOfFloors) {
            throw new IllegalArgumentException("Number of units cannot be less than number of floors");
        }

        property.setOwner(landlord);
        property.setCreatedAt(LocalDateTime.now());
        property.setUpdatedAt(LocalDateTime.now());
        Property savedProperty = propertyRepository.save(property);

        int unitsPerFloor = (int) Math.ceil((double) property.getUnits() / numberOfFloors);
        int remainingUnits = property.getUnits();

        // Get the highest existing room number globally
        int maxRoomNumber = roomRepository.findMaxRoomNumber().orElse(0);

        for (int floorNum = 0; floorNum < numberOfFloors; floorNum++) {
            Floor floor = new Floor();
            floor.setFloorNumber(floorNum);
            floor.setFloorName(getFloorName(floorNum));
            floor.setProperty(savedProperty);
            Floor savedFloor = floorRepository.save(floor);

            int floorUnits = Math.min(unitsPerFloor, remainingUnits);
            createRoomsForFloor(savedProperty, savedFloor, floorUnits, floorNum, maxRoomNumber);
            remainingUnits -= floorUnits;
            maxRoomNumber += floorUnits;
        }

        return savedProperty;
    }

    private String getFloorName(int floorNumber) {
        if (floorNumber == 0) return "Ground Floor";
        if (floorNumber == 1) return "1st Floor";
        if (floorNumber == 2) return "2nd Floor";
        if (floorNumber == 3) return "3rd Floor";
        return floorNumber + "th Floor";
    }

    private void createRoomsForFloor(Property property, Floor floor, int units, int floorNumber, int startingRoomNumber) {
        for (int i = 0; i < units; i++) {
            Room room = new Room();
            room.setRoomNumber(startingRoomNumber + i + 1);
            room.setOccupied(false);
            room.setRentAmount(1000.0);
            room.setFloor(floor);
            room.setProperty(property);
            roomRepository.save(room);
        }
    }

    public Property getPropertyById(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found with ID: " + id));
    }

    /**
     * Updates an existing property with new details.
     *
     * @param propertyId      The ID of the property to update.
     * @param updatedProperty The Property object containing the updated details (e.g., name, type, location, units, owner).
     * @return The updated Property object after persisting changes to the database.
     * @throws IllegalArgumentException If no property exists with the specified ID.
     */
    @Transactional
    public Property updateProperty(Long propertyId, Property updatedProperty) {
        logger.info("Updating property with ID: {}", propertyId);
        Property existingProperty = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found with ID: " + propertyId));

        // Update all relevant fields
        existingProperty.setName(updatedProperty.getName());
        existingProperty.setType(updatedProperty.getType());
        existingProperty.setLocation(updatedProperty.getLocation());
        existingProperty.setUnits(updatedProperty.getUnits());
        if (updatedProperty.getOwner() != null) {
            existingProperty.setOwner(updatedProperty.getOwner());
        }
        existingProperty.setUpdatedAt(LocalDateTime.now());

        return propertyRepository.save(existingProperty);
    }

    /**
     * Deletes a property from the system by its ID.
     *
     * @param propertyId The ID of the property to delete.
     * @throws IllegalArgumentException If no property exists with the specified ID.
     */
    public void deleteProperty(Long propertyId) throws IllegalArgumentException {
        logger.info("Deleting property with ID: {}", propertyId);
            if (!propertyRepository.existsById(propertyId)) {
                throw new IllegalArgumentException("Property not found with ID: " + propertyId);
            }
            propertyRepository.deleteById(propertyId);
    }

    /**
     * Retrieves all properties in the system.
     *
     * @return A list of all Property objects stored in the database.
     */
    public List<Property> findAll() {
        return propertyRepository.findAll();
    }

    /**
     * Retrieves all properties owned by a specific landlord.
     *
     * @param landlordId The ID of the landlord whose properties are to be retrieved.
     * @return A list of Property objects owned by the specified landlord.
     */
    public List<Property> getPropertiesByLandlord(Long landlordId) {
        return propertyRepository.findByOwnerId(landlordId);
    }

    /**
     * Finds a property by its ID.
     *
     * @param id The ID of the property to find.
     * @return An Optional containing the Property if found, or empty if not.
     */
    public Optional<Property> findById(Long id) {
        return propertyRepository.findById(id);
    }

    /**
     * Checks if a specific landlord is the owner of a given property.
     *
     * @param propertyId The ID of the property to check.
     * @param landlordId The ID of the landlord to verify against the property's owner.
     * @return true if the landlord owns the property, false otherwise.
     * @throws IllegalArgumentException If no property exists with the specified ID.
     */
    public boolean isPropertyOwner(Long propertyId, Long landlordId) throws IllegalArgumentException {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found with ID: " + propertyId));
        return property.getOwner() != null && property.getOwner().getId().equals(landlordId);
    }
}