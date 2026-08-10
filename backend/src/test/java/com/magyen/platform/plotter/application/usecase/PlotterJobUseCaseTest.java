package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.usecase.CreateInventoryItemUseCase;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobCommand;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobResult;
import com.magyen.platform.plotter.application.dto.GetPlotterJobQuery;
import com.magyen.platform.plotter.application.dto.GetPlotterJobResult;
import com.magyen.platform.plotter.application.dto.GetPlotterJobsResult;
import com.magyen.platform.plotter.domain.PlotterJobStatus;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class PlotterJobUseCaseTest {

    @Autowired
    private CreatePlotterJobUseCase createPlotterJobUseCase;

    @Autowired
    private GetPlotterJobUseCase getPlotterJobUseCase;

    @Autowired
    private GetPlotterJobsUseCase getPlotterJobsUseCase;

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Test
    void createsListsAndGetsPlotterJobWithServerCalculatedTotal() {
        CreateInventoryItemResult roll = createPaperRoll();
        UUID customerId = UUID.randomUUID();

        CreatePlotterJobResult created = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                customerId,
                roll.inventoryItemId(),
                new BigDecimal("10.5"),
                new BigDecimal("8000"),
                "Trabajo de verificación"
        ));

        assertEquals(new BigDecimal("84000.00"), created.totalAmount());
        assertEquals(PlotterJobStatus.REGISTERED, created.status());
        assertEquals(customerId, created.customerId());
        assertEquals(roll.inventoryItemId(), created.paperInventoryItemId());

        GetPlotterJobResult detail = getPlotterJobUseCase.execute(
                new GetPlotterJobQuery(created.plotterJobId())
        );
        assertEquals(created.plotterJobId(), detail.plotterJobId());
        assertEquals(new BigDecimal("84000.00"), detail.totalAmount());
        assertEquals("Trabajo de verificación", detail.observations());

        GetPlotterJobsResult list = getPlotterJobsUseCase.execute();
        assertTrue(list.jobs().stream()
                .anyMatch(job -> job.plotterJobId().equals(created.plotterJobId())));
    }

    @Test
    void rejectsMissingEntityOnGet() {
        assertThrows(IllegalArgumentException.class, () ->
                getPlotterJobUseCase.execute(new GetPlotterJobQuery(UUID.randomUUID()))
        );
    }

    @Test
    void rejectsInvalidPrintedMetersAndPrice() {
        CreateInventoryItemResult roll = createPaperRoll();

        assertThrows(PlotterDomainException.class, () ->
                createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                        UUID.randomUUID(),
                        roll.inventoryItemId(),
                        BigDecimal.ZERO,
                        new BigDecimal("8000"),
                        null
                ))
        );

        assertThrows(PlotterDomainException.class, () ->
                createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                        UUID.randomUUID(),
                        roll.inventoryItemId(),
                        new BigDecimal("10"),
                        new BigDecimal("-1"),
                        null
                ))
        );
    }

    @Test
    void rejectsNullCustomerAndPaperReferences() {
        CreateInventoryItemResult roll = createPaperRoll();

        assertThrows(PlotterDomainException.class, () ->
                createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                        null,
                        roll.inventoryItemId(),
                        new BigDecimal("10"),
                        new BigDecimal("8000"),
                        null
                ))
        );

        assertThrows(PlotterDomainException.class, () ->
                createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("10"),
                        new BigDecimal("8000"),
                        null
                ))
        );
    }

    private CreateInventoryItemResult createPaperRoll() {
        return createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "PLTUC-" + UUID.randomUUID().toString().substring(0, 8),
                "Papel plotter",
                "PAPER",
                "METER",
                new BigDecimal("100.0000"),
                new BigDecimal("20.0000"),
                null,
                new BigDecimal("4500.00"),
                "PAPER",
                true
        ));
    }
}
