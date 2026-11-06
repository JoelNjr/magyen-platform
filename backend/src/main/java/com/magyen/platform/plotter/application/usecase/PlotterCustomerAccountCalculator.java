package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobStatus;
import com.magyen.platform.plotter.domain.PlotterPayment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Cuenta corriente de un cliente externo de Plotter.
 * <p>
 * El débito es el valor comercial de cada trabajo externo no cancelado.
 * El crédito es cada pago del cliente, una sola vez, aunque el pago histórico
 * siga apuntando a un trabajo. No existen cargos manuales.
 */
public final class PlotterCustomerAccountCalculator {

    public static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private PlotterCustomerAccountCalculator() {
    }

    public static Map<UUID, Position> calculate(List<PlotterJob> jobs, List<PlotterPayment> payments) {
        Map<UUID, PlotterJob> jobsById = new LinkedHashMap<>();
        for (PlotterJob job : jobs) {
            jobsById.put(job.getId(), job);
        }

        Map<UUID, MutablePosition> positions = new LinkedHashMap<>();
        for (PlotterJob job : jobs) {
            if (!job.getJobType().isExternal() || job.getStatus() == PlotterJobStatus.CANCELLED) {
                continue;
            }
            if (job.getCustomerId() == null) {
                continue;
            }
            positions.computeIfAbsent(job.getCustomerId(), MutablePosition::new)
                    .addDebit(job);
        }

        for (PlotterPayment payment : payments) {
            UUID customerId = resolveCustomerId(payment, jobsById);
            if (customerId == null) {
                continue;
            }
            positions.computeIfAbsent(customerId, MutablePosition::new)
                    .addPayment(payment);
        }

        Map<UUID, Position> result = new LinkedHashMap<>();
        for (MutablePosition position : positions.values()) {
            result.put(position.customerId, position.toPosition());
        }
        return result;
    }

    public static Position positionOf(
            UUID customerId,
            List<PlotterJob> jobs,
            List<PlotterPayment> payments
    ) {
        return calculate(jobs, payments).getOrDefault(customerId, Position.empty(customerId));
    }

    public static UUID resolveCustomerId(PlotterPayment payment, Map<UUID, PlotterJob> jobsById) {
        if (payment.getCustomerId() != null) {
            return payment.getCustomerId();
        }
        if (payment.getPlotterJobId() == null) {
            return null;
        }
        PlotterJob job = jobsById.get(payment.getPlotterJobId());
        return job == null ? null : job.getCustomerId();
    }

    public static BigDecimal money(BigDecimal amount) {
        return amount.setScale(SCALE, ROUNDING);
    }

    public record Position(
            UUID customerId,
            BigDecimal debitTotal,
            BigDecimal paymentTotal,
            BigDecimal outstandingAmount,
            List<PlotterJob> jobs,
            List<PlotterPayment> payments
    ) {
        public static Position empty(UUID customerId) {
            return new Position(
                    customerId,
                    money(BigDecimal.ZERO),
                    money(BigDecimal.ZERO),
                    money(BigDecimal.ZERO),
                    List.of(),
                    List.of()
            );
        }
    }

    private static final class MutablePosition {
        private final UUID customerId;
        private BigDecimal debitTotal = money(BigDecimal.ZERO);
        private BigDecimal paymentTotal = money(BigDecimal.ZERO);
        private final List<PlotterJob> jobs = new ArrayList<>();
        private final List<PlotterPayment> payments = new ArrayList<>();

        private MutablePosition(UUID customerId) {
            this.customerId = customerId;
        }

        private void addDebit(PlotterJob job) {
            debitTotal = money(debitTotal.add(job.getTotalAmount()));
            jobs.add(job);
        }

        private void addPayment(PlotterPayment payment) {
            paymentTotal = money(paymentTotal.add(payment.getAmount()));
            payments.add(payment);
        }

        private Position toPosition() {
            return new Position(
                    customerId,
                    debitTotal,
                    paymentTotal,
                    money(debitTotal.subtract(paymentTotal)),
                    List.copyOf(jobs),
                    List.copyOf(payments)
            );
        }
    }
}
