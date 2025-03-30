package payup.payup.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import payup.payup.model.LandlordBill;
import payup.payup.service.LandlordBillService;

import java.util.List;

/**
 * REST controller for managing landlord bills. Provides endpoints for creating, retrieving,
 * and deleting bills, with role-based access control for 'ADMIN' and 'LANDLORD' users.
 */
@RestController
@RequestMapping("/api/landlord-bills")
public class LandlordBillController {

    private static final Logger logger = LoggerFactory.getLogger(LandlordBillController.class);

    @Autowired private LandlordBillService landlordBillService;

    /**
     * Creates a new landlord bill.
     *
     * @param landlordBill The bill details to create.
     * @return ResponseEntity containing the created LandlordBill object with HTTP 201 (Created) status.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LandlordBill> createBill(@Valid @RequestBody LandlordBill landlordBill) {
        logger.info("Creating landlord bill");
        LandlordBill createdBill = landlordBillService.createLandlordBill(landlordBill);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBill);
    }

    /**
     * Retrieves all bills for a specific landlord.
     *
     * @param landlordId The ID of the landlord.
     * @return ResponseEntity containing a List of LandlordBill objects with HTTP 200 (OK) status.
     */
    @GetMapping("/landlord/{landlordId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ResponseEntity<List<LandlordBill>> getBillsByLandlord(@PathVariable Long landlordId) {
        logger.info("Fetching bills for landlordId={}", landlordId);
        List<LandlordBill> bills = landlordBillService.getBillsByLandlord(landlordId);
        return ResponseEntity.ok(bills);
    }

    /**
     * Retrieves all bills for a specific property.
     *
     * @param propertyId The ID of the property.
     * @return ResponseEntity containing a List of LandlordBill objects with HTTP 200 (OK) status.
     */
    @GetMapping("/property/{propertyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LandlordBill>> getBillsByProperty(@PathVariable Long propertyId) {
        logger.info("Fetching bills for propertyId={}", propertyId);
        List<LandlordBill> bills = landlordBillService.getBillsByProperty(propertyId);
        return ResponseEntity.ok(bills);
    }

    /**
     * Retrieves all bills for a specific landlord and property.
     *
     * @param landlordId The ID of the landlord.
     * @param propertyId The ID of the property.
     * @return ResponseEntity containing a List of LandlordBill objects with HTTP 200 (OK) status.
     */
    @GetMapping("/landlord/{landlordId}/property/{propertyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ResponseEntity<List<LandlordBill>> getBillsByLandlordAndProperty(@PathVariable Long landlordId, @PathVariable Long propertyId) {
        logger.info("Fetching bills for landlordId={} and propertyId={}", landlordId, propertyId);
        List<LandlordBill> bills = landlordBillService.getBillsByLandlordAndProperty(landlordId, propertyId);
        return ResponseEntity.ok(bills);
    }

    /**
     * Deletes a landlord bill by its ID.
     *
     * @param id The ID of the bill to delete.
     * @return ResponseEntity with HTTP 204 (No Content) status upon successful deletion.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBill(@PathVariable Long id) {
        logger.info("Deleting bill: id={}", id);
        landlordBillService.deleteLandlordBill(id);
        return ResponseEntity.noContent().build();
    }
}