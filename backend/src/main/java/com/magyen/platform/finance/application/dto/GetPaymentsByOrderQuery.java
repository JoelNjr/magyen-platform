package com.magyen.platform.finance.application.dto;

import java.util.UUID;

/**
 * Entrada del caso de uso para consultar los pagos de una Orden.
 */
public record GetPaymentsByOrderQuery(
        UUID orderId
) {
}
