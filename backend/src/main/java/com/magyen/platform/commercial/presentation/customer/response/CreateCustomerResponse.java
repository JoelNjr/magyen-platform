package com.magyen.platform.commercial.presentation.customer.response;

import com.magyen.platform.commercial.domain.CustomerCategory;

import java.util.UUID;

/**
 * Respuesta HTTP tras crear un cliente exitosamente.
 */
public record CreateCustomerResponse(
        UUID customerId,
        String name,
        CustomerCategory category
) {
}
