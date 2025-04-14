package payup.payup.controller;

import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import payup.payup.dto.*;
import payup.payup.exception.UserNotFoundException;
import payup.payup.mapper.*;
import payup.payup.model.*;
import payup.payup.service.*;
import payup.repository.TenantRepository;


    @RestController
    @RequestMapping("/api/notifications")
    public class NotificationController {
        private final NotificationService notificationService;
        private final TenantRepository tenantRepository;
        private final UserService userService;
        private final TenantService tenantService;

        @Autowired
        public NotificationController(NotificationService notificationService, TenantRepository tenantRepository, UserService userService, TenantService tenantService) {
            this.notificationService = notificationService;
            this.tenantRepository = tenantRepository;
            this.userService = userService;
            this.tenantService = tenantService;
        }

        @PostMapping("/tenant/{tenantId}")
        @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
        public ResponseEntity<?> sendToTenant(@PathVariable Long tenantId, @RequestBody NotificationRequestDto request) {
            Tenant tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
            User sender = tenantService.getTenantById(tenantId).getUser();
            Notification notification = notificationService.sendNotificationToTenant(tenant, request.getMessage(), sender);
            return ResponseEntity.ok(new NotificationResponseDto(notification.getId(), notification.getMessage()));
        }

        public static class NotificationRequestDto {
            private String message;
            // Getters and setters
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
            // Getters
            public Long getId() { return id; }
            public String getMessage() { return message; }
        }
    }
