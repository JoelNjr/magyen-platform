package com.magyen.platform.commercial.application.dto;

import java.util.UUID;

/**
 * Resultado del caso de uso de creación de vendedor.
 */
public record CreateSellerResult(
        UUID sellerId,
        String name,
        boolean active
) {
}
