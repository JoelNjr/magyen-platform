package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.inventory.application.dto.ConsumeInventoryMaterialCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.usecase.ConsumeInventoryMaterialUseCase;
import com.magyen.platform.inventory.application.usecase.CreateInventoryItemUseCase;
import com.magyen.platform.inventory.application.usecase.GetInventoryItemUseCase;
import com.magyen.platform.inventory.application.dto.GetInventoryItemQuery;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobCommand;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobResult;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
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
class PlotterInventoryIntegrationUseCaseTest {

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Autowired
    private CreatePlotterJobUseCase createPlotterJobUseCase;

    @Autowired
    private GetInventoryItemUseCase getInventoryItemUseCase;

    @Autowired
    private ConsumeInventoryMaterialUseCase consumeInventoryMaterialUseCase;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private PlotterJobRepository plotterJobRepository;

    @Test
    void validPaperRollJobConsumesExactMetersAndCapturesCost() {
        CreateInventoryItemResult roll = createPaperRoll(new BigDecimal("100.0000"), new BigDecimal("4500.00"));

        CreatePlotterJobResult job = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                UUID.randomUUID(),
                roll.inventoryItemId(),
                new BigDecimal("12.50"),
                new BigDecimal("8000"),
                "Uniformes"
        ));

        assertEquals(new BigDecimal("100000.00"), job.totalAmount());

        var item = getInventoryItemUseCase.execute(new GetInventoryItemQuery(roll.inventoryItemId()));
        assertEquals(0, new BigDecimal("87.5000").compareTo(item.stock()));

        var movement = inventoryMovementRepository
                .findBySourceTypeAndSourceId(InventoryMovementSourceType.PLOTTER, job.plotterJobId())
                .orElseThrow();
        assertEquals(0, new BigDecimal("12.5000").compareTo(movement.getQuantity()));
        assertEquals(new BigDecimal("4500.00"), movement.getUnitCost());
        assertEquals(new BigDecimal("56250.00"), movement.getTotalCost());
        assertEquals(InventoryMovementSourceType.PLOTTER, movement.getSourceType());
        assertEquals(job.plotterJobId(), movement.getSourceId());
    }

    @Test
    void rejectsFabricAndInsufficientStockWithoutPersistingJob() {
        CreateInventoryItemResult fabric = createInventoryItemUseCase.execute(
                new CreateInventoryItemCommand(
                        "FAB-PLT-" + UUID.randomUUID().toString().substring(0, 8),
                        "Tela",
                        "FABRIC",
                        "METER",
                        new BigDecimal("50.0000"),
                        null,
                        null,
                        new BigDecimal("1000.00"),
                        "FABRIC",
                        false
                )
        );

        assertThrows(PlotterDomainException.class, () ->
                createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                        UUID.randomUUID(),
                        fabric.inventoryItemId(),
                        new BigDecimal("1.00"),
                        new BigDecimal("8000"),
                        null
                ))
        );

        CreateInventoryItemResult roll = createPaperRoll(new BigDecimal("10.0000"), new BigDecimal("4500.00"));
        long jobsBefore = plotterJobRepository.findAll().size();

        assertThrows(PlotterDomainException.class, () ->
                createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                        UUID.randomUUID(),
                        roll.inventoryItemId(),
                        new BigDecimal("12.50"),
                        new BigDecimal("8000"),
                        null
                ))
        );

        assertEquals(jobsBefore, plotterJobRepository.findAll().size());
        var item = getInventoryItemUseCase.execute(new GetInventoryItemQuery(roll.inventoryItemId()));
        assertEquals(0, new BigDecimal("10.0000").compareTo(item.stock()));
    }

    @Test
    void samePlotterJobCannotCreateTwoMovements() {
        CreateInventoryItemResult roll = createPaperRoll(new BigDecimal("100.0000"), new BigDecimal("4500.00"));
        CreatePlotterJobResult job = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                UUID.randomUUID(),
                roll.inventoryItemId(),
                new BigDecimal("12.50"),
                new BigDecimal("8000"),
                null
        ));

        var second = consumeInventoryMaterialUseCase.execute(new ConsumeInventoryMaterialCommand(
                roll.inventoryItemId(),
                new BigDecimal("12.50"),
                "METER",
                InventoryMovementSourceType.PLOTTER,
                job.plotterJobId(),
                "retry"
        ));

        assertTrue(second.alreadyProcessed());
        var item = getInventoryItemUseCase.execute(new GetInventoryItemQuery(roll.inventoryItemId()));
        assertEquals(0, new BigDecimal("87.5000").compareTo(item.stock()));
        assertTrue(inventoryMovementRepository
                .findBySourceTypeAndSourceId(InventoryMovementSourceType.PLOTTER, job.plotterJobId())
                .isPresent());
    }

    private CreateInventoryItemResult createPaperRoll(BigDecimal stock, BigDecimal unitCost) {
        return createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "RPTEST-" + UUID.randomUUID().toString().substring(0, 8),
                "Papel plotter",
                "PAPER",
                "METER",
                stock,
                new BigDecimal("20.0000"),
                "Rollo",
                unitCost,
                "PAPER",
                true
        ));
    }
}
