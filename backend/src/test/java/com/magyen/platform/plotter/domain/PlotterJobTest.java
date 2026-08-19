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
        assertEquals(PlotterJobType.EXTERNAL, plotterJob.getJobType());
        assertEquals("Trabajo para uniformes", plotterJob.getObservations());
        assertTrue(plotterJob.getId() != null);
        assertNull(plotterJob.getOrderId());
    }

    @Test
    void internalJobRequiresOrderAndHasVariableServiceValue() {
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
        assertEquals(PlotterJobType.INTERNAL_MAGYEN, plotterJob.getJobType());
        assertEquals(new BigDecimal("8000.00"), plotterJob.getPricePerMeter());
        assertEquals(new BigDecimal("48000.00"), plotterJob.getTotalAmount());
    }

    @Test
    void rejectsInternalJobWithoutCommercialOrder() {
        assertThrows(PlotterDomainException.class, () -> PlotterJob.create(
                UUID.randomUUID(),
                PlotterJobType.INTERNAL_MAGYEN,
                UUID.randomUUID(),
                null,
                LocalDate.of(2026, 8, 3),
                UUID.randomUUID(),
                new BigDecimal("6"),
                BigDecimal.ZERO,
                null
        ));
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
    void reconstitutesLegacyExternalJobWithLeftoverOrderId() {
        UUID orderId = UUID.randomUUID();
        PlotterJob plotterJob = PlotterJob.reconstitute(
                UUID.randomUUID(),
                PlotterJobType.EXTERNAL,
                UUID.randomUUID(),
                orderId,
                LocalDate.of(2026, 8, 3),
                UUID.randomUUID(),
                new BigDecimal("7"),
                new BigDecimal("8000"),
                new BigDecimal("56000.00"),
                PlotterJobStatus.REGISTERED,
                null
        );

        assertEquals(PlotterJobType.EXTERNAL, plotterJob.getJobType());
        assertEquals(orderId, plotterJob.getOrderId());
        assertEquals(new BigDecimal("56000.00"), plotterJob.getTotalAmount());
    }

    @Test
    void wasteJobDoesNotRequireCustomerOrOrderAndHasZeroRevenue() {
        PlotterJob plotterJob = PlotterJob.create(
                UUID.randomUUID(),
                PlotterJobType.WASTE,
                null,
                null,
                LocalDate.of(2026, 8, 18),
                UUID.randomUUID(),
                new BigDecimal("3.5000"),
                BigDecimal.ZERO,
                "prueba fallida"
        );

        assertEquals(PlotterJobType.WASTE, plotterJob.getJobType());
        assertNull(plotterJob.getCustomerId());
        assertNull(plotterJob.getOrderId());
        assertEquals(new BigDecimal("0.00"), plotterJob.getTotalAmount());
        assertEquals("prueba fallida", plotterJob.getObservations());
    }

    @Test
    void rejectsWasteJobWithCustomerOrOrder() {
        assertThrows(PlotterDomainException.class, () -> PlotterJob.create(
                UUID.randomUUID(),
                PlotterJobType.WASTE,
                UUID.randomUUID(),
                null,
                LocalDate.of(2026, 8, 18),
                UUID.randomUUID(),
                new BigDecimal("2"),
                BigDecimal.ZERO,
                null
        ));
        assertThrows(PlotterDomainException.class, () -> PlotterJob.create(
                UUID.randomUUID(),
                PlotterJobType.WASTE,
                null,
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 18),
                UUID.randomUUID(),
                new BigDecimal("2"),
                BigDecimal.ZERO,
                null
        ));
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
