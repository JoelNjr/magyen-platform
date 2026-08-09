package com.magyen.platform.commercial.application.dto;

import java.util.UUID;

/**
 * Entrada del caso de uso para consultar una Orden por identificador.
 */
public record GetOrderCommand(
        UUID orderId
) {
}
