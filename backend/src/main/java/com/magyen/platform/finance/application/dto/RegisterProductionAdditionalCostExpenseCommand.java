package com.magyen.platform.finance.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Comando para registrar el gasto de un costo directo adicional de producción.
 */
public record RegisterProductionAdditionalCostExpenseCommand(
        UUID additionalCostId,
        BigDecimal amount,
        LocalDate incurredDate,
        String description
) {
}
