package payup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import payup.payup.model.Payment;
import payup.payup.model.Tenant;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Payment entities in the PayUp system.
 * Provides CRUD operations and custom queries for retrieving payments based on tenant,
 * date ranges, and amounts.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Retrieves all payments made by a specific tenant.
     *
     * @param tenantId The ID of the tenant.
     * @return A list of Payment entities for the tenant.
     */
    List<Payment> findByTenantId(Long tenantId);

    /**
     * Calculates the total payment amount for a specific tenant.
     *
     * @param tenant The Tenant entity to query by.
     * @return The sum of payment amounts for the tenant, or null if none exist.
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.tenant = :tenant")
    Double sumPaymentsByTenant(Tenant tenant);

    /**
     * Finds a tenant by the M-Pesa CheckoutRequestID.
     *
     * @param checkoutRequestId The M-Pesa CheckoutRequestID.
     * @return An Optional containing the Tenant if found, or empty if not.
     */
//    Optional<Tenant> findByCheckoutRequestId(String checkoutRequestId);

    /**
     * Finds a payment by its transaction ID.
     *
     * @param transactionId The transaction ID (e.g., M-Pesa transaction code).
     * @return An Optional containing the Payment if found, or empty if not.
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Retrieves payments within a specific date range.
     *
     * @param start The start date and time.
     * @param end   The end date and time.
     * @return A list of Payment entities within the date range.
     */
    List<Payment> findByPaymentDateBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Retrieves payments made after a specific date.
     *
     * @param start The start date and time.
     * @return A list of Payment entities after the start date.
     */
    List<Payment> findByPaymentDateAfter(LocalDateTime start);

    /**
     * Retrieves payments made before a specific date.
     *
     * @param end The end date and time.
     * @return A list of Payment entities before the end date.
     */
    List<Payment> findByPaymentDateBefore(LocalDateTime end);

    /**
     * Retrieves payments for a tenant within a specific date range.
     *
     * @param start    The start date and time.
     * @param end      The end date and time.
     * @param tenantId The ID of the tenant.
     * @return A list of Payment entities matching the criteria.
     */
    List<Payment> findByPaymentDateBetweenAndTenantId(LocalDateTime start, LocalDateTime end, Long tenantId);

    /**
     * Retrieves payments for a tenant after a specific date with a minimum amount.
     *
     * @param start  The start date and time.
     * @param amount The minimum payment amount.
     * @return A list of Payment entities matching the criteria.
     */
    List<Payment> findByPaymentDateAfterAndAmountGreaterThanEqual(LocalDateTime start, Double amount);
}