package com.magyen.platform.commercial.application.dto;

import com.magyen.platform.commercial.domain.CustomerCategory;

import java.util.UUID;

/**
 * Resultado del caso de uso de actualización de cliente.
 */
public record UpdateCustomerResult(
        UUID customerId,
        String name,
        CustomerCategory category
) {
}
