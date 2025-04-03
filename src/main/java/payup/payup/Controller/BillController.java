package payup.payup.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payup.payup.dto.BillDto;
import payup.payup.mapper.BillMapper;
import payup.payup.model.Bill;
import payup.payup.service.BillService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing bill-related operations.
 * Provides endpoints for creating, retrieving, and updating bills.
 * All responses use BillDto for data transfer.
 */
@RestController
@RequestMapping("/api/bills")
public class BillController {

    private static final Logger logger = LoggerFactory.getLogger(BillController.class);

    private final BillService billService;
    private final BillMapper billMapper;

    @Autowired
    public BillController(BillService billService, BillMapper billMapper) {
        this.billService = billService;
        this.billMapper = billMapper;
    }

    /**
     * Creates a new bill for a specified tenant.
     *
     * @param tenantId The ID of the tenant for whom the bill is created
     * @param billDto  The bill details to create
     * @return ResponseEntity containing the created BillDto with HTTP 201 (Created) status
     *         or error message if operation fails
     */
    @PostMapping("/tenant/{tenantId}")
    public ResponseEntity<?> createBill(
            @PathVariable Long tenantId,
            @Valid @RequestBody BillDto billDto) {

        logger.info("Creating bill for tenantId={}", tenantId);
        try {
            Bill bill = billMapper.toEntity(billDto);
            Bill createdBill = billService.createBill(tenantId, bill);
            BillDto createdBillDto = billMapper.toDto(createdBill);

            logger.info("Bill created successfully for tenantId={}", tenantId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBillDto);

        } catch (Exception e) {
            logger.error("Failed to create bill for tenantId={}: {}", tenantId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDto("Bill creation failed", e.getMessage()));
        }
    }

    /**
     * Retrieves all pending bills for a specified tenant.
     *
     * @param tenantId The ID of the tenant whose pending bills are retrieved
     * @return ResponseEntity containing List of pending BillDto objects with HTTP 200 (OK) status
     *         or error message if tenant not found
     */
    @GetMapping("/tenant/{tenantId}/pending")
    public ResponseEntity<?> getPendingBills(@PathVariable Long tenantId) {
        logger.info("Fetching pending bills for tenantId={}", tenantId);
        try {
            List<Bill> pendingBills = billService.getPendingBills(tenantId);
            List<BillDto> pendingBillDtos = pendingBills.stream()
                    .map(billMapper::toDto)
                    .collect(Collectors.toList());

            logger.info("Found {} pending bills for tenantId={}", pendingBillDtos.size(), tenantId);
            return ResponseEntity.ok(pendingBillDtos);

        } catch (Exception e) {
            logger.error("Failed to fetch pending bills for tenantId={}: {}", tenantId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto("Pending bills not found", e.getMessage()));
        }
    }

    /**
     * Updates the status of a specified bill.
     *
     * @param billId The ID of the bill to update
     * @param status The new status for the bill (PENDING, PAID, or OVERDUE)
     * @return ResponseEntity containing the updated BillDto with HTTP 200 (OK) status
     *         or error message if bill not found
     */
    @PutMapping("/{billId}/status")
    public ResponseEntity<?> updateBillStatus(
            @PathVariable Long billId,
            @RequestParam Bill.BillStatus status) {

        logger.info("Updating bill status: billId={}, status={}", billId, status);
        try {
            Bill updatedBill = billService.updateBillStatus(billId, status);
            BillDto updatedBillDto = billMapper.toDto(updatedBill);

            logger.info("Bill status updated successfully for billId={}", billId);
            return ResponseEntity.ok(updatedBillDto);

        } catch (Exception e) {
            logger.error("Failed to update bill status for billId={}: {}", billId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto("Bill update failed", e.getMessage()));
        }
    }

    /**
     * Data transfer object for error responses.
     */
    private static class ErrorResponseDto {
        private final String error;
        private final String message;

        public ErrorResponseDto(String error, String message) {
            this.error = error;
            this.message = message;
        }

        public String getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }
    }
}