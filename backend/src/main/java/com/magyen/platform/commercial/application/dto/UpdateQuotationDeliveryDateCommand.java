package com.magyen.platform.commercial.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entrada para corregir la fecha de entrega de una cotización.
 */
public record UpdateQuotationDeliveryDateCommand(
        UUID quotationId,
        LocalDate deliveryDate
) {
}
