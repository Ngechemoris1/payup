package payup.payup.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import payup.payup.model.Tenant;
import payup.payup.service.MpesaService;
import payup.repository.TenantRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for managing payment operations in the PayUp system. Provides endpoints
 * for initiating M-Pesa STK Push payments and handling callbacks from Safaricom. The payment
 * initiation endpoint is secured for authenticated tenants, while the callback endpoint is
 * public to allow Safaricom's servers to communicate with it.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private MpesaService mpesaService;

    @Autowired
    private TenantRepository tenantRepository;

    /**
     * Data Transfer Object (DTO) for M-Pesa payment initiation requests.
     */
    @Data
    static class MpesaPaymentRequest {
        @NotNull(message = "Amount cannot be null")
        @Positive(message = "Amount must be greater than zero")
        private Double amount;

        private Long billId; // Optional: Links payment to a specific bill
    }

    /**
     * Initiates an M-Pesa STK Push payment for the authenticated tenant.
     * <p>
     * This endpoint requires tenant authentication and validates the payment amount and tenant's
     * phone number. It uses the MpesaService to initiate the payment and returns a CheckoutRequestID
     * upon success. The optional billId can link the payment to a specific bill.
     * </p>
     *
     * @param request     The payment request containing the amount and optional bill ID.
     * @param userDetails The authenticated tenant's details from Spring Security.
     * @return ResponseEntity with a success message and CheckoutRequestID, or an error message.
     * @throws IOException If payment initiation fails due to network or API issues.
     */
    @PostMapping("/mpesa")
    public ResponseEntity<Map<String, String>> initiateMpesaPayment(
            @Valid @RequestBody MpesaPaymentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Initiating M-Pesa payment for user: {}", userDetails.getUsername());

        try {
            // Fetch tenant by email (assuming email matches username in UserDetails)
            Tenant tenant = tenantRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> {
                        logger.error("Tenant not found for email: {}", userDetails.getUsername());
                        return new RuntimeException("Tenant not found");
                    });

            // Validate phone number (Kenyan M-Pesa format: 2547XXXXXXXX or 2541XXXXXXXX)
            String phoneNumber = tenant.getPhone();
            if (phoneNumber == null || !phoneNumber.matches("^254[17][0-9]{8}$")) {
                logger.warn("Invalid or missing phone number for tenant: id={}, phone={}", tenant.getId(), phoneNumber);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid or missing phone number (must be in 2547XXXXXXXX or 2541XXXXXXXX format)"));
            }

            // Initiate payment via MpesaService
            String checkoutRequestId = mpesaService.initiatePayment(
                    tenant.getId(),
                    request.getAmount(),
                    phoneNumber
            );
            logger.info("Payment initiated successfully for tenant: id={}, CheckoutRequestID={}", tenant.getId(), checkoutRequestId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Payment initiated successfully");
            response.put("checkoutRequestId", checkoutRequestId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid payment request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Payment initiation failed: " + e.getMessage()));
        } catch (RuntimeException e) {
            logger.warn("Tenant or payment error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            logger.error("Failed to initiate payment due to IO error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Payment initiation failed due to network error: " + e.getMessage()));
        }
    }

    /**
     * Handles M-Pesa callback from Safaricom to process payment results.
     * <p>
     * This endpoint is publicly accessible to allow Safaricom's servers to send callback data.
     * It processes the payment result asynchronously via the MpesaService and logs the outcome.
     * The callback data is expected to be in a Map format as provided by Safaricom's API.
     * </p>
     *
     * @param callbackData The callback payload from Safaricom containing payment result details.
     * @return ResponseEntity with a success message and HTTP 200 (OK) status, or an error message if processing fails.
     */
    @PostMapping("/mpesa/callback")
    public ResponseEntity<Map<String, String>> handleMpesaCallback(@RequestBody Map<String, Object> callbackData) {
        logger.info("Received M-Pesa callback: {}", callbackData);

        try {
            mpesaService.handleCallback(callbackData);
            logger.debug("M-Pesa callback processed successfully");
            return ResponseEntity.ok(Map.of("message", "Callback processed successfully"));
        } catch (Exception e) {
            logger.error("Failed to process M-Pesa callback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Callback processing failed: " + e.getMessage()));
        }
    }
}