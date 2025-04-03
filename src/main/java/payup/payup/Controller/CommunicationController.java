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
import payup.payup.service.CommunicationService;

/**
 * REST controller for managing communication operations.
 * Provides endpoints for sending emails and SMS messages.
 * All endpoints require ADMIN or LANDLORD role authorization.
 */
@RestController
@RequestMapping("/api/communication")
public class CommunicationController {

    private static final Logger logger = LoggerFactory.getLogger(CommunicationController.class);

    private final CommunicationService communicationService;

    @Autowired
    public CommunicationController(CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    /**
     * Sends an email to a specified recipient.
     *
     * @param emailRequest DTO containing email details (to, subject, text)
     * @return ResponseEntity with EmailResponseDto on success or ErrorResponseDto on failure
     */
    @PostMapping("/email")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ResponseEntity<?> sendEmail(@Valid @RequestBody EmailRequestDto emailRequest) {
        logger.info("Attempting to send email to: {}", emailRequest.getTo());

        try {
            communicationService.sendEmail(
                    emailRequest.getTo(),
                    emailRequest.getSubject(),
                    emailRequest.getText()
            );

            logger.info("Email sent successfully to: {}", emailRequest.getTo());
            return ResponseEntity.ok(
                    new EmailResponseDto(
                            "Email sent successfully",
                            emailRequest.getTo(),
                            emailRequest.getSubject()
                    )
            );

        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", emailRequest.getTo(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto(
                            "Email delivery failed",
                            e.getMessage()
                    ));
        }
    }

    /**
     * Sends an SMS to a specified phone number.
     *
     * @param smsRequest DTO containing SMS details (to, message)
     * @return ResponseEntity with SmsResponseDto on success or ErrorResponseDto on failure
     */
    @PostMapping("/sms")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ResponseEntity<?> sendSms(@Valid @RequestBody SmsRequestDto smsRequest) {
        logger.info("Attempting to send SMS to: {}", smsRequest.getTo());

        try {
            communicationService.sendSms(
                    smsRequest.getTo(),
                    smsRequest.getMessage()
            );

            logger.info("SMS sent successfully to: {}", smsRequest.getTo());
            return ResponseEntity.ok(
                    new SmsResponseDto(
                            "SMS sent successfully",
                            smsRequest.getTo()
                    )
            );

        } catch (Exception e) {
            logger.error("Failed to send SMS to {}: {}", smsRequest.getTo(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto(
                            "SMS delivery failed",
                            e.getMessage()
                    ));
        }
    }

    /**
     * DTO for email requests
     */
    public static class EmailRequestDto {
        private String to;
        private String subject;
        private String text;

        // Getters and setters
        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    /**
     * DTO for email responses
     */
    public static class EmailResponseDto {
        private final String message;
        private final String recipient;
        private final String subject;

        public EmailResponseDto(String message, String recipient, String subject) {
            this.message = message;
            this.recipient = recipient;
            this.subject = subject;
        }

        // Getters
        public String getMessage() {
            return message;
        }

        public String getRecipient() {
            return recipient;
        }

        public String getSubject() {
            return subject;
        }
    }

    /**
     * DTO for SMS requests
     */
    public static class SmsRequestDto {
        private String to;
        private String message;

        // Getters and setters
        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * DTO for SMS responses
     */
    public static class SmsResponseDto {
        private final String message;
        private final String recipient;

        public SmsResponseDto(String message, String recipient) {
            this.message = message;
            this.recipient = recipient;
        }

        // Getters
        public String getMessage() {
            return message;
        }

        public String getRecipient() {
            return recipient;
        }
    }

    /**
     * DTO for error responses
     */
    public static class ErrorResponseDto {
        private final String error;
        private final String details;

        public ErrorResponseDto(String error, String details) {
            this.error = error;
            this.details = details;
        }

        // Getters
        public String getError() {
            return error;
        }

        public String getDetails() {
            return details;
        }
    }
}