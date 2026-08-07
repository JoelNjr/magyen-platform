package com.magyen.platform.intelligence.presentation.notification.mapper;

import com.magyen.platform.intelligence.application.dto.GetNotificationsResult;
import com.magyen.platform.intelligence.presentation.notification.response.GetNotificationsResponse;
import com.magyen.platform.intelligence.presentation.notification.response.GetNotificationsResponse.NotificationResponse;

import java.util.List;
import java.util.Objects;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application para notificaciones.
 * <p>
 * No contiene reglas de negocio ni accede a repositorios, dominio o infraestructura.
 */
public class NotificationPresentationMapper {

    public GetNotificationsResponse toResponse(GetNotificationsResult result) {
        Objects.requireNonNull(result, "GetNotificationsResult must not be null");

        List<NotificationResponse> notifications = result.notifications().stream()
                .map(notification -> new NotificationResponse(
                        notification.notificationId(),
                        notification.type(),
                        notification.title(),
                        notification.message(),
                        notification.severity(),
                        notification.createdAt(),
                        notification.referenceId(),
                        notification.module()
                ))
                .toList();

        return new GetNotificationsResponse(notifications);
    }
}
