package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobStatus;
import com.magyen.platform.plotter.domain.PlotterJobType;
import com.magyen.platform.plotter.domain.PlotterPayment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlotterCustomerAccountCalculatorTest {

    @Test
    void severalExternalJobsAccumulateAndAnUnallocatedPaymentReducesTheBalanceOnce() {
        UUID customerId = UUID.randomUUID();
        UUID machineId = UUID.randomUUID();
        PlotterJob first = external(customerId, machineId, "4000", "7500");
        PlotterJob second = external(customerId, machineId, "2000", "2000");
        PlotterJob cancelled = external(customerId, machineId, "9000", "9000").cancel();
        PlotterPayment historical = PlotterPayment.createForJob(
                customerId,
                first.getId(),
                new BigDecimal("3000"),
                LocalDate.of(2026, 8, 10),
                "Abono histórico"
        );
        PlotterPayment accountPayment = PlotterPayment.createForCustomer(
                customerId,
                new BigDecimal("1500"),
                LocalDate.of(2026, 8, 20),
                "Transferencia",
                UUID.randomUUID()
        );

        var position = PlotterCustomerAccountCalculator.positionOf(
                customerId,
                List.of(first, second, cancelled),
                List.of(historical, accountPayment)
        );

        assertEquals(new BigDecimal("9500.00"), position.debitTotal());
        assertEquals(new BigDecimal("4500.00"), position.paymentTotal());
        assertEquals(new BigDecimal("5000.00"), position.outstandingAmount());
        assertEquals(2, position.jobs().size());
        assertNull(accountPayment.getPlotterJobId());
        assertEquals(LocalDate.of(2026, 8, 10), historical.getPaymentDate());
    }

    @Test
    void aHistoricalPaymentWithoutCustomerIdStillFollowsItsJob() {
        UUID customerId = UUID.randomUUID();
        PlotterJob job = external(customerId, UUID.randomUUID(), "1000", "1000");
        PlotterPayment payment = PlotterPayment.create(
                job.getId(),
                new BigDecimal("400"),
                LocalDate.of(2026, 7, 1),
                null
        );

        var position = PlotterCustomerAccountCalculator.positionOf(
                customerId,
                List.of(job),
                List.of(payment)
        );

        assertEquals(new BigDecimal("600.00"), position.outstandingAmount());
    }

    private static PlotterJob external(
            UUID customerId,
            UUID machineId,
            String calculated,
            String commercial
    ) {
        return PlotterJob.create(
                UUID.randomUUID(),
                PlotterJobType.EXTERNAL,
                customerId,
                null,
                LocalDate.of(2026, 8, 4),
                UUID.randomUUID(),
                new BigDecimal("4"),
                new BigDecimal(calculated).divide(new BigDecimal("4")),
                null,
                machineId
        ).withExternalCommercialValue(new BigDecimal(commercial));
    }
}
