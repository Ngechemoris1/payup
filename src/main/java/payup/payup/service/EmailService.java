package payup.payup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.MailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender javaMailSender;

    /**
     * Sends a simple email.
     *
     * @param to      The recipient's email address.
     * @param subject The subject of the email.
     * @param text    The body of the email.
     */
    public void sendEmail(String to, String subject, String text) {
        if (to == null || subject == null || text == null) {
            logger.error("Invalid email parameters: to={}, subject={}, text={}", to, subject, text);
            throw new IllegalArgumentException("Email parameters must not be null");
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            javaMailSender.send(message);
            logger.info("Email sent to: {}", to);
        } catch (MailException e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }
}