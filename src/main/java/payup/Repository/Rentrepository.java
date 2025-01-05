package payup.payup.repository;

import payup.payup.model.Rent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for performing CRUD operations on Rent entities.
 */
@Repository
public interface RentRepository extends JpaRepository<Rent, Long> {
    
    /**
     * Retrieves all rent records for a specific tenant.
     * 
     * @param tenantId The ID of the tenant whose rent records are needed.
     * @return A list of Rent objects for the tenant.
     */
    List<Rent> findByTenantId(Long tenantId);

    /**
     * Finds all rent payments that are due or overdue for a tenant with pagination.
     * 
     * @param tenantId The ID of the tenant.
     * @param dueDate The date after which rents are considered due or overdue.
     * @param pageable Pagination information.
     * @return A Page of rent records that are due or overdue.
     */
    Page<Rent> findByTenantIdAndDueDateLessThanEqualAndPaidIsFalse(Long tenantId, LocalDate dueDate, Pageable pageable);

    /**
     * Finds all rent payments that have not been paid for a tenant up to a specific date with pagination.
     * 
     * @param tenantId The ID of the tenant.
     * @param dueDate The latest due date to consider.
     * @param pageable Pagination information.
     * @return A Page of unpaid rent records.
     */
    Page<Rent> findByTenantIdAndDueDateLessThanEqualAndPaidIsFalseOrderByDueDateDesc(Long tenantId, LocalDate dueDate, Pageable pageable);

    /**
     * Retrieves all paid rent records for a tenant with pagination.
     * 
     * @param tenantId The ID of the tenant.
     * @param pageable Pagination information.
     * @return A Page of paid rent records for the tenant.
     */
    Page<Rent> findByTenantIdAndPaidIsTrue(Long tenantId, Pageable pageable);
}