package com.magyen.platform.commercial.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Consulta analítica de comisión de un vendedor. Fechas nulas = todo el historial elegible.
 */
public record GetSellerCommissionQuery(
        UUID sellerEmployeeId,
        LocalDate fromDate,
        LocalDate toDate
) {
}
