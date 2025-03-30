package payup.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import payup.payup.model.Property;

import java.util.List;

/**
 * Repository interface for managing Property entities in the PayUp system.
 * Provides CRUD operations and custom queries for retrieving properties based on ownership and name.
 */
@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    /**
     * Retrieves all properties owned by a specific user.
     *
     * @param ownerId The ID of the owner (landlord).
     * @return A list of Property entities owned by the user.
     */
    List<Property> findByOwnerId(Long ownerId);

    /**
     * Checks if a property is owned by a specific user.
     *
     * @param propertyId The ID of the property.
     * @param ownerId    The ID of the owner.
     * @return True if the property is owned by the user, false otherwise.
     */
    boolean existsByIdAndOwnerId(Long propertyId, Long ownerId);

    /**
     * Retrieves properties owned by a specific user with pagination.
     *
     * @param ownerId  The ID of the owner (landlord).
     * @param pageable Pagination and sorting information.
     * @return A Page of Property entities owned by the user.
     */
    Page<Property> findByOwnerId(Long ownerId, Pageable pageable);

    /**
     * Retrieves properties with names containing the specified string (case-insensitive).
     *
     * @param namePart The partial name to search for.
     * @return A list of Property entities matching the name criteria.
     */
    List<Property> findByNameContainingIgnoreCase(String namePart);
}