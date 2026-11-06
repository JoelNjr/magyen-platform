package com.magyen.platform.plotter.presentation.plotterjob.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Corrección de un trabajo. La máquina, el cliente y la orden no se cambian.
 */
public record EditPlotterJobRequest(
        LocalDate creationDate,
        UUID paperInventoryItemId,
        BigDecimal printedMeters,
        BigDecimal pricePerMeter,
        BigDecimal commercialValue,
        String observations,
        UUID plotterMachineId
) {
}
