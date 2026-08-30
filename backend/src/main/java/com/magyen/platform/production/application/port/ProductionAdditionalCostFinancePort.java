package com.magyen.platform.production.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Puerto de Application para registrar el gasto de un costo directo adicional.
 */
public interface ProductionAdditionalCostFinancePort {

    UUID registerAdditionalCostExpense(
            UUID additionalCostId,
            BigDecimal amount,
            LocalDate incurredDate,
            String description
    );
}
