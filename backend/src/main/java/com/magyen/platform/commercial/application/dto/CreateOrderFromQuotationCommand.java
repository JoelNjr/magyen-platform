package com.magyen.platform.commercial.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entrada del caso de uso para crear una Orden a partir de una Cotización aprobada.
 */
public record CreateOrderFromQuotationCommand(
        UUID quotationId,
        String orderNumber,
        LocalDate deliveryDate,
        String salesperson,
        String observations
) {
}
