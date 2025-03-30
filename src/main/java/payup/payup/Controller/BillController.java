package payup.payup.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payup.payup.model.Bill;
import payup.payup.service.BillService;

import java.util.List;

/**
 * REST controller for managing bill-related operations. Provides endpoints for creating,
 * retrieving, and updating bills.
 */
@RestController
@RequestMapping("/api/bills")
public class BillController {

    private static final Logger logger = LoggerFactory.getLogger(BillController.class);

    @Autowired private BillService billService;

    /**
     * Creates a new bill for a specified tenant.
     *
     * @param tenantId The ID of the tenant for whom the bill is created.
     * @param bill     The bill details to create.
     * @return ResponseEntity containing the created Bill object with HTTP 201 (Created) status.
     */
    @PostMapping("/tenant/{tenantId}")
    public ResponseEntity<Bill> createBill(@PathVariable Long tenantId, @Valid @RequestBody Bill bill) {
        logger.info("Creating bill for tenantId={}", tenantId);
        Bill createdBill = billService.createBill(tenantId, bill);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBill);
    }

    /**
     * Retrieves all pending bills for a specified tenant.
     *
     * @param tenantId The ID of the tenant whose pending bills are retrieved.
     * @return ResponseEntity containing a List of pending Bill objects with HTTP 200 (OK) status.
     */
    @GetMapping("/tenant/{tenantId}/pending")
    public ResponseEntity<List<Bill>> getPendingBills(@PathVariable Long tenantId) {
        logger.info("Fetching pending bills for tenantId={}", tenantId);
        List<Bill> pendingBills = billService.getPendingBills(tenantId);
        return ResponseEntity.ok(pendingBills);
    }

    /**
     * Updates the status of a specified bill.
     *
     * @param billId The ID of the bill to update.
     * @param status The new status for the bill (e.g., PENDING, PAID, OVERDUE).
     * @return ResponseEntity containing the updated Bill object with HTTP 200 (OK) status.
     * @throws RuntimeException if the bill is not found.
     */
    @PutMapping("/{billId}/status")
    public ResponseEntity<Bill> updateBillStatus(@PathVariable Long billId, @RequestParam Bill.BillStatus status) {
        logger.info("Updating bill status: billId={}, status={}", billId, status);
        Bill updatedBill = billService.updateBillStatus(billId, status);
        return ResponseEntity.ok(updatedBill);
    }
}