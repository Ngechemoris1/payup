package payup.payup.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import payup.payup.model.Property;
import payup.payup.model.Room;
import payup.repository.PropertyRepository;
import payup.repository.RoomRepository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Service class responsible for managing room-related operations within the GoldenProperties system.
 * Provides methods to create and retrieve room data, ensuring proper integration with properties
 * and consistent data management through RoomRepository and PropertyRepository.
 */
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final PropertyRepository propertyRepository;
    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);

    /**
     * Constructs a RoomService instance with dependency injection for repositories.
     *
     * @param roomRepository The repository for room data access.
     * @param propertyRepository The repository for property data access.
     */
    public RoomService(RoomRepository roomRepository, PropertyRepository propertyRepository) {
        this.roomRepository = roomRepository;
        this.propertyRepository = propertyRepository;
    }

    /**
     * Adds a new room to a specified property.
     * Validates the input parameters, associates the room with the provided property,
     * sets the initial occupancy status to unoccupied, and persists the room in the database.
     *
     * @param propertyId The ID of the property to which the room will be added. Must not be null.
     * @param room The Room object containing details such as room number and rent amount. Must not be null and must have valid attributes.
     * @return The persisted Room entity with updated properties.
     * @throws IllegalArgumentException if propertyId or room is null, or if room attributes (e.g., roomNumber, rentAmount) are invalid.
     * @throws RuntimeException if the specified property is not found in the database.
     */
    public Room addRoom(Long propertyId, Room room) {
        logger.info("Adding a new room to property with ID: {}", propertyId);
        Objects.requireNonNull(propertyId, "Property ID must not be null");
        Objects.requireNonNull(room, "Room details must not be null");
        validateRoomDetails(room);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found with ID: " + propertyId));
        room.setProperty(property);
        room.setOccupied(false); // Set the initial occupancy status to unoccupied
        return roomRepository.save(room);
    }

    /**
     * Validates the essential attributes of a Room object.
     *
     * @param room The Room object to validate.
     * @throws IllegalArgumentException if roomNumber is not positive or rentAmount is not positive.
     */
    private void validateRoomDetails(Room room) {
        if (room.getRoomNumber() <= 0) {
            throw new IllegalArgumentException("Room number must be a positive integer");
        }
        if (room.getRentAmount() <= 0) {
            throw new IllegalArgumentException("Rent amount must be a positive value");
        }
    }

    /**
     * Retrieves all vacant rooms in a specified property.
     *
     * @param propertyId The ID of the property to query for vacant rooms. Must not be null.
     * @return A list of Room entities that are not occupied within the specified property.
     * @throws IllegalArgumentException if propertyId is null.
     * @throws RuntimeException if the specified property is not found.
     */
    @Transactional(readOnly = true)
    public List<Room> getVacantRooms(Long propertyId) {
        logger.info("Retrieving vacant rooms for propertyId={}", propertyId);
        Objects.requireNonNull(propertyId, "Property ID must not be null");
        Property property = propertyRepository.findById(propertyId).orElseThrow(() -> new RuntimeException("Property not found"));
        List<Room> vacantRooms = roomRepository.findByPropertyAndIsOccupiedFalse(property);
        logger.debug("Retrieved {} vacant rooms", vacantRooms.size());
        return vacantRooms;
    }
}