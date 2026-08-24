package com.magyen.platform.commercial.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entrada del caso de uso para crear una Orden a partir de una Cotización aprobada.
 * <p>
 * El vendedor y el número de orden se toman de la cotización; no se aceptan
 * como texto libre.
 */
public record CreateOrderFromQuotationCommand(
        UUID quotationId,
        String description,
        LocalDate confirmationDate,
        LocalDate deliveryDate,
        String observations
) {
}
