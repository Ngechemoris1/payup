package payup.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import payup.payup.model.Rent;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for managing Rent entities in the PayUp system.
 * Provides CRUD operations and custom queries for retrieving rent records based on tenant and payment status.
 */
@Repository
public interface RentRepository extends JpaRepository<Rent, Long> {

    /**
     * Retrieves all rent records for a specific tenant.
     *
     * @param tenantId The ID of the tenant.
     * @return A list of Rent entities for the tenant.
     */
    List<Rent> findByTenantId(Long tenantId);

    /**
     * Retrieves unpaid rent records for a tenant due on or before a specific date, with pagination.
     *
     * @param tenantId The ID of the tenant.
     * @param dueDate  The latest due date to consider.
     * @param pageable Pagination and sorting information.
     * @return A Page of unpaid Rent entities due on or before the specified date.
     */
    Page<Rent> findByTenantIdAndDueDateLessThanEqualAndPaidIsFalse(Long tenantId, LocalDate dueDate, Pageable pageable);

    /**
     * Retrieves paid rent records for a tenant, with pagination.
     *
     * @param tenantId The ID of the tenant.
     * @param pageable Pagination and sorting information.
     * @return A Page of paid Rent entities for the tenant.
     */
    Page<Rent> findByTenantIdAndPaidIsTrue(Long tenantId, Pageable pageable);
}