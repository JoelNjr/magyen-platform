package com.magyen.platform.intelligence.presentation.notification.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Respuesta HTTP con las notificaciones operativas activas.
 */
public record GetNotificationsResponse(
        List<NotificationResponse> notifications
) {

    /**
     * Notificación operativa expuesta por la API.
     */
    public record NotificationResponse(
            String notificationId,
            String type,
            String title,
            String message,
            String severity,
            LocalDateTime createdAt,
            String referenceId,
            String module
    ) {
    }
}
