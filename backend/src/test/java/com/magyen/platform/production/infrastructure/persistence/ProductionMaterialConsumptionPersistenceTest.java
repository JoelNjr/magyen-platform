package com.magyen.platform.production.infrastructure.persistence;

import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.MaterialCode;
import com.magyen.platform.production.application.dto.PlanProductionOrderCommand;
import com.magyen.platform.production.application.dto.RegisterProductionMaterialConsumptionCommand;
import com.magyen.platform.production.application.dto.StartProductionOrderCommand;
import com.magyen.platform.production.application.usecase.PlanProductionOrderUseCase;
import com.magyen.platform.production.application.usecase.RegisterProductionMaterialConsumptionUseCase;
import com.magyen.platform.production.application.usecase.StartProductionOrderUseCase;
import com.magyen.platform.production.domain.ProductionMaterialConsumption;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionPriority;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ProductionMaterialConsumptionPersistenceTest {

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Autowired
    private PlanProductionOrderUseCase planProductionOrderUseCase;

    @Autowired
    private StartProductionOrderUseCase startProductionOrderUseCase;

    @Autowired
    private RegisterProductionMaterialConsumptionUseCase registerProductionMaterialConsumptionUseCase;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsMultipleConsumptionsWithStableIds() {
        ProductionOrder created = productionOrderRepository.save(ProductionOrder.create(
                UUID.randomUUID(),
                LocalDate.now(),
                ProductionPriority.NORMAL,
                null,
                null,
                null
        ));
        UUID productionOrderId = created.getId();

        planProductionOrderUseCase.execute(new PlanProductionOrderCommand(
                productionOrderId,
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                ProductionPriority.NORMAL
        ));
        startProductionOrderUseCase.execute(new StartProductionOrderCommand(productionOrderId, null));

        InventoryItem fabric = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("PMF-" + UUID.randomUUID().toString().substring(0, 8)),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("50.0000"),
                null
        ));
        InventoryItem thread = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("PMT-" + UUID.randomUUID().toString().substring(0, 8)),
                "Hilo",
                "THREAD",
                "ROLL",
                new BigDecimal("8.0000"),
                null
        ));

        UUID firstId = registerProductionMaterialConsumptionUseCase.execute(
                new RegisterProductionMaterialConsumptionCommand(
                        productionOrderId,
                        fabric.getId(),
                        new BigDecimal("18.7000"),
                        "METER",
                        "first"
                )
        ).consumptionId();

        UUID secondId = registerProductionMaterialConsumptionUseCase.execute(
                new RegisterProductionMaterialConsumptionCommand(
                        productionOrderId,
                        thread.getId(),
                        new BigDecimal("2.0000"),
                        "ROLL",
                        "second"
                )
        ).consumptionId();

        entityManager.flush();
        entityManager.clear();

        ProductionOrder reloaded = productionOrderRepository.findById(productionOrderId).orElseThrow();
        List<ProductionMaterialConsumption> consumptions = reloaded.getMaterialConsumptions();

        assertEquals(2, consumptions.size());
        assertTrue(consumptions.stream().anyMatch(consumption -> consumption.getId().equals(firstId)));
        assertTrue(consumptions.stream().anyMatch(consumption -> consumption.getId().equals(secondId)));

        ProductionMaterialConsumption fabricConsumption = consumptions.stream()
                .filter(consumption -> consumption.getId().equals(firstId))
                .findFirst()
                .orElseThrow();

        assertEquals(fabric.getId(), fabricConsumption.getInventoryItemId());
        assertEquals(new BigDecimal("18.7000"), fabricConsumption.getQuantity());
        assertEquals("METER", fabricConsumption.getUnitOfMeasure().name());
        assertNotNull(fabricConsumption.getConsumptionDate());
        assertEquals("first", fabricConsumption.getObservation());
        assertEquals(new BigDecimal("31.3000"), inventoryItemRepository.findById(fabric.getId()).orElseThrow().getStock());
    }
}
