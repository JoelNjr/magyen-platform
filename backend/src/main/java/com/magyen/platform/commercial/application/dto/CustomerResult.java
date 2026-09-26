package com.magyen.platform.commercial.application.dto;

import com.magyen.platform.commercial.domain.CustomerCategory;

import java.util.UUID;

/**
 * Representación de un cliente para casos de uso de consulta.
 */
public record CustomerResult(
        UUID customerId,
        String name,
        CustomerCategory category
) {
    public CustomerResult(UUID customerId, String name) {
        this(customerId, name, CustomerCategory.MAGYEN);
    }
}
