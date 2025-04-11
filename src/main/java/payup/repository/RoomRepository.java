package payup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import payup.payup.model.Property;
import payup.payup.model.Room;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Room entities in the PayUp system.
 * Provides CRUD operations and custom queries for retrieving rooms based on property, floor, and occupancy.
 */
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Retrieves the highest room number currently assigned across all rooms in the database.
     * This is used to ensure new rooms are assigned unique room numbers when the roomNumber
     * column has a global unique constraint.
     *
     * @return An Optional containing the maximum room number, or empty if no rooms exist.
     */
    @Query("SELECT MAX(r.roomNumber) FROM Room r")
    Optional<Integer> findMaxRoomNumber();

    /**
     * Retrieves all rooms in a specific property.
     *
     * @param property The Property entity to query by.
     * @return A list of Room entities in the property.
     */
    List<Room> findByProperty(Property property);

    /**
     * Retrieves all rooms in a specific property on a given floor.
     *
     * @param property  The Property entity to query by.
     * @param floorName The name of the floor.
     * @return A list of Room entities matching the property and floor.
     */
    List<Room> findByPropertyAndFloorFloorName(Property property, String floorName);

    /**
     * Retrieves all rooms in a specific property with a given occupancy status.
     *
     * @param property   The Property entity to query by.
     * @param isOccupied The occupancy status (true for occupied, false for vacant).
     * @return A list of Room entities matching the property and occupancy status.
     */
    List<Room> findByPropertyAndIsOccupied(Property property, Boolean isOccupied);

    /**
     * Retrieves all vacant rooms in a specific property.
     *
     * @param property The Property entity to query by.
     * @return A list of vacant Room entities in the property.
     */
    List<Room> findByPropertyAndIsOccupiedFalse(Property property);

    /**
     * Retrieves all occupied rooms in a specific property.
     *
     * @param property The Property entity to query by.
     * @return A list of occupied Room entities in the property.
     */
    List<Room> findByPropertyAndIsOccupiedTrue(Property property);
}