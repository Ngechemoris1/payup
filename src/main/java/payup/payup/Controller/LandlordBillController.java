package payup.payup.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import payup.payup.dto.*;
import payup.payup.mapper.LandlordBillMapper;
import payup.payup.model.LandlordBill;
import payup.payup.service.LandlordBillService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing landlord bills.
 * Provides endpoints for creating, retrieving, and deleting bills with DTO support.
 * All endpoints have role-based access control for ADMIN and LANDLORD users.
 */
@RestController
@RequestMapping("/api/landlord-bills")
public class LandlordBillController {

    private static final Logger logger = LoggerFactory.getLogger(LandlordBillController.class);

    private final LandlordBillService landlordBillService;
    private final LandlordBillMapper landlordBillMapper;

    @Autowired
    public LandlordBillController(LandlordBillService landlordBillService,
                                  LandlordBillMapper landlordBillMapper) {
        this.landlordBillService = landlordBillService;
        this.landlordBillMapper = landlordBillMapper;
    }

    /**
     * Creates a new landlord bill.
     *
     * @param billDto The bill details to create
     * @return ResponseEntity containing the created LandlordBillDto with HTTP 201 (Created) status
     *         or ErrorResponseDto if creation fails
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createBill(@Valid @RequestBody LandlordBillDto billDto) {
        logger.info("Creating landlord bill for property {}", billDto.getPropertyId());
        try {
            LandlordBill bill = landlordBillMapper.toEntity(billDto);
            LandlordBill createdBill = landlordBillService.createLandlordBill(bill);
            LandlordBillDto createdBillDto = landlordBillMapper.toDto(createdBill);

            logger.info("Landlord bill created successfully with ID: {}", createdBill.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBillDto);

        } catch (Exception e) {
            logger.error("Failed to create landlord bill: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDto("Bill creation failed", e.getMessage()));
        }
    }

    /**
     * Retrieves all bills for a specific landlord.
     *
     * @param landlordId The ID of the landlord
     * @return ResponseEntity containing List of LandlordBillDto objects with HTTP 200 (OK) status
     *         or ErrorResponseDto if landlord not found
     */
    @GetMapping("/landlord/{landlordId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ResponseEntity<?> getBillsByLandlord(@PathVariable Long landlordId) {
        logger.info("Fetching bills for landlordId={}", landlordId);
        try {
            List<LandlordBill> bills = landlordBillService.getBillsByLandlord(landlordId);
            List<LandlordBillDto> billDtos = bills.stream()
                    .map(landlordBillMapper::toDto)
                    .collect(Collectors.toList());

            logger.info("Found {} bills for landlordId={}", billDtos.size(), landlordId);
            return ResponseEntity.ok(billDtos);

        } catch (Exception e) {
            logger.error("Failed to fetch bills for landlordId={}: {}", landlordId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto("Bills not found", e.getMessage()));
        }
    }

    /**
     * Retrieves all bills for a specific property.
     *
     * @param propertyId The ID of the property
     * @return ResponseEntity containing List of LandlordBillDto objects with HTTP 200 (OK) status
     *         or ErrorResponseDto if property not found
     */
    @GetMapping("/property/{propertyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getBillsByProperty(@PathVariable Long propertyId) {
        logger.info("Fetching bills for propertyId={}", propertyId);
        try {
            List<LandlordBill> bills = landlordBillService.getBillsByProperty(propertyId);
            List<LandlordBillDto> billDtos = bills.stream()
                    .map(landlordBillMapper::toDto)
                    .collect(Collectors.toList());

            logger.info("Found {} bills for propertyId={}", billDtos.size(), propertyId);
            return ResponseEntity.ok(billDtos);

        } catch (Exception e) {
            logger.error("Failed to fetch bills for propertyId={}: {}", propertyId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto("Bills not found", e.getMessage()));
        }
    }

    /**
     * Retrieves all bills for a specific landlord and property.
     *
     * @param landlordId The ID of the landlord
     * @param propertyId The ID of the property
     * @return ResponseEntity containing List of LandlordBillDto objects with HTTP 200 (OK) status
     *         or ErrorResponseDto if no bills found
     */
    @GetMapping("/landlord/{landlordId}/property/{propertyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ResponseEntity<?> getBillsByLandlordAndProperty(
            @PathVariable Long landlordId,
            @PathVariable Long propertyId) {

        logger.info("Fetching bills for landlordId={} and propertyId={}", landlordId, propertyId);
        try {
            List<LandlordBill> bills = landlordBillService.getBillsByLandlordAndProperty(landlordId, propertyId);
            List<LandlordBillDto> billDtos = bills.stream()
                    .map(landlordBillMapper::toDto)
                    .collect(Collectors.toList());

            logger.info("Found {} bills for landlordId={} and propertyId={}",
                    billDtos.size(), landlordId, propertyId);
            return ResponseEntity.ok(billDtos);

        } catch (Exception e) {
            logger.error("Failed to fetch bills: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto("Bills not found", e.getMessage()));
        }
    }

    /**
     * Deletes a landlord bill by its ID.
     *
     * @param id The ID of the bill to delete
     * @return ResponseEntity with HTTP 204 (No Content) on success or ErrorResponseDto on failure
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBill(@PathVariable Long id) {
        logger.info("Deleting bill with ID: {}", id);
        try {
            landlordBillService.deleteLandlordBill(id);
            logger.info("Successfully deleted bill with ID: {}", id);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            logger.error("Failed to delete bill with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto("Bill deletion failed", e.getMessage()));
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