package com.magyen.platform.intelligence.application.dto;

import java.time.LocalDateTime;

/**
 * Notificación operativa generada a partir del estado actual de la plataforma.
 */
public record NotificationResult(
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
