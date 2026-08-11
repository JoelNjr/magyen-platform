package com.magyen.platform.commercial.application.dto;

import java.util.UUID;

/**
 * Consulta de rentabilidad directa de una Orden comercial.
 */
public record GetOrderProfitabilityQuery(
        UUID orderId
) {
}
