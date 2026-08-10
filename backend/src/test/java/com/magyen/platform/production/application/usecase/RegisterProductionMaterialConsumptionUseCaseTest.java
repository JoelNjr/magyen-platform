package com.magyen.platform.production.application.usecase;

import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMovement;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.InventoryMovementType;
import com.magyen.platform.inventory.domain.MaterialCode;
import com.magyen.platform.inventory.domain.exception.InventoryDomainException;
import com.magyen.platform.production.application.dto.GetProductionMaterialConsumptionsQuery;
import com.magyen.platform.production.application.dto.GetProductionMaterialConsumptionsResult;
import com.magyen.platform.production.application.dto.PlanProductionOrderCommand;
import com.magyen.platform.production.application.dto.RegisterProductionMaterialConsumptionCommand;
import com.magyen.platform.production.application.dto.RegisterProductionMaterialConsumptionResult;
import com.magyen.platform.production.application.dto.StartProductionOrderCommand;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.ProductionStatus;
import com.magyen.platform.production.domain.exception.ProductionDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class RegisterProductionMaterialConsumptionUseCaseTest {

    @Autowired
    private RegisterProductionMaterialConsumptionUseCase registerProductionMaterialConsumptionUseCase;

    @Autowired
    private GetProductionMaterialConsumptionsUseCase getProductionMaterialConsumptionsUseCase;

    @Autowired
    private PlanProductionOrderUseCase planProductionOrderUseCase;

    @Autowired
    private StartProductionOrderUseCase startProductionOrderUseCase;

    @Autowired
    private CompleteProductionOrderUseCase completeProductionOrderUseCase;

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    private UUID productionOrderId;
    private InventoryItem inventoryItem;

    @BeforeEach
    void setUp() {
        ProductionOrder created = productionOrderRepository.save(ProductionOrder.create(
                UUID.randomUUID(),
                LocalDate.now(),
                ProductionPriority.NORMAL,
                null,
                null,
                "consumption test"
        ));
        productionOrderId = created.getId();

        inventoryItem = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("PINC-" + UUID.randomUUID().toString().substring(0, 8)),
                "Tela Fastra",
                "FABRIC",
                "METER",
                new BigDecimal("135.2500"),
                null,
                null,
                new BigDecimal("18000.00")
        ));
    }

    @Test
    void registersConsumptionAndDeductsInventoryOnceWithProductionSource() {
        moveToInProgress();

        RegisterProductionMaterialConsumptionResult result =
                registerProductionMaterialConsumptionUseCase.execute(
                        new RegisterProductionMaterialConsumptionCommand(
                                productionOrderId,
                                inventoryItem.getId(),
                                new BigDecimal("18.7000"),
                                "METER",
                                "Tela utilizada durante confección"
                        )
                );

        assertEquals(productionOrderId, result.productionOrderId());
        assertEquals(inventoryItem.getId(), result.inventoryItemId());
        assertEquals(new BigDecimal("18.7000"), result.quantity());

        GetProductionMaterialConsumptionsResult history = getProductionMaterialConsumptionsUseCase.execute(
                new GetProductionMaterialConsumptionsQuery(productionOrderId)
        );
        assertEquals(1, history.consumptions().size());
        assertEquals(result.consumptionId(), history.consumptions().getFirst().consumptionId());

        InventoryItem reloadedItem = inventoryItemRepository.findById(inventoryItem.getId()).orElseThrow();
        assertEquals(new BigDecimal("116.5500"), reloadedItem.getStock());

        List<InventoryMovement> movements = inventoryMovementRepository
                .findByInventoryItemIdOrderByMovementDateDesc(inventoryItem.getId());
        assertEquals(1, movements.size());
        InventoryMovement movement = movements.getFirst();
        assertEquals(InventoryMovementType.OUT, movement.getMovementType());
        assertEquals(InventoryMovementSourceType.PRODUCTION, movement.getSourceType());
        assertEquals(result.consumptionId(), movement.getSourceId());
        assertEquals(new BigDecimal("18000.00"), movement.getUnitCost());
        assertEquals(new BigDecimal("336600.00"), movement.getTotalCost());

        ProductionOrder reloaded = productionOrderRepository.findById(productionOrderId).orElseThrow();
        assertEquals(ProductionStatus.IN_PROGRESS, reloaded.getStatus());
        assertEquals(1, reloaded.getMaterialConsumptions().size());
    }

    @Test
    void rejectsInsufficientStockAndDoesNotPersistConsumption() {
        moveToInProgress();

        assertThrows(InventoryDomainException.class, () -> register(
                productionOrderId,
                inventoryItem.getId(),
                "200.0000",
                "METER"
        ));

        assertTrue(getProductionMaterialConsumptionsUseCase.execute(
                new GetProductionMaterialConsumptionsQuery(productionOrderId)
        ).consumptions().isEmpty());
        assertEquals(
                new BigDecimal("135.2500"),
                inventoryItemRepository.findById(inventoryItem.getId()).orElseThrow().getStock()
        );
    }

    @Test
    void rejectsIncompatibleUnit() {
        moveToInProgress();

        assertThrows(InventoryDomainException.class, () -> register(
                productionOrderId,
                inventoryItem.getId(),
                "1.0000",
                "ROLL"
        ));
    }

    @Test
    void returnsConsumptionsNewestFirst() {
        moveToInProgress();

        ProductionOrder productionOrder = productionOrderRepository.findById(productionOrderId).orElseThrow();
        UUID olderId = UUID.randomUUID();
        UUID newerId = UUID.randomUUID();

        ProductionOrder reconstituted = ProductionOrder.reconstitute(
                productionOrder.getId(),
                productionOrder.getOrderId(),
                productionOrder.getCreationDate(),
                productionOrder.getStatus(),
                productionOrder.getPriority(),
                productionOrder.getPlannedStartDate(),
                productionOrder.getPlannedEndDate(),
                productionOrder.getObservations(),
                productionOrder.getItems(),
                productionOrder.getOperations(),
                List.of(
                        com.magyen.platform.production.domain.ProductionMaterialConsumption.reconstitute(
                                olderId,
                                productionOrderId,
                                inventoryItem.getId(),
                                new BigDecimal("1.0000"),
                                com.magyen.platform.production.domain.ProductionMaterialUnitOfMeasure.METER,
                                java.time.LocalDateTime.now().minusHours(2),
                                "older"
                        ),
                        com.magyen.platform.production.domain.ProductionMaterialConsumption.reconstitute(
                                newerId,
                                productionOrderId,
                                inventoryItem.getId(),
                                new BigDecimal("2.0000"),
                                com.magyen.platform.production.domain.ProductionMaterialUnitOfMeasure.METER,
                                java.time.LocalDateTime.now(),
                                "newer"
                        )
                )
        );
        productionOrderRepository.save(reconstituted);

        GetProductionMaterialConsumptionsResult history = getProductionMaterialConsumptionsUseCase.execute(
                new GetProductionMaterialConsumptionsQuery(productionOrderId)
        );

        assertEquals(2, history.consumptions().size());
        assertEquals("newer", history.consumptions().get(0).observation());
        assertEquals("older", history.consumptions().get(1).observation());
    }

    @Test
    void rejectsCreatedPlannedCompletedAndMissingOrder() {
        assertThrows(ProductionDomainException.class, () -> register(
                productionOrderId,
                inventoryItem.getId(),
                "1.0000",
                "METER"
        ));

        planProductionOrderUseCase.execute(new PlanProductionOrderCommand(
                productionOrderId,
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                ProductionPriority.NORMAL
        ));
        assertThrows(ProductionDomainException.class, () -> register(
                productionOrderId,
                inventoryItem.getId(),
                "1.0000",
                "METER"
        ));

        startProductionOrderUseCase.execute(new StartProductionOrderCommand(productionOrderId));
        completeProductionOrderUseCase.execute(
                new com.magyen.platform.production.application.dto.CompleteProductionOrderCommand(productionOrderId)
        );
        assertThrows(ProductionDomainException.class, () -> register(
                productionOrderId,
                inventoryItem.getId(),
                "1.0000",
                "METER"
        ));

        IllegalArgumentException missing = assertThrows(
                IllegalArgumentException.class,
                () -> register(UUID.randomUUID(), inventoryItem.getId(), "1.0000", "METER")
        );
        assertTrue(missing.getMessage().contains("Production order not found"));
    }

    private void moveToInProgress() {
        planProductionOrderUseCase.execute(new PlanProductionOrderCommand(
                productionOrderId,
                LocalDate.now(),
                LocalDate.now().plusDays(2),
                ProductionPriority.NORMAL
        ));
        startProductionOrderUseCase.execute(new StartProductionOrderCommand(productionOrderId));
    }

    private RegisterProductionMaterialConsumptionResult register(
            UUID orderId,
            UUID inventoryItemId,
            String quantity,
            String unit
    ) {
        return registerProductionMaterialConsumptionUseCase.execute(
                new RegisterProductionMaterialConsumptionCommand(
                        orderId,
                        inventoryItemId,
                        new BigDecimal(quantity),
                        unit,
                        null
                )
        );
    }
}
