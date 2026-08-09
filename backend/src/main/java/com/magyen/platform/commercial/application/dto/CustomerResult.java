package com.magyen.platform.commercial.application.dto;

import java.util.UUID;

/**
 * Representación de un cliente para casos de uso de consulta.
 */
public record CustomerResult(
        UUID customerId,
        String name
) {
}
