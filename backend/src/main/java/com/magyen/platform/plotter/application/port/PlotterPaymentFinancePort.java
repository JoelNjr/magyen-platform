package com.magyen.platform.plotter.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Puerto de salida de Plotter hacia el ledger Finance.
 * <p>
 * Plotter no depende de entidades JPA de Finance.
 */
public interface PlotterPaymentFinancePort {

    /**
     * Garantiza exactamente un ingreso del ledger para el pago de Plotter.
     * Idempotente por {@code plotterPaymentId}.
     */
    void ensureIncomeForPlotterPayment(
            UUID plotterPaymentId,
            BigDecimal amount,
            LocalDate paymentDate,
            String observation
    );
}
