package payup.payup.mapper;

import org.springframework.stereotype.Component;
import payup.payup.dto.NotificationDto;
import payup.payup.model.Notification;

@Component
public class NotificationMapper {

    public NotificationDto toDto(Notification notification) {
        if (notification == null) {
            return null;
        }

        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setStatus(notification.getStatus().name());
        dto.setType(notification.getType().name());

        if (notification.getTenant() != null) {
            dto.setTenantId(notification.getTenant().getId());
        }
        return dto;
    }

    public Notification toEntity(NotificationDto dto) {
        if (dto == null) {
            return null;
        }

        Notification notification = new Notification();
        notification.setId(dto.getId());
        notification.setMessage(dto.getMessage());
        notification.setCreatedAt(dto.getCreatedAt());
        if (dto.getStatus() != null) {
            notification.setStatus(Notification.NotificationStatus.valueOf(dto.getStatus()));
        }
        if (dto.getType() != null) {
            notification.setType(Notification.NotificationType.valueOf(dto.getType()));
        }
        // Tenant should be set separately via service
        return notification;
    }
}
