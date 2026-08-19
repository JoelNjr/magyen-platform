package com.magyen.platform.plotter.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Puerto Plotter → Finance para el par EXPENSE/INCOME del servicio interno.
 */
public interface PlotterInternalServiceFinancePort {

    void ensureInternalServiceLedger(
            UUID plotterJobId,
            BigDecimal amount,
            LocalDate transactionDate,
            String observation
    );
}
