package com.magyen.platform.commercial.application.dto;

import java.util.UUID;

/**
 * Representación de un vendedor para casos de uso de consulta.
 */
public record SellerResult(
        UUID sellerId,
        String name,
        boolean active
) {
}
