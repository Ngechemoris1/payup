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

/**
 * Service for handling notifications in the PayUp system, supporting email and SMS channels.
 * Notifications can be sent from tenants to admins or from landlords to tenants.
 */
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
    private SmsService smsService; // New SMS service injection

    /**
     * Sends a notification from a tenant to an admin via email.
     *
     * @param notification The notification object containing the message and tenant info.
     * @return The saved notification object.
     * @throws IllegalArgumentException if the notification or its fields are invalid.
     * @throws RuntimeException         if no admin is found or email sending fails.
     */
    @Transactional
    public Notification sendNotificationToAdmin(Notification notification) {
        if (notification == null || notification.getMessage() == null || notification.getTenant() == null) {
            logger.error("Invalid notification: {}", notification);
            throw new IllegalArgumentException("Notification, message, and tenant must not be null");
        }

        User admin = userService.findAdmin()
                .orElseThrow(() -> {
                    logger.error("No admin user found");
                    return new RuntimeException("No admin user found");
                });

        logger.info("Sending notification from tenantId={} to admin: {} - Message: {}",
                notification.getTenant().getId(), admin.getEmail(), notification.getMessage());

        try {
            emailService.sendEmail(admin.getEmail(), "Tenant Notification", notification.getMessage());
        } catch (Exception e) {
            logger.error("Failed to send email to admin: {}", e.getMessage(), e);
            throw new RuntimeException("Email sending failed", e);
        }

        return notificationRepository.save(notification);
    }

    /**
     * Sends a notification from a landlord to a specific tenant via email and SMS.
     *
     * @param tenant  The tenant to notify.
     * @param message The message content.
     * @param sender  The landlord sending the notification.
     * @return The saved notification object.
     * @throws IllegalArgumentException if tenant, message, or sender is invalid.
     * @throws RuntimeException         if email or SMS sending fails.
     */
    @Transactional
    public Notification sendNotificationToTenant(Tenant tenant, String message, User sender) {
        if (tenant == null || message == null || message.trim().isEmpty() || sender == null || sender.getRole() != User.UserRole.LANDLORD) {
            logger.error("Invalid input: tenant={}, message={}, sender={}", tenant, message, sender);
            throw new IllegalArgumentException("Tenant, message, and valid landlord sender must not be null or empty");
        }

        Notification notification = new Notification(message, tenant);
        notification.setType(Notification.NotificationType.GENERAL); // Default type, can be overridden

        logger.info("Sending notification from landlordId={} to tenantId={}: {}", sender.getId(), tenant.getId(), message);

        try {
            emailService.sendEmail(tenant.getEmail(), "Landlord Notification", message);
            smsService.sendSms(tenant.getPhone(), message); // Send SMS to tenant's registered phone
        } catch (Exception e) {
            logger.error("Failed to send notification to tenantId={}: {}", tenant.getId(), e.getMessage(), e);
            throw new RuntimeException("Notification sending failed", e);
        }

        return notificationRepository.save(notification);
    }

    /**
     * Asynchronously sends a notification from a landlord to all tenants via email and SMS.
     *
     * @param message The message to broadcast.
     * @param sender  The landlord sending the notification.
     * @return A CompletableFuture indicating completion.
     * @throws IllegalArgumentException if message or sender is invalid.
     */
    @Async
    @Transactional
    public CompletableFuture<Void> sendNotificationToAllTenants(String message, User sender) {
        if (message == null || message.trim().isEmpty() || sender == null || sender.getRole() != User.UserRole.LANDLORD) {
            logger.error("Invalid input: message={}, sender={}", message, sender);
            throw new IllegalArgumentException("Message and valid landlord sender must not be null or empty");
        }

        List<Tenant> allTenants = tenantRepository.findAll();
        logger.info("Broadcasting notification from landlordId={} to {} tenants: {}", sender.getId(), allTenants.size(), message);

        for (Tenant tenant : allTenants) {
            try {
                sendNotificationToTenant(tenant, message, sender);
            } catch (Exception e) {
                logger.warn("Failed to notify tenantId={}: {}", tenant.getId(), e.getMessage());
                // Continue processing despite individual failures
            }
        }

        logger.info("Broadcast notification completed: {}", message);
        return CompletableFuture.completedFuture(null);
    }
}