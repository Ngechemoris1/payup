package payup.payup.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
 * Handles creation, retrieval, and status updates of bills.
 * All endpoints return responses in JSON format using BillDto.
 */
@RestController
@RequestMapping("/api/bills")
public class BillController {

    private static final Logger logger = LoggerFactory.getLogger(BillController.class);

    private final BillService billService;
    private final BillMapper billMapper;

    /**
     * Constructor for dependency injection.
     *
     * @param billService The service for bill operations.
     * @param billMapper  The mapper for converting between Bill and BillDto.
     */
    @Autowired
    public BillController(BillService billService, BillMapper billMapper) {
        this.billService = billService;
        this.billMapper = billMapper;
    }

    /**
     * Creates a new bill for a specified tenant.
     *
     * @param tenantId The ID of the tenant for whom the bill is created.
     * @param billDto  The bill details (type, amount, due date, etc.) in DTO format.
     * @return ResponseEntity with the created BillDto and HTTP 201 status,
     *         or HTTP 400 with an error message if creation fails.
     */
    @PostMapping(value = "/tenant/{tenantId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createBill(@PathVariable Long tenantId, @Valid @RequestBody BillDto billDto) {
        // Log the attempt to create a bill
        logger.info("Creating bill for tenantId={}", tenantId);
        try {
            // Convert DTO to entity
            Bill bill = billMapper.toEntity(billDto);
            // Create bill using service
            Bill createdBill = billService.createBill(tenantId, bill);
            // Convert back to DTO for response
            BillDto createdBillDto = billMapper.toDto(createdBill);
            // Log success
            logger.info("Bill created successfully for tenantId={}", tenantId);
            // Return created bill with 201 status
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBillDto);
        } catch (Exception e) {
            // Log error details
            logger.error("Failed to create bill for tenantId={}: {}", tenantId, e.getMessage());
            // Return error response with 400 status
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDto("Bill creation failed", e.getMessage()));
        }
    }

    /**
     * Retrieves all pending bills for a specified tenant.
     *
     * @param tenantId The ID of the tenant whose pending bills are retrieved.
     * @return ResponseEntity with a list of pending BillDto objects and HTTP 200 status,
     *         or HTTP 404 with an error message if retrieval fails.
     */
    @GetMapping("/tenant/{tenantId}/pending")
    public ResponseEntity<?> getPendingBills(@PathVariable Long tenantId) {
        // Log the attempt to fetch pending bills
        logger.info("Fetching pending bills for tenantId={}", tenantId);
        try {
            // Retrieve pending bills from service
            List<Bill> pendingBills = billService.getPendingBills(tenantId);
            // Convert to DTOs
            List<BillDto> pendingBillDtos = pendingBills.stream()
                    .map(billMapper::toDto)
                    .collect(Collectors.toList());
            // Log the number of bills found
            logger.info("Found {} pending bills for tenantId={}", pendingBillDtos.size(), tenantId);
            // Return list with 200 status
            return ResponseEntity.ok(pendingBillDtos);
        } catch (Exception e) {
            // Log error details
            logger.error("Failed to fetch pending bills for tenantId={}: {}", tenantId, e.getMessage());
            // Return error response with 404 status
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto("Pending bills not found", e.getMessage()));
        }
    }

    /**
     * Updates the status of a specified bill.
     *
     * @param billId The ID of the bill to update.
     * @param status The new status (PENDING, PAID, OVERDUE) for the bill.
     * @return ResponseEntity with the updated BillDto and HTTP 200 status,
     *         or HTTP 404 with an error message if update fails.
     */
    @PutMapping("/{billId}/status")
    public ResponseEntity<?> updateBillStatus(@PathVariable Long billId, @RequestParam Bill.BillStatus status) {
        // Log the attempt to update bill status
        logger.info("Updating bill status: billId={}, status={}", billId, status);
        try {
            // Update bill status via service
            Bill updatedBill = billService.updateBillStatus(billId, status);
            // Convert to DTO for response
            BillDto updatedBillDto = billMapper.toDto(updatedBill);
            // Log success
            logger.info("Bill status updated successfully for billId={}", billId);
            // Return updated bill with 200 status
            return ResponseEntity.ok(updatedBillDto);
        } catch (Exception e) {
            // Log error details
            logger.error("Failed to update bill status for billId={}: {}", billId, e.getMessage());
            // Return error response with 404 status
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto("Bill update failed", e.getMessage()));
        }
    }

    /**
     * DTO for error responses, encapsulating error type and message.
     */
    private static class ErrorResponseDto {
        private final String error;
        private final String message;

        /**
         * Constructs an error response.
         *
         * @param error   The error type (e.g., "Bill creation failed").
         * @param message The detailed error message.
         */
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