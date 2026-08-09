package com.magyen.platform.commercial.presentation.customer.response;

import java.util.UUID;

/**
 * Respuesta HTTP tras actualizar un cliente exitosamente.
 */
public record UpdateCustomerResponse(
        UUID customerId,
        String name
) {
}
