package com.magyen.platform.commercial.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entrada del caso de uso para crear una cotización.
 */
public record CreateQuotationCommand(
        UUID customerId,
        LocalDate deliveryDate,
        String salesperson,
        String observations
) {
}
