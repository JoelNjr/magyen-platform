package com.magyen.platform.inventory.application.dto;

import java.time.LocalDate;

/**
 * Consulta de adquisiciones de papel en un rango de fechas.
 */
public record GetPaperAcquisitionsQuery(
        LocalDate fromDate,
        LocalDate toDate
) {
}
