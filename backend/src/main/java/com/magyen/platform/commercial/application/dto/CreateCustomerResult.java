package com.magyen.platform.commercial.application.dto;

import com.magyen.platform.commercial.domain.CustomerCategory;

import java.util.UUID;

/**
 * Resultado del caso de uso de creación de cliente.
 */
public record CreateCustomerResult(
        UUID customerId,
        String name,
        CustomerCategory category
) {
}
