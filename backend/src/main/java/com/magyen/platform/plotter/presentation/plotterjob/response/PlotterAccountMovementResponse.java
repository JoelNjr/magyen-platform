package com.magyen.platform.plotter.presentation.plotterjob.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PlotterAccountMovementResponse(
        String movementType,
        LocalDate movementDate,
        UUID plotterJobId,
        UUID paymentId,
        String description,
        BigDecimal printedMeters,
        UUID plotterMachineId,
        String machineName,
        BigDecimal amount
) {
}
