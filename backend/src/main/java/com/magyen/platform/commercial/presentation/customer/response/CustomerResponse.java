package com.magyen.platform.commercial.presentation.customer.response;

import java.util.UUID;

/**
 * Cliente expuesto por la API de consulta.
 */
public record CustomerResponse(
        UUID customerId,
        String name
) {
}
