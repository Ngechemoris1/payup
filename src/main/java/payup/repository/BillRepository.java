package payup.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import payup.payup.model.Bill;
import payup.payup.model.Tenant;

import java.util.List;

/**
 * Repository interface for managing Bill entities in the PayUp system.
 * Extends JpaRepository to provide basic CRUD operations and includes custom queries
 * for retrieving bills based on tenant, status, and payment state.
 */
public interface BillRepository extends JpaRepository<Bill, Long> {

    /**
     * Retrieves a paginated list of unpaid bills.
     *
     * @param pageable Pagination and sorting information.
     * @return A Page of Bill entities where isPaid is false.
     */
    Page<Bill> findByIsPaidFalse(Pageable pageable);

    /**
     * Retrieves all bills for a specific tenant.
     *
     * @param tenant The Tenant entity to query by.
     * @return A list of Bill entities associated with the tenant.
     */
    List<Bill> findByTenant(Tenant tenant);


    /**
     * Retrieves all bills for a specific tenant with a given status.
     *
     * @param tenant The Tenant entity to query by.
     * @param status The BillStatus to filter by (e.g., PENDING, PAID, OVERDUE).
     * @return A list of Bill entities matching the tenant and status.
     */
    List<Bill> findByTenantAndStatus(Tenant tenant, Bill.BillStatus status);

    /**
     * Calculates the total amount of pending bills for a specific tenant.
     *
     * @param tenant The Tenant entity to query by.
     * @return The sum of amounts for pending bills, or null if none exist.
     */
    @Query("SELECT SUM(b.amount) FROM Bill b WHERE b.tenant = :tenant AND b.status = 'PENDING'")
    Double sumPendingAmountByTenant(Tenant tenant);

    /**
     * Calculates the total amount of overdue bills for a specific tenant.
     *
     * @param tenant The Tenant entity to query by.
     * @return The sum of amounts for overdue bills, or null if none exist.
     */
    @Query("SELECT SUM(b.amount) FROM Bill b WHERE b.tenant = :tenant AND b.status = 'OVERDUE'")
    Double sumOverdueAmountByTenant(Tenant tenant);

    /**
     * Retrieves all bills associated with a specific tenant by their ID.
     *
     * @param tenantId The ID of the tenant.
     * @return A list of Bill entities linked to the specified tenant.
     */
    List<Bill> findByTenantId(Long tenantId);

    /**
     * Retrieves all unpaid bills.
     *
     * @return A list of Bill entities where isPaid is false.
     */
    List<Bill> findByIsPaidFalse();

    /**
     * Retrieves all unpaid bills associated with a specific tenant by their ID.
     *
     * @param tenantId The ID of the tenant.
     * @return A list of unpaid Bill entities linked to the specified tenant.
     */
    List<Bill> findByTenantIdAndIsPaidFalse(Long tenantId);
    
}