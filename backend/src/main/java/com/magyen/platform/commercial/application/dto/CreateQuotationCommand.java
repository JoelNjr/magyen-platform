package com.magyen.platform.commercial.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entrada del caso de uso para crear una cotización.
 * <p>
 * {@code quotationDate} es opcional: si es nulo, se usa la fecha de hoy.
 */
public record CreateQuotationCommand(
        UUID customerId,
        LocalDate deliveryDate,
        UUID sellerId,
        String observations,
        LocalDate quotationDate
) {
}
