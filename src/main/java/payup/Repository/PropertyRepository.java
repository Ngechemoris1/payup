package payup.payup.repository;

import payup.payup.model.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for performing CRUD operations on Property entities.
 */
@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    
    /**
     * Retrieves all properties owned by a specific user (landlord).
     * 
     * @param ownerId The ID of the landlord.
     * @return A list of properties owned by the user.
     */
    List<Property> findByOwnerId(Long ownerId);

    /**
     * Checks if a property with the given ID is owned by the specified owner.
     * 
     * @param propertyId The ID of the property to check.
     * @param ownerId The ID of the owner to verify against.
     * @return True if the property belongs to the owner, false otherwise.
     */
    boolean existsByIdAndOwnerId(Long propertyId, Long ownerId);

    /**
     * Retrieves properties owned by a specific user with pagination support.
     * 
     * @param ownerId The ID of the landlord.
     * @param pageable Pagination information.
     * @return A Page of properties owned by the user.
     */
    Page<Property> findByOwnerId(Long ownerId, Pageable pageable);

    /**
     * Finds properties by name with partial match.
     * 
     * @param namePart Part of the property name to search for.
     * @return A list of properties where the name partially matches the given string.
     */
    List<Property> findByNameContainingIgnoreCase(String namePart);
}