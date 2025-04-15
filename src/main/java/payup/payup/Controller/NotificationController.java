package payup.payup.controller;

import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import payup.payup.model.*;
import payup.payup.service.*;
import payup.repository.TenantRepository;
import payup.repository.NotificationRepository;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;
    private final TenantRepository tenantRepository;
    private final UserService userService;
    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationController(NotificationService notificationService, TenantRepository tenantRepository,
                                  UserService userService, NotificationRepository notificationRepository) {
        this.notificationService = notificationService;
        this.tenantRepository = tenantRepository;
        this.userService = userService;
        this.notificationRepository = notificationRepository;
    }

    @PostMapping("/notify/tenant/{tenantId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ResponseEntity<?> notifyTenant(@PathVariable Long tenantId, @RequestBody @Valid NotificationRequestDto request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
        User sender = userService.getAuthenticatedUser();
        Notification notification = notificationService.sendNotificationToTenant(tenant, request.getMessage(), sender);
        logger.info("Notification sent to tenantId={} from {}: {}", tenantId, sender.getRole(), request.getMessage());
        return ResponseEntity.ok(new NotificationResponseDto(notification.getId(), notification.getMessage()));
    }

    @PostMapping("/notify/all-tenants")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ResponseEntity<?> notifyAllTenants(@RequestBody @Valid NotificationRequestDto request) {
        User sender = userService.getAuthenticatedUser();
        notificationService.sendNotificationToAllTenants(request.getMessage(), sender);
        logger.info("Broadcast notification sent from {}: {}", sender.getRole(), request.getMessage());
        return ResponseEntity.ok("Broadcast notification initiated");
    }

    @PostMapping("/notify/admin")
    @PreAuthorize("hasAnyRole('TENANT', 'LANDLORD')")
    public ResponseEntity<?> notifyAdmin(@RequestBody @Valid NotificationRequestDto request) {
        User sender = userService.getAuthenticatedUser();
        Notification notification;
        if (sender.getRole() == User.UserRole.TENANT) {
            Tenant tenant = tenantRepository.findByEmail(sender.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Tenant not found for user"));
            notification = new Notification(request.getMessage(), tenant);
        } else {
            notification = new Notification(request.getMessage(), null, sender);
        }
        notification.setType(Notification.NotificationType.GENERAL);
        Notification savedNotification = notificationService.sendNotificationToAdmin(notification);
        logger.info("Notification sent to admin from {}: {}", sender.getRole(), request.getMessage());
        return ResponseEntity.ok(new NotificationResponseDto(savedNotification.getId(), savedNotification.getMessage()));
    }

    @PostMapping("/notify/landlord/{landlordId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> notifyLandlord(@PathVariable Long landlordId, @RequestBody @Valid NotificationRequestDto request) {
        User landlord = userService.findById(landlordId)
                .filter(u -> u.getRole() == User.UserRole.LANDLORD)
                .orElseThrow(() -> new IllegalArgumentException("Landlord not found"));
        User sender = userService.getAuthenticatedUser();
        Notification notification = notificationService.sendNotificationToLandlord(landlord, request.getMessage(), sender);
        logger.info("Notification sent to landlordId={} from admin: {}", landlordId, request.getMessage());
        return ResponseEntity.ok(new NotificationResponseDto(notification.getId(), notification.getMessage()));
    }

    @GetMapping("/notify")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD', 'TENANT')")
    public ResponseEntity<?> getNotifications(Pageable pageable) {
        User user = userService.getAuthenticatedUser();
        Page<Notification> notifications;
        if (user.getRole() == User.UserRole.TENANT) {
            Tenant tenant = tenantRepository.findByEmail(user.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Tenant not found for user"));
            notifications = notificationRepository.findByTenantId(tenant.getId(), pageable);
        } else {
            notifications = notificationRepository.findByRecipientUserId(user.getId(), pageable);
        }
        logger.info("Retrieved notifications for userId={}, role={}", user.getId(), user.getRole());
        return ResponseEntity.ok(notifications.map(n -> new NotificationResponseDto(n.getId(), n.getMessage())));
    }

    public static class NotificationRequestDto {
        @NotBlank(message = "Message cannot be empty")
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class NotificationResponseDto {
        private final Long id;
        private final String message;

        public NotificationResponseDto(Long id, String message) {
            this.id = id;
            this.message = message;
        }

        public Long getId() { return id; }
        public String getMessage() { return message; }
    }
}