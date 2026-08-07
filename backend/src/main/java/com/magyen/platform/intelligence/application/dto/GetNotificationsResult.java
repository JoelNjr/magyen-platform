package com.magyen.platform.intelligence.application.dto;

import java.util.List;

/**
 * Resultado del caso de uso que consolida las notificaciones activas de la plataforma.
 */
public record GetNotificationsResult(
        List<NotificationResult> notifications
) {
}
