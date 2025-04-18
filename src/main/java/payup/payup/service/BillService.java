package payup.payup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import payup.payup.model.Bill;
import payup.payup.model.Tenant;
import payup.repository.BillRepository;
import payup.repository.TenantRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Service for managing bill-related operations.
 */
@Service
public class BillService {

    private static final Logger logger = LoggerFactory.getLogger(BillService.class);

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private TenantRepository tenantRepository;

    /**
     * Creates a new bill for a specified tenant.
     *
     * @param tenantId The ID of the tenant for whom the bill is created.
     * @param bill The Bill object containing bill details (type, amount, due date, etc.).
     * @return The saved Bill entity after persistence in the database.
     * @throws IllegalArgumentException if bill details are invalid.
     */
    @Transactional
    public Bill createBill(Long tenantId, Bill bill) {
        // Validate bill details
        if (bill == null || bill.getBillType() == null || bill.getAmount() <= 0 || bill.getDueDate() == null) {
            logger.error("Invalid bill details: {}", bill);
            throw new IllegalArgumentException("Bill type, amount, and due date must be valid");
        }

        // Fetch tenant by ID
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> {
                    logger.error("Tenant not found: tenantId={}", tenantId);
                    return new IllegalArgumentException("Tenant not found with ID: " + tenantId);
                });

        // Set tenant and default values
        bill.setTenant(tenant.getUser());
        bill.setPaid(false);
        bill.setStatus(Bill.BillStatus.PENDING);

        // Save and log
        Bill savedBill = billRepository.save(bill);
        logger.info("Bill created: billId={}, tenantId={}", savedBill.getId(), tenantId);
        return savedBill;
    }

    /**
     * Retrieves all bills for a specific tenant by their ID.
     *
     * @param tenantId The ID of the tenant.
     * @return A list of bills associated with the tenant.
     * @throws IllegalArgumentException if tenantId is null.
     */
    @Transactional(readOnly = true)
    public List<Bill> findByTenantId(Long tenantId) {
        // Validate tenantId
        if (tenantId == null) {
            logger.error("Tenant ID cannot be null");
            throw new IllegalArgumentException("Tenant ID must not be null");
        }
        // Fetch bills
        List<Bill> bills = billRepository.findByTenantId(tenantId);
        logger.debug("Retrieved {} bills for tenantId={}", bills.size(), tenantId);
        return bills;
    }

    /**
     * Retrieves all pending bills for a specific tenant.
     *
     * @param tenantId The ID of the tenant.
     * @return A list of unpaid bills associated with the tenant.
     * @throws IllegalArgumentException if tenantId is null.
     */
    @Transactional(readOnly = true)
    public List<Bill> getPendingBills(Long tenantId) {
        // Validate tenantId
        if (tenantId == null) {
            logger.error("Tenant ID cannot be null");
            throw new IllegalArgumentException("Tenant ID must not be null");
        }
        // Fetch pending bills
        List<Bill> pendingBills = billRepository.findByTenantIdAndIsPaidFalse(tenantId);
        logger.debug("Retrieved {} pending bills for tenantId={}", pendingBills.size(), tenantId);
        return pendingBills;
    }

    /**
     * Updates the status of a bill identified by its ID.
     *
     * @param billId The ID of the bill to update.
     * @param status The new BillStatus (PENDING, PAID, OVERDUE) to set.
     * @return The updated Bill entity after saving changes to the database.
     * @throws IllegalArgumentException if billId or status is null.
     * @throws RuntimeException if the bill is not found.
     */
    @Transactional
    public Bill updateBillStatus(Long billId, Bill.BillStatus status) {
        // Validate inputs
        if (billId == null || status == null) {
            logger.error("Bill ID or status cannot be null: billId={}, status={}", billId, status);
            throw new IllegalArgumentException("Bill ID and status must not be null");
        }

        // Fetch bill
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> {
                    logger.error("Bill not found: billId={}", billId);
                    return new RuntimeException("Bill not found with ID: " + billId);
                });

        // Update status and paid flag
        bill.setStatus(status);
        bill.setPaid(status == Bill.BillStatus.PAID);
        if (bill.isOverdue()) {
            bill.setStatus(Bill.BillStatus.OVERDUE);
        }

        // Save and log
        Bill updatedBill = billRepository.save(bill);
        logger.info("Bill status updated: billId={}, newStatus={}", billId, status);
        return updatedBill;
    }
}