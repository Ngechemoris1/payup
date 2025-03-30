package payup.payup.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import payup.payup.service.CommunicationService;

import java.util.Map;

/**
 * REST controller for managing communication operations. Provides endpoints for sending
 * emails and SMS messages, restricted to users with 'ADMIN' or 'LANDLORD' roles.
 */
@RestController
@RequestMapping("/api/communication")
public class CommunicationController {

    private static final Logger logger = LoggerFactory.getLogger(CommunicationController.class);

    @Autowired private CommunicationService communicationService;

    /**
     * Sends an email to a specified recipient.
     *
     * @param to      The recipient's email address.
     * @param subject The subject of the email.
     * @param text    The body of the email.
     * @return ResponseEntity with a success message and HTTP 200 (OK) status.
     */
    @PostMapping("/email")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ResponseEntity<Map<String, String>> sendEmail(@RequestParam String to, @RequestParam String subject, @RequestParam String text) {
        logger.info("Sending email to: {}", to);
        communicationService.sendEmail(to, subject, text);
        return ResponseEntity.ok(Map.of("message", "Email sent successfully to " + to));
    }

    /**
     * Sends an SMS to a specified phone number.
     *
     * @param to      The recipient's phone number.
     * @param message The SMS message content.
     * @return ResponseEntity with a success message and HTTP 200 (OK) status.
     */
    @PostMapping("/sms")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ResponseEntity<Map<String, String>> sendSms(@RequestParam String to, @RequestParam String message) {
        logger.info("Sending SMS to: {}", to);
        communicationService.sendSms(to, message);
        return ResponseEntity.ok(Map.of("message", "SMS sent successfully to " + to));
    }
}