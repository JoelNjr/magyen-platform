package com.magyen.platform.commercial.application.dto;

import java.util.UUID;

/**
 * Entrada del caso de uso para actualizar el nombre de un cliente.
 */
public record UpdateCustomerCommand(
        UUID customerId,
        String name
) {
}
