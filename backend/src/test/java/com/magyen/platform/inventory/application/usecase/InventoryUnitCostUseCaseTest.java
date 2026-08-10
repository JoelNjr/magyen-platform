package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.dto.GetInventoryItemResult;
import com.magyen.platform.inventory.application.dto.RegisterInventoryMovementCommand;
import com.magyen.platform.inventory.application.dto.RegisterInventoryMovementResult;
import com.magyen.platform.inventory.application.dto.UpdateInventoryUnitCostCommand;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMovement;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementType;
import com.magyen.platform.inventory.domain.MaterialCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Transactional
class InventoryUnitCostUseCaseTest {

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Autowired
    private UpdateInventoryUnitCostUseCase updateInventoryUnitCostUseCase;

    @Autowired
    private RegisterInventoryMovementUseCase registerInventoryMovementUseCase;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Test
    void createsInventoryWithOptionalUnitCost() {
        String code = "UC-" + UUID.randomUUID().toString().substring(0, 8);

        CreateInventoryItemResult result = createInventoryItemUseCase.execute(
                new CreateInventoryItemCommand(
                        code,
                        "Tela deportiva",
                        "FABRIC",
                        "METER",
                        new BigDecimal("100.0000"),
                        null,
                        null,
                        new BigDecimal("15000.00")
                )
        );

        assertEquals(new BigDecimal("15000.00"), result.unitCost());

        InventoryItem reloaded = inventoryItemRepository.findById(result.inventoryItemId()).orElseThrow();
        assertEquals(new BigDecimal("15000.00"), reloaded.getUnitCost());
        assertEquals(new BigDecimal("100.0000"), reloaded.getStock());
    }

    @Test
    void updatesUnitCostWithoutChangingStockOrCreatingMovement() {
        InventoryItem inventoryItem = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("UC2-" + UUID.randomUUID().toString().substring(0, 8)),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("40.0000"),
                new BigDecimal("10.0000"),
                null,
                new BigDecimal("10000.00")
        ));

        GetInventoryItemResult result = updateInventoryUnitCostUseCase.execute(
                new UpdateInventoryUnitCostCommand(inventoryItem.getId(), new BigDecimal("18000.00"))
        );

        assertEquals(new BigDecimal("18000.00"), result.unitCost());
        assertEquals(new BigDecimal("40.0000"), result.stock());
        assertEquals(new BigDecimal("10.0000"), result.minimumStock());

        InventoryItem reloaded = inventoryItemRepository.findById(inventoryItem.getId()).orElseThrow();
        assertEquals(new BigDecimal("18000.00"), reloaded.getUnitCost());
        assertEquals(new BigDecimal("40.0000"), reloaded.getStock());
        assertEquals(
                0,
                inventoryMovementRepository
                        .findByInventoryItemIdOrderByMovementDateDesc(inventoryItem.getId())
                        .size()
        );
    }

    @Test
    void movementUsesCurrentUnitCostAndPreservesHistoricalSnapshot() {
        InventoryItem inventoryItem = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("UC3-" + UUID.randomUUID().toString().substring(0, 8)),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("100.0000"),
                null,
                null,
                new BigDecimal("15000.00")
        ));

        RegisterInventoryMovementResult first = registerInventoryMovementUseCase.execute(
                new RegisterInventoryMovementCommand(
                        inventoryItem.getId(),
                        InventoryMovementType.OUT,
                        new BigDecimal("20.0000"),
                        "METER",
                        "first out"
                )
        );

        assertEquals(new BigDecimal("15000.00"), first.unitCost());
        assertEquals(new BigDecimal("300000.00"), first.totalCost());

        updateInventoryUnitCostUseCase.execute(
                new UpdateInventoryUnitCostCommand(inventoryItem.getId(), new BigDecimal("18000.00"))
        );

        RegisterInventoryMovementResult second = registerInventoryMovementUseCase.execute(
                new RegisterInventoryMovementCommand(
                        inventoryItem.getId(),
                        InventoryMovementType.OUT,
                        new BigDecimal("10.0000"),
                        null,
                        "second out"
                )
        );

        assertEquals(new BigDecimal("18000.00"), second.unitCost());
        assertEquals(new BigDecimal("180000.00"), second.totalCost());

        List<InventoryMovement> movements = inventoryMovementRepository
                .findByInventoryItemIdOrderByMovementDateDesc(inventoryItem.getId());

        InventoryMovement historical = movements.stream()
                .filter(movement -> movement.getId().equals(first.movementId()))
                .findFirst()
                .orElseThrow();

        assertEquals(new BigDecimal("15000.00"), historical.getUnitCost());
        assertEquals(new BigDecimal("300000.00"), historical.getTotalCost());
    }

    @Test
    void movementWithoutConfiguredCostRemainsValidWithNullCosts() {
        InventoryItem inventoryItem = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("UC4-" + UUID.randomUUID().toString().substring(0, 8)),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("30.0000"),
                null
        ));

        RegisterInventoryMovementResult result = registerInventoryMovementUseCase.execute(
                new RegisterInventoryMovementCommand(
                        inventoryItem.getId(),
                        InventoryMovementType.OUT,
                        new BigDecimal("5.0000"),
                        null,
                        "no cost"
                )
        );

        assertNull(result.unitCost());
        assertNull(result.totalCost());
        assertEquals(new BigDecimal("25.0000"), result.resultingStock());
    }
}
