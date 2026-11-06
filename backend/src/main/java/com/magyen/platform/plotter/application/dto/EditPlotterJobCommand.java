package com.magyen.platform.plotter.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Corrección de un trabajo existente. No cambia cliente, orden, tipo ni máquina.
 */
public record EditPlotterJobCommand(
        UUID plotterJobId,
        LocalDate creationDate,
        UUID paperInventoryItemId,
        BigDecimal printedMeters,
        BigDecimal pricePerMeter,
        BigDecimal commercialValue,
        String observations,
        UUID plotterMachineId
) {
}
