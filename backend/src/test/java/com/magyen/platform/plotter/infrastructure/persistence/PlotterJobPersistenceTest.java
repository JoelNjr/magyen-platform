package com.magyen.platform.plotter.infrastructure.persistence;

import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterJobStatus;
import com.magyen.platform.plotter.domain.PlotterJobType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class PlotterJobPersistenceTest {

    @Autowired
    private PlotterJobRepository plotterJobRepository;

    @Test
    void savesAndReloadsPlotterJobWithPrecisionAndStableUuid() {
        UUID customerId = UUID.randomUUID();
        UUID paperInventoryItemId = UUID.randomUUID();

        PlotterJob created = PlotterJob.create(
                customerId,
                LocalDate.of(2026, 8, 9),
                paperInventoryItemId,
                new BigDecimal("18.7500"),
                new BigDecimal("8000.00"),
                "Persistencia"
        );

        PlotterJob saved = plotterJobRepository.save(created);
        assertEquals(created.getId(), saved.getId());

        PlotterJob reloaded = plotterJobRepository.findById(created.getId()).orElseThrow();

        assertEquals(created.getId(), reloaded.getId());
        assertEquals(customerId, reloaded.getCustomerId());
        assertEquals(paperInventoryItemId, reloaded.getPaperInventoryItemId());
        assertEquals(LocalDate.of(2026, 8, 9), reloaded.getCreationDate());
        assertEquals(0, new BigDecimal("18.7500").compareTo(reloaded.getPrintedMeters()));
        assertEquals(new BigDecimal("8000.00"), reloaded.getPricePerMeter());
        assertEquals(new BigDecimal("150000.00"), reloaded.getTotalAmount());
        assertEquals(PlotterJobStatus.REGISTERED, reloaded.getStatus());
        assertEquals(PlotterJobType.EXTERNAL, reloaded.getJobType());
        assertEquals("Persistencia", reloaded.getObservations());
        assertTrue(plotterJobRepository.findAll().stream()
                .anyMatch(job -> job.getId().equals(created.getId())));
    }
}
