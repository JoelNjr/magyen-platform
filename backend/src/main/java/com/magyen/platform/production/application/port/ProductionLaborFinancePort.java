package com.magyen.platform.production.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Puerto de Application para registrar el gasto de caja de mano de obra en Finance.
 */
public interface ProductionLaborFinancePort {

    UUID registerLaborExpense(
            UUID laborWorkId,
            BigDecimal amount,
            LocalDate paymentDate,
            String operatorDisplayName,
            String observation
    );
}
