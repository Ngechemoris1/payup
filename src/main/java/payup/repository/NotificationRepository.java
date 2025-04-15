package payup.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import payup.payup.model.Notification;

import java.util.List;

/**
 * Repository interface for managing Notification entities in the PayUp system.
 * Provides CRUD operations and custom queries for retrieving and updating notifications
 * based on tenant, status, and type.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByTenantId(Long tenantId, Pageable pageable);
    List<Notification> findByTenantIdAndStatus(Long tenantId, Notification.NotificationStatus status);
    Page<Notification> findByRecipientUserId(Long userId, Pageable pageable);

    /**
     * Retrieves all notifications sent to a specific tenant.
     *
     * @param tenantId The ID of the tenant.
     * @return A list of Notification entities for the tenant.
     */
    List<Notification> findByTenantId(Long tenantId);

    /**
     * Retrieves all notifications for a tenant with a specific status, with pagination.
     *
     * @param tenantId The ID of the tenant.
     * @param status   The NotificationStatus to filter by (e.g., READ, UNREAD).
     * @param pageable Pagination and sorting information.
     * @return A Page of Notification entities matching the tenant and status.
     */
    Page<Notification> findByTenantIdAndStatus(Long tenantId, Notification.NotificationStatus status, Pageable pageable);

    /**
     * Retrieves all notifications of a specific type for a tenant, with pagination.
     *
     * @param tenantId The ID of the tenant.
     * @param type     The NotificationType to filter by.
     * @param pageable Pagination and sorting information.
     * @return A Page of Notification entities matching the tenant and type.
     */
    Page<Notification> findByTenantIdAndType(Long tenantId, Notification.NotificationType type, Pageable pageable);

    /**
     * Updates the status of all unread notifications for a tenant to READ.
     *
     * @param tenantId The ID of the tenant whose notifications should be updated.
     * @return The number of notifications updated.
     */
    @Query("UPDATE Notification n SET n.status = 'READ' WHERE n.tenant.id = :tenantId AND n.status = 'UNREAD'")
    int markAllAsReadForTenant(@Param("tenantId") Long tenantId);
}