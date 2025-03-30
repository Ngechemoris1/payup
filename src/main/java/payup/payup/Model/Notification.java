package payup.payup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a notification in the PayUp system. This class is an entity 
 * mapped to the "notifications" table in the database.
 */
@Entity
@Table(name = "notifications")
public class Notification {
    
    /**
     * The unique identifier for the notification.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The content of the notification message.
     */
    @Column(columnDefinition = "TEXT")
    private String message;

    /**
     * The timestamp when the notification was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * The tenant who is the target of this notification. 
     * This establishes a many-to-one relationship with the Tenant entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    /**
     * The status of the notification, indicating whether it has been read or not.
     */
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    /**
     * The type of the notification which can help categorize or filter notifications.
     */
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    // Constructors

    /**
     * Default constructor for JPA and other frameworks.
     */
    public Notification() {
    }

    /**
     * Constructor to create a new notification with a message and target tenant.
     * 
     * @param message The content of the notification.
     * @param tenant The tenant to whom the notification is directed.
     */
    public Notification(String message, Tenant tenant) {
        this.message = message;
        this.tenant = tenant;
        this.createdAt = LocalDateTime.now();
        this.status = NotificationStatus.UNREAD;
        this.type = NotificationType.GENERAL;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    // Custom methods

    /**
     * Marks the notification as read.
     */
    public void markAsRead() {
        this.status = NotificationStatus.READ;
    }

    // Enum for notification status
    public enum NotificationStatus {
        READ, UNREAD
    }

    // Enum for notification types
    public enum NotificationType {
        GENERAL, RENT_DUE, MAINTENANCE, URGENT
    }

    // Override toString for better logging and debugging
    @Override
    public String toString() {
        return "Notification{" +
               "id=" + id +
               ", message='" + message + '\'' +
               ", createdAt=" + createdAt +
               ", status=" + status +
               ", type=" + type +
               ", tenant=" + (tenant != null ? tenant.getId() : "null") +
               '}';
    }
}