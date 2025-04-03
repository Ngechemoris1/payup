package payup.payup.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import payup.payup.dto.*;
import payup.payup.model.Tenant;
import payup.payup.service.MpesaService;
import payup.repository.TenantRepository;

import java.io.IOException;

/**
 * REST controller for managing payment operations in the PayUp system.
 * Provides endpoints for initiating M-Pesa STK Push payments and handling callbacks.
 * Payment initiation is secured for authenticated tenants, while callbacks are public.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final MpesaService mpesaService;
    private final TenantRepository tenantRepository;

    @Autowired
    public PaymentController(MpesaService mpesaService, TenantRepository tenantRepository) {
        this.mpesaService = mpesaService;
        this.tenantRepository = tenantRepository;
    }

    /**
     * Initiates an M-Pesa STK Push payment for the authenticated tenant.
     *
     * @param request Payment request containing amount and optional bill ID
     * @param userDetails Authenticated tenant details
     * @return ResponseEntity with PaymentResponseDto or ErrorResponseDto
     */
    @PostMapping("/mpesa")
    public ResponseEntity<?> initiateMpesaPayment(
            @Valid @RequestBody MpesaPaymentRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {

        logger.info("Initiating M-Pesa payment for user: {}", userDetails.getUsername());

        try {
            Tenant tenant = tenantRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new TenantNotFoundException(userDetails.getUsername()));

            if (!PhoneNumberValidator.isValidMpesaNumber(tenant.getPhone())) {
                logger.warn("Invalid phone number format for tenant: {}", tenant.getId());
                return ResponseEntity.badRequest()
                        .body(new ErrorResponseDto(
                                "Invalid phone number",
                                "Phone must be in 2547XXXXXXXX or 2541XXXXXXXX format"
                        ));
            }

            PaymentResponseDto response = mpesaService.initiatePayment(
                    tenant.getId(),
                    request.getAmount(),
                    tenant.getPhone(),
                    request.getBillId()
            );

            logger.info("Payment initiated successfully for tenant: {}", tenant.getId());
            return ResponseEntity.ok(response);

        } catch (TenantNotFoundException e) {
            logger.error("Tenant not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto("Tenant not found", e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid payment request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseDto("Invalid request", e.getMessage()));
        } catch (IOException e) {
            logger.error("Payment initiation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto(
                            "Payment initiation failed",
                            "Network error occurred"
                    ));
        }
    }

    /**
     * Handles M-Pesa callback from Safaricom to process payment results.
     *
     * @param callbackData Callback payload from Safaricom
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/mpesa/callback")
    public ResponseEntity<BasicResponseDto> handleMpesaCallback(
            @RequestBody MpesaCallbackDto callbackData) {

        logger.info("Received M-Pesa callback for transaction: {}",
                callbackData.getTransactionId());

        try {
            mpesaService.handleCallback(callbackData);
            logger.debug("Callback processed successfully");
            return ResponseEntity.ok(new BasicResponseDto("Callback processed successfully"));
        } catch (Exception e) {
            logger.error("Callback processing failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BasicResponseDto("Callback processing failed"));
        }
    }

    /**
     * Custom exception for tenant not found scenarios
     */
    private static class TenantNotFoundException extends RuntimeException {
        public TenantNotFoundException(String email) {
            super("Tenant not found with email: " + email);
        }
    }

    /**
     * Utility class for phone number validation
     */
    private static class PhoneNumberValidator {
        public static boolean isValidMpesaNumber(String phoneNumber) {
            return phoneNumber != null && phoneNumber.matches("^254[17][0-9]{8}$");
        }
    }
}