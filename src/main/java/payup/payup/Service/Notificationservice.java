package payup.payup.service;

import payup.payup.model.Notification;
import payup.payup.model.Tenant;
import payup.payup.model.User;
import payup.payup.repository.NotificationRepository;
import payup.payup.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class NotificationService {

    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserService userService;

    /**
     * Sends a notification from a tenant to an admin.
     * 
     * @param notification The notification object containing the message and tenant information.
     * @return The saved notification object for record-keeping.
     * @throws RuntimeException if no admin user is found in the system.
     */
    public Notification sendNotificationToAdmin(Notification notification) {
        User admin = userService.findAdmin().orElseThrow(() -> new RuntimeException("No admin user found"));
        LOGGER.info("Sending notification from tenant to admin: " + admin.getEmail() + " - Message: " + notification.getMessage());
        // Here you would implement the actual sending mechanism, e.g., email, SMS, or internal system notification
        // Example: EmailService.sendEmail(admin.getEmail(), "Tenant Notification", notification.getMessage());
        return notificationRepository.save(notification);
    }

    /**
     * Sends a notification from an admin to a specific tenant.
     * 
     * @param tenant The tenant object to notify.
     * @param message The message to send in the notification.
     * @return The saved notification object for record-keeping.
     */
    public Notification sendNotificationToTenant(Tenant tenant, String message) {
        Notification notification = new Notification(message, tenant);
        LOGGER.info("Sending notification from admin to tenant: " + tenant.getEmail() + " - Message: " + message);
        // Here you would implement the actual sending mechanism, e.g., email, SMS, or internal system notification
        // Example: EmailService.sendEmail(tenant.getEmail(), "Admin Notification", message);
        return notificationRepository.save(notification);
    }

    /**
     * Sends a notification from an admin to all tenants.
     * 
     * @param message The message to send to all tenants.
     * @note This method assumes that sending to all tenants is an immediate action. 
     *       In a production environment, consider using batch operations or asynchronous processing 
     *       for better performance with large numbers of tenants.
     */
    public void sendNotificationToAllTenants(String message) {
        List<Tenant> allTenants = tenantRepository.findAll();
        for (Tenant tenant : allTenants) {
            sendNotificationToTenant(tenant, message);
        }
        LOGGER.info("Notification sent to all tenants: " + message);
        //  might want to use a batch operation or an async task for this.
    }
}