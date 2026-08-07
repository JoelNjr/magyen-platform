package com.magyen.platform.intelligence.presentation.notification.controller;

import com.magyen.platform.intelligence.application.dto.GetNotificationsResult;
import com.magyen.platform.intelligence.application.usecase.GetNotificationsUseCase;
import com.magyen.platform.intelligence.presentation.notification.mapper.NotificationPresentationMapper;
import com.magyen.platform.intelligence.presentation.notification.response.GetNotificationsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone la API REST de notificaciones operativas.
 * <p>
 * Coordina HTTP con Application; no contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final GetNotificationsUseCase getNotificationsUseCase;
    private final NotificationPresentationMapper notificationPresentationMapper;

    public NotificationController(
            GetNotificationsUseCase getNotificationsUseCase,
            NotificationPresentationMapper notificationPresentationMapper
    ) {
        this.getNotificationsUseCase = getNotificationsUseCase;
        this.notificationPresentationMapper = notificationPresentationMapper;
    }

    @GetMapping
    public ResponseEntity<GetNotificationsResponse> getNotifications() {
        GetNotificationsResult result = getNotificationsUseCase.execute();
        GetNotificationsResponse response = notificationPresentationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }
}
