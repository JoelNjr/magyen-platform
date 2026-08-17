package com.magyen.platform.plotter.domain;

import com.magyen.platform.plotter.domain.exception.PlotterDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlotterJobTest {

    @Test
    void createsValidPlotterJobWithCalculatedTotalAndRegisteredStatus() {
        UUID customerId = UUID.randomUUID();
        UUID paperInventoryItemId = UUID.randomUUID();
        LocalDate creationDate = LocalDate.of(2026, 8, 9);

        PlotterJob plotterJob = PlotterJob.create(
                customerId,
                creationDate,
                paperInventoryItemId,
                new BigDecimal("18.50"),
                new BigDecimal("8000"),
                "Trabajo para uniformes"
        );

        assertEquals(customerId, plotterJob.getCustomerId());
        assertEquals(creationDate, plotterJob.getCreationDate());
        assertEquals(paperInventoryItemId, plotterJob.getPaperInventoryItemId());
        assertEquals(0, new BigDecimal("18.50").compareTo(plotterJob.getPrintedMeters()));
        assertEquals(new BigDecimal("8000.00"), plotterJob.getPricePerMeter());
        assertEquals(new BigDecimal("148000.00"), plotterJob.getTotalAmount());
        assertEquals(PlotterJobStatus.REGISTERED, plotterJob.getStatus());
        assertEquals("Trabajo para uniformes", plotterJob.getObservations());
        assertTrue(plotterJob.getId() != null);
        assertNull(plotterJob.getOrderId());
    }

    @Test
    void storesOptionalCommercialOrderAttribution() {
        UUID orderId = UUID.randomUUID();
        PlotterJob plotterJob = PlotterJob.create(
                UUID.randomUUID(),
                orderId,
                LocalDate.of(2026, 8, 3),
                UUID.randomUUID(),
                new BigDecimal("6"),
                new BigDecimal("8000"),
                null
        );

        assertEquals(orderId, plotterJob.getOrderId());
        assertEquals(new BigDecimal("48000.00"), plotterJob.getTotalAmount());
    }

    @Test
    void rejectsPrintedMetersNotGreaterThanZero() {
        assertThrows(PlotterDomainException.class, () -> PlotterJob.create(
                UUID.randomUUID(),
                LocalDate.now(),
                UUID.randomUUID(),
                BigDecimal.ZERO,
                new BigDecimal("8000"),
                null
        ));

        assertThrows(PlotterDomainException.class, () -> PlotterJob.create(
                UUID.randomUUID(),
                LocalDate.now(),
                UUID.randomUUID(),
                new BigDecimal("-1"),
                new BigDecimal("8000"),
                null
        ));
    }

    @Test
    void rejectsNegativePricePerMeter() {
        assertThrows(PlotterDomainException.class, () -> PlotterJob.create(
                UUID.randomUUID(),
                LocalDate.now(),
                UUID.randomUUID(),
                new BigDecimal("10.5"),
                new BigDecimal("-1"),
                null
        ));
    }

    @Test
    void allowsZeroPricePerMeter() {
        PlotterJob plotterJob = PlotterJob.create(
                UUID.randomUUID(),
                LocalDate.now(),
                UUID.randomUUID(),
                new BigDecimal("10.5000"),
                BigDecimal.ZERO,
                null
        );

        assertEquals(new BigDecimal("0.00"), plotterJob.getPricePerMeter());
        assertEquals(new BigDecimal("0.00"), plotterJob.getTotalAmount());
        assertNull(plotterJob.getObservations());
    }

    @Test
    void calculatesTotalWithDecimalMeters() {
        BigDecimal total = PlotterJob.calculateTotalAmount(
                new BigDecimal("10.5"),
                new BigDecimal("8000")
        );

        assertEquals(new BigDecimal("84000.00"), total);
    }
}
