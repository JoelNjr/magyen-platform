package com.magyen.platform.plotter.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Línea del extracto. {@code JOB_DEBIT} nace del trabajo. {@code CUSTOMER_PAYMENT} es dinero recibido.
 */
public record PlotterAccountMovement(
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
