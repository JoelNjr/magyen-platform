package com.magyen.platform.finance.application.dto;

import java.util.UUID;

/**
 * Entrada del caso de uso para consultar un pago por identidad.
 */
public record GetPaymentQuery(
        UUID paymentId
) {
}
