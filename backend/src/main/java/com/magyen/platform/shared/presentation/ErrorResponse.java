package com.magyen.platform.shared.presentation;

import java.time.LocalDateTime;

/**
 * DTO de salida para errores HTTP de la API REST.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
