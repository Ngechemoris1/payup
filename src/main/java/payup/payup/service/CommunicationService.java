package payup.payup.service;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import payup.payup.model.Communication;
import payup.payup.model.User;
import payup.repository.CommunicationRepository;
import payup.repository.UserRepository;

@Service
public class CommunicationService {

    private static final Logger logger = LoggerFactory.getLogger(CommunicationService.class);

    @Autowired
    private JavaMailSender javaMailSender;

    // Send an email to a tenant
    public void sendEmail(String to, String subject, String text) {
        if (to == null || subject == null || text == null) throw new IllegalArgumentException("Email fields must not be null");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }

    @Autowired
    private SmsService smsService; // Injects SmsService for SMS operations

    // Send an SMS to a tenant (requires integration with an SMS API like Twilio)
    public void sendSms(String to, String message) {
        smsService.sendSms(to, message);
        System.out.println("Sending SMS to " + to + ": " + message);
    }
    @Autowired
    private CommunicationRepository messageRepository; // Injects MessageRepository for message operations

    @Autowired
    private UserRepository userRepository; // Injects UserRepository to validate sender/receiver

    // Sends a message from one user to another
    public Communication sendMessage(Long senderId, Long receiverId, Communication message) {

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        // Set sender, receiver, and timestamp
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setSentAt(LocalDateTime.now());
        // Save the message (SMS/Email delivery would be handled externally)
        return messageRepository.save(message);
    }

    // Retrieves all messages received by a user
    public List<Communication> getMessagesForUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Query messages where the user is the receiver
        return messageRepository.findByReceiver(user);
    }
}