package com.magyen.platform.commercial.presentation.seller.response;

import java.util.UUID;

/**
 * Respuesta HTTP tras crear un vendedor exitosamente.
 */
public record CreateSellerResponse(
        UUID sellerId,
        String name,
        boolean active
) {
}
