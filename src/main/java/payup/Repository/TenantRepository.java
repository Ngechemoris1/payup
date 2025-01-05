package payup.payup.repository;

import payup.payup.model.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for performing CRUD operations on Tenant entities.
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    
    /**
     * Finds a tenant by email address.
     * 
     * @param email The email to search for.
     * @return An Optional containing the Tenant if found, or empty if not.
     */
    Optional<Tenant> findByEmail(String email);

    /**
     * Retrieves all tenants associated with a specific property.
     * 
     * @param propertyId The ID of the property to search tenants for.
     * @return A list of tenants in that property.
     */
    List<Tenant> findByPropertyId(Long propertyId);

    /**
     * Checks if a tenant with the given ID resides in a property owned by the specified landlord.
     * 
     * @param tenantId The ID of the tenant to check.
     * @param propertyOwnerId The ID of the property owner to verify against.
     * @return True if the tenant belongs to a property owned by the landlord, false otherwise.
     */
    boolean existsByIdAndPropertyOwnerId(Long tenantId, Long propertyOwnerId);

    /**
     * Retrieves tenants for a property with pagination support.
     * 
     * @param propertyId The ID of the property.
     * @param pageable Pagination information.
     * @return A Page of tenants in the specified property.
     */
    Page<Tenant> findByPropertyId(Long propertyId, Pageable pageable);

    /**
     * Searches for tenants by name or email with partial match and pagination.
     * 
     * @param searchTerm Part of the name or email to search for.
     * @param pageable Pagination information.
     * @return A Page of tenants matching the search term.
     */
    Page<Tenant> findByNameContainingOrEmailContainingIgnoreCase(String searchTerm, String searchTerm, Pageable pageable);
}