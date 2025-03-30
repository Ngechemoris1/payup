package payup.payup.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

/**
 * Service for sending SMS notifications using Twilio (or another provider).
 */
@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    public SmsService() {
    }

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken); // Runs after @Value injection
        logger.info("Twilio initialized with Account SID: {}", accountSid);
    }


    /**
     * Sends an SMS to the specified phone number.
     *
     * @param toPhoneNumber The recipient's phone number (e.g., "254712345678").
     * @param message       The SMS content.
     * @throws RuntimeException if SMS sending fails.
     */
    public void sendSms(String toPhoneNumber, String message) {
        try {
            Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    message
            ).create();
            logger.info("SMS sent to {}: {}", toPhoneNumber, message);
        } catch (Exception e) {
            logger.error("Failed to send SMS to {}: {}", toPhoneNumber, e.getMessage(), e);
            throw new RuntimeException("SMS sending failed", e);
        }
    }
}