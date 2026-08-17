package com.magyen.platform.commercial.presentation.seller.response;

import java.util.UUID;

/**
 * Vendedor expuesto por la API de consulta.
 */
public record SellerResponse(
        UUID sellerId,
        String name,
        boolean active
) {
}
