package com.magyen.platform.inventory.application.dto;

import java.time.LocalDate;

/**
 * Consulta de adquisiciones de tinta en un rango de fechas.
 */
public record GetInkAcquisitionsQuery(
        LocalDate fromDate,
        LocalDate toDate
) {
}
