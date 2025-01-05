package payup.payup.repository;

import payup.payup.model.Notification;
import payup.payup.model.Notification.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for performing CRUD operations on Notification entities.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Finds all notifications sent to a specific tenant.
     * 
     * @param tenantId The ID of the tenant to fetch notifications for.
     * @return A list of notifications for the tenant.
     */
    List<Notification> findByTenantId(Long tenantId);

    /**
     * Retrieves all unread notifications for a tenant with pagination.
     * 
     * @param tenantId The ID of the tenant.
     * @param pageable Pagination information.
     * @return A Page of unread notifications for the tenant.
     */
    Page<Notification> findByTenantIdAndStatus(Long tenantId, NotificationStatus status, Pageable pageable);

    /**
     * Finds notifications by type for a specific tenant with pagination.
     * 
     * @param tenantId The ID of the tenant.
     * @param type The type of notification to search for.
     * @param pageable Pagination information.
     * @return A Page of notifications of the specified type for the tenant.
     */
    Page<Notification> findByTenantIdAndType(Long tenantId, Notification.NotificationType type, Pageable pageable);

    /**
     * Marks all notifications as read for a tenant.
     * 
     * @param tenantId The ID of the tenant whose notifications should be marked as read.
     * @return The number of affected rows.
     */
    @Query("UPDATE Notification n SET n.status = :#{T(com.payup.model.Notification.NotificationStatus).READ} WHERE n.tenant.id = :tenantId AND n.status = :#{T(com.payup.model.Notification.NotificationStatus).UNREAD}")
    int markAllAsReadForTenant(@Param("tenantId") Long tenantId);
}