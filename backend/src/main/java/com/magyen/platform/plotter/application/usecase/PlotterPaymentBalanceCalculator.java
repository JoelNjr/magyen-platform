package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterPayment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Calcula totales de pago de Plotter a partir del modelo de pagos (no del ledger).
 * <p>
 * El servicio interno Magyen no es cobrable a un cliente externo: el saldo por cobrar es 0.
 */
final class PlotterPaymentBalanceCalculator {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);

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

    static BigDecimal collectableOutstanding(PlotterJob job, BigDecimal paidAmount) {
        Objects.requireNonNull(job, "Plotter job must not be null");
        if (job.getJobType().isInternal() || job.getJobType().isWaste()) {
            return ZERO;
        }
        return outstanding(job.getTotalAmount(), paidAmount);
    }
}
