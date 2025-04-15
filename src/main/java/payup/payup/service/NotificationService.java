package payup.payup.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import payup.payup.model.Notification;
import payup.payup.model.Tenant;
import payup.payup.model.User;
import payup.repository.NotificationRepository;
import payup.repository.TenantRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    @Transactional
    public Notification sendNotificationToAdmin(Notification notification) {
        if (notification == null || notification.getMessage() == null) {
            logger.error("Invalid notification: {}", notification);
            throw new IllegalArgumentException("Notification and message must not be null");
        }

        User admin = userService.findAdmin()
                .orElseThrow(() -> {
                    logger.error("No admin user found");
                    return new RuntimeException("No admin user found");
                });

        notification.setRecipientUser(admin);
        if (notification.getSenderUser() == null && notification.getTenant() != null) {
            notification.setSenderUser(notification.getTenant().getUser());
        }

        logger.info("Sending notification to admin: {} - Message: {}", admin.getEmail(), notification.getMessage());

        try {
            emailService.sendEmail(admin.getEmail(), "Notification", notification.getMessage());
        } catch (Exception e) {
            logger.error("Failed to send email to admin: {}", e.getMessage(), e);
            throw new RuntimeException("Email sending failed", e);
        }

        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification sendNotificationToTenant(Tenant tenant, String message, User sender) {
        if (tenant == null || message == null || message.trim().isEmpty() || sender == null) {
            logger.error("Invalid input: tenant={}, message={}, sender={}", tenant, message, sender);
            throw new IllegalArgumentException("Tenant, message, and sender must not be null or empty");
        }

        Notification notification = new Notification(message, tenant);
        notification.setSenderUser(sender);
        notification.setRecipientUser(tenant.getUser());
        notification.setType(Notification.NotificationType.GENERAL);

        logger.info("Sending notification from userId={} to tenantId={}: {}", sender.getId(), tenant.getId(), message);

        try {
            emailService.sendEmail(tenant.getEmail(), "Notification", message);
            smsService.sendSms(tenant.getPhone(), message);
        } catch (Exception e) {
            logger.error("Failed to send notification to tenantId={}: {}", tenant.getId(), e.getMessage(), e);
            throw new RuntimeException("Notification sending failed", e);
        }

        return notificationRepository.save(notification);
    }

    @Async
    @Transactional
    public CompletableFuture<Void> sendNotificationToAllTenants(String message, User sender) {
        if (message == null || message.trim().isEmpty() || sender == null) {
            logger.error("Invalid input: message={}, sender={}", message, sender);
            throw new IllegalArgumentException("Message and sender must not be null or empty");
        }

        List<Tenant> allTenants = tenantRepository.findAll();
        logger.info("Broadcasting notification from userId={} to {} tenants: {}", sender.getId(), allTenants.size(), message);

        for (Tenant tenant : allTenants) {
            try {
                sendNotificationToTenant(tenant, message, sender);
            } catch (Exception e) {
                logger.warn("Failed to notify tenantId={}: {}", tenant.getId(), e.getMessage());
            }
        }

        logger.info("Broadcast notification completed: {}", message);
        return CompletableFuture.completedFuture(null);
    }

    @Transactional
    public Notification sendNotificationToLandlord(User landlord, String message, User sender) {
        if (landlord == null || message == null || message.trim().isEmpty() || sender == null) {
            logger.error("Invalid input: landlord={}, message={}, sender={}", landlord, message, sender);
            throw new IllegalArgumentException("Landlord, message, and sender must not be null or empty");
        }
        if (landlord.getRole() != User.UserRole.LANDLORD) {
            logger.error("Recipient is not a landlord: role={}", landlord.getRole());
            throw new IllegalArgumentException("Recipient must be a landlord");
        }
        if (sender.getRole() != User.UserRole.ADMIN) {
            logger.error("Sender is not an admin: role={}", sender.getRole());
            throw new IllegalArgumentException("Sender must be an admin");
        }

        Notification notification = new Notification(message, landlord, sender);
        notification.setType(Notification.NotificationType.GENERAL);

        logger.info("Sending notification from adminId={} to landlordId={}: {}", sender.getId(), landlord.getId(), message);

        try {
            emailService.sendEmail(landlord.getEmail(), "Admin Notification", message);
        } catch (Exception e) {
            logger.error("Failed to send notification to landlordId={}: {}", landlord.getId(), e.getMessage(), e);
            throw new RuntimeException("Email sending failed", e);
        }

        return notificationRepository.save(notification);
    }
}