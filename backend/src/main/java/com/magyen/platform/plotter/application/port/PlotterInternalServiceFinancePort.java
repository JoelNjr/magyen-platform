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

    /**
     * Crea el par si no existe, o actualiza monto y fecha de los dos lados.
     */
    void synchronizeInternalServiceLedger(
            UUID plotterJobId,
            BigDecimal amount,
            LocalDate transactionDate,
            String observation
    );

    /**
     * Elimina los dos lados del par. Idempotente si ya no existen.
     */
    void reverseInternalServiceLedger(UUID plotterJobId);

    boolean hasInternalServiceLedger(UUID plotterJobId);
}
