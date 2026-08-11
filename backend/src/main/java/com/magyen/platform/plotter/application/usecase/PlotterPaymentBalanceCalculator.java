package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.domain.PlotterPayment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Calcula totales de pago de Plotter a partir del modelo de pagos (no del ledger).
 */
final class PlotterPaymentBalanceCalculator {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private PlotterPaymentBalanceCalculator() {
    }

    static BigDecimal sumPaid(List<PlotterPayment> payments) {
        Objects.requireNonNull(payments, "Payments must not be null");
        return payments.stream()
                .map(PlotterPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, ROUNDING_MODE);
    }

    static BigDecimal outstanding(BigDecimal totalAmount, BigDecimal paidAmount) {
        Objects.requireNonNull(totalAmount, "Total amount must not be null");
        Objects.requireNonNull(paidAmount, "Paid amount must not be null");
        return totalAmount.subtract(paidAmount).setScale(SCALE, ROUNDING_MODE);
    }
}
