package payup.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import payup.payup.model.Tenant;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Tenant entities in the PayUp system.
 * Provides CRUD operations and custom queries for retrieving tenants by email, property, and search terms.
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    /**
     * Finds a tenant by their email address.
     *
     * @param email The email address to search for.
     * @return An Optional containing the Tenant if found, or empty if not.
     */
    @Query("SELECT t FROM Tenant t JOIN t.user u WHERE u.email = :email")
    Optional<Tenant> findByEmail(@Param("email") String email);

    /**
     * Retrieves all tenants in a specific property.
     *
     * @param propertyId The ID of the property.
     * @return A list of Tenant entities in the property.
     */
    List<Tenant> findByPropertyId(Long propertyId);

    /**
     * Checks if a tenant belongs to a property owned by a specific landlord.
     *
     * @param tenantId        The ID of the tenant.
     * @param propertyOwnerId The ID of the property owner (landlord).
     * @return True if the tenant belongs to a property owned by the landlord, false otherwise.
     */
    boolean existsByIdAndPropertyOwnerId(Long tenantId, Long propertyOwnerId);

    /**
     * Retrieves tenants in a specific property with pagination.
     *
     * @param propertyId The ID of the property.
     * @param pageable   Pagination and sorting information.
     * @return A Page of Tenant entities in the property.
     */
    Page<Tenant> findByPropertyId(Long propertyId, Pageable pageable);

    /**
     * Searches for tenants by name or email (case-insensitive) with pagination.
     *
     * @param searchTerm  The term to search in names.
     * @param searchTerm2 The term to search in emails (can be same as searchTerm).
     * @param pageable    Pagination and sorting information.
     * @return A Page of Tenant entities matching the search criteria.
     */
    @Query("SELECT t FROM Tenant t JOIN t.user u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm2, '%'))")
    Page<Tenant> findByNameContainingOrEmailContainingIgnoreCase(@Param("searchTerm") String searchTerm, @Param("searchTerm2") String searchTerm2, Pageable pageable);
}