package com.magyen.platform.production.application.usecase;

import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMovement;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.MaterialCode;
import com.magyen.platform.production.application.dto.GetProductionMaterialConsumptionsQuery;
import com.magyen.platform.production.application.dto.GetProductionMaterialConsumptionsResult;
import com.magyen.platform.production.application.dto.GetProductionOrderCommand;
import com.magyen.platform.production.application.dto.GetProductionOrderResult;
import com.magyen.platform.production.application.dto.PlanProductionOrderCommand;
import com.magyen.platform.production.application.dto.RegisterProductionMaterialConsumptionCommand;
import com.magyen.platform.production.application.dto.RegisterProductionMaterialConsumptionResult;
import com.magyen.platform.production.application.dto.StartProductionOrderCommand;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fundamento de atribución de costo de materiales de producción.
 * <p>
 * El costo histórico vive en Inventory OUT; el consumo no crea gasto de caja en Finance.
 */
@SpringBootTest
@Transactional
class ProductionMaterialCostAttributionUseCaseTest {

    @Autowired
    private RegisterProductionMaterialConsumptionUseCase registerProductionMaterialConsumptionUseCase;

    @Autowired
    private GetProductionMaterialConsumptionsUseCase getProductionMaterialConsumptionsUseCase;

    @Autowired
    private GetProductionOrderUseCase getProductionOrderUseCase;

    @Autowired
    private PlanProductionOrderUseCase planProductionOrderUseCase;

    @Autowired
    private StartProductionOrderUseCase startProductionOrderUseCase;

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    private UUID productionOrderId;

    @BeforeEach
    void setUp() {
        ProductionOrder created = productionOrderRepository.save(ProductionOrder.create(
                UUID.randomUUID(),
                LocalDate.now(),
                ProductionPriority.NORMAL,
                null,
                null,
                "cost attribution test"
        ));
        productionOrderId = created.getId();
        moveToInProgress();
    }

    @Test
    void exposesHistoricalUnitAndTotalCostFromInventorySnapshot() {
        InventoryItem fabric = createFabric("15000.00", "100.0000");

        RegisterProductionMaterialConsumptionResult registered = register(
                fabric.getId(),
                "10.0000",
                "METER"
        );

        InventoryMovement movement = inventoryMovementRepository
                .findBySourceTypeAndSourceId(InventoryMovementSourceType.PRODUCTION, registered.consumptionId())
                .orElseThrow();
        assertEquals(new BigDecimal("15000.00"), movement.getUnitCost());
        assertEquals(new BigDecimal("150000.00"), movement.getTotalCost());

        GetProductionMaterialConsumptionsResult history = getHistory();
        assertEquals(1, history.consumptions().size());
        assertEquals(new BigDecimal("15000.00"), history.consumptions().getFirst().unitCost());
        assertEquals(new BigDecimal("150000.00"), history.consumptions().getFirst().totalCost());
        assertEquals(new BigDecimal("150000.00"), history.materialCostSummary().totalMaterialCost());
        assertEquals(1, history.materialCostSummary().valuedConsumptionCount());
        assertEquals(0, history.materialCostSummary().unvaluedConsumptionCount());
    }

    @Test
    void sumsMultipleHistoricalUnitCostsWithoutMergingPrices() {
        InventoryItem fabricA = createFabric("15000.00", "100.0000");
        InventoryItem fabricB = createFabric("18000.00", "100.0000");

        register(fabricA.getId(), "10.0000", "METER");
        register(fabricB.getId(), "5.0000", "METER");

        GetProductionMaterialConsumptionsResult history = getHistory();
        assertEquals(2, history.consumptions().size());
        assertEquals(new BigDecimal("240000.00"), history.materialCostSummary().totalMaterialCost());
        assertEquals(2, history.materialCostSummary().valuedConsumptionCount());
        assertEquals(0, history.materialCostSummary().unvaluedConsumptionCount());

        GetProductionOrderResult order = getProductionOrderUseCase.execute(
                new GetProductionOrderCommand(productionOrderId)
        );
        assertEquals(new BigDecimal("240000.00"), order.materialCostSummary().totalMaterialCost());
        assertEquals(2, order.materialCostSummary().consumptionCount());
    }

    @Test
    void changingCurrentInventoryUnitCostDoesNotChangeHistoricalProductionCost() {
        InventoryItem fabric = createFabric("15000.00", "100.0000");
        register(fabric.getId(), "10.0000", "METER");

        InventoryItem reloaded = inventoryItemRepository.findById(fabric.getId()).orElseThrow();
        reloaded.updateUnitCost(new BigDecimal("18000.00"));
        inventoryItemRepository.save(reloaded);

        assertEquals(new BigDecimal("18000.00"),
                inventoryItemRepository.findById(fabric.getId()).orElseThrow().getUnitCost());

        GetProductionMaterialConsumptionsResult history = getHistory();
        assertEquals(new BigDecimal("15000.00"), history.consumptions().getFirst().unitCost());
        assertEquals(new BigDecimal("150000.00"), history.consumptions().getFirst().totalCost());
        assertEquals(new BigDecimal("150000.00"), history.materialCostSummary().totalMaterialCost());
    }

    @Test
    void unvaluedConsumptionRemainsUnvaluedAndDoesNotInventZeroCost() {
        InventoryItem unvaluedFabric = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("PUNV-" + UUID.randomUUID().toString().substring(0, 8)),
                "Tela sin costo",
                "FABRIC",
                "METER",
                new BigDecimal("50.0000"),
                null
        ));

        RegisterProductionMaterialConsumptionResult registered = register(
                unvaluedFabric.getId(),
                "5.0000",
                "METER"
        );

        InventoryMovement movement = inventoryMovementRepository
                .findBySourceTypeAndSourceId(InventoryMovementSourceType.PRODUCTION, registered.consumptionId())
                .orElseThrow();
        assertNull(movement.getUnitCost());
        assertNull(movement.getTotalCost());
        assertEquals(new BigDecimal("45.0000"),
                inventoryItemRepository.findById(unvaluedFabric.getId()).orElseThrow().getStock());

        GetProductionMaterialConsumptionsResult history = getHistory();
        assertNull(history.consumptions().getFirst().unitCost());
        assertNull(history.consumptions().getFirst().totalCost());
        assertNull(history.materialCostSummary().totalMaterialCost());
        assertEquals(1, history.materialCostSummary().consumptionCount());
        assertEquals(0, history.materialCostSummary().valuedConsumptionCount());
        assertEquals(1, history.materialCostSummary().unvaluedConsumptionCount());
    }

    @Test
    void valuedAndUnvaluedConsumptionsAreDistinguishedInSummary() {
        InventoryItem valued = createFabric("15000.00", "100.0000");
        InventoryItem unvalued = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("PMIX-" + UUID.randomUUID().toString().substring(0, 8)),
                "Tela mixta",
                "FABRIC",
                "METER",
                new BigDecimal("50.0000"),
                null
        ));

        register(valued.getId(), "10.0000", "METER");
        register(unvalued.getId(), "5.0000", "METER");

        GetProductionMaterialConsumptionsResult history = getHistory();
        assertEquals(new BigDecimal("150000.00"), history.materialCostSummary().totalMaterialCost());
        assertEquals(2, history.materialCostSummary().consumptionCount());
        assertEquals(1, history.materialCostSummary().valuedConsumptionCount());
        assertEquals(1, history.materialCostSummary().unvaluedConsumptionCount());
    }

    @Test
    void productionConsumptionDoesNotCreateFinanceExpense() {
        InventoryItem fabric = createFabric("15000.00", "100.0000");
        long expenseCountBefore = financialTransactionRepository.findAllNewestFirst().stream()
                .filter(transaction -> transaction.getType() == FinancialTransactionType.EXPENSE)
                .count();

        RegisterProductionMaterialConsumptionResult registered = register(
                fabric.getId(),
                "10.0000",
                "METER"
        );

        assertTrue(financialTransactionRepository
                .findBySourceTypeAndSourceId(FinancialTransactionSourceType.PRODUCTION, registered.consumptionId())
                .isEmpty());
        assertTrue(financialTransactionRepository
                .findBySourceTypeAndSourceId(FinancialTransactionSourceType.PRODUCTION, productionOrderId)
                .isEmpty());

        long expenseCountAfter = financialTransactionRepository.findAllNewestFirst().stream()
                .filter(transaction -> transaction.getType() == FinancialTransactionType.EXPENSE)
                .count();
        assertEquals(expenseCountBefore, expenseCountAfter);
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

    private InventoryItem createFabric(String unitCost, String stock) {
        return inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("PCST-" + UUID.randomUUID().toString().substring(0, 8)),
                "Tela costo",
                "FABRIC",
                "METER",
                new BigDecimal(stock),
                null,
                null,
                new BigDecimal(unitCost)
        ));
    }

    private RegisterProductionMaterialConsumptionResult register(
            UUID inventoryItemId,
            String quantity,
            String unit
    ) {
        return registerProductionMaterialConsumptionUseCase.execute(
                new RegisterProductionMaterialConsumptionCommand(
                        productionOrderId,
                        inventoryItemId,
                        new BigDecimal(quantity),
                        unit,
                        null
                )
        );
    }

    private GetProductionMaterialConsumptionsResult getHistory() {
        return getProductionMaterialConsumptionsUseCase.execute(
                new GetProductionMaterialConsumptionsQuery(productionOrderId)
        );
    }
}
