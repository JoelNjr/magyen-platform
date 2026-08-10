package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.ConsumeInventoryMaterialCommand;
import com.magyen.platform.inventory.application.dto.ConsumeInventoryMaterialResult;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMovement;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.InventoryMovementType;
import com.magyen.platform.inventory.domain.MaterialCode;
import com.magyen.platform.inventory.domain.exception.InventoryDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ConsumeInventoryMaterialUseCaseTest {

    @Autowired
    private ConsumeInventoryMaterialUseCase consumeInventoryMaterialUseCase;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    private InventoryItem inventoryItem;

    @BeforeEach
    void setUp() {
        inventoryItem = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("CNS-" + UUID.randomUUID().toString().substring(0, 8)),
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
    void createsProductionOutWithCostSnapshot() {
        UUID consumptionId = UUID.randomUUID();

        ConsumeInventoryMaterialResult result = consumeInventoryMaterialUseCase.execute(
                new ConsumeInventoryMaterialCommand(
                        inventoryItem.getId(),
                        new BigDecimal("18.7000"),
                        "METER",
                        InventoryMovementSourceType.PRODUCTION,
                        consumptionId,
                        "production consume"
                )
        );

        assertFalse(result.alreadyProcessed());
        assertEquals(InventoryMovementType.OUT, result.movementType());
        assertEquals(new BigDecimal("116.5500"), result.resultingStock());
        assertEquals(new BigDecimal("18000.00"), result.unitCost());
        assertEquals(new BigDecimal("336600.00"), result.totalCost());
        assertEquals(InventoryMovementSourceType.PRODUCTION, result.sourceType());
        assertEquals(consumptionId, result.sourceId());

        InventoryItem reloaded = inventoryItemRepository.findById(inventoryItem.getId()).orElseThrow();
        assertEquals(new BigDecimal("116.5500"), reloaded.getStock());
    }

    @Test
    void isIdempotentForSameSourceAndDoesNotDoubleDeduct() {
        UUID consumptionId = UUID.randomUUID();
        ConsumeInventoryMaterialCommand command = new ConsumeInventoryMaterialCommand(
                inventoryItem.getId(),
                new BigDecimal("10.0000"),
                "METER",
                InventoryMovementSourceType.PRODUCTION,
                consumptionId,
                "once"
        );

        ConsumeInventoryMaterialResult first = consumeInventoryMaterialUseCase.execute(command);
        ConsumeInventoryMaterialResult second = consumeInventoryMaterialUseCase.execute(command);

        assertFalse(first.alreadyProcessed());
        assertTrue(second.alreadyProcessed());
        assertEquals(first.movementId(), second.movementId());
        assertEquals(new BigDecimal("125.2500"), second.resultingStock());

        List<InventoryMovement> movements = inventoryMovementRepository
                .findByInventoryItemIdOrderByMovementDateDesc(inventoryItem.getId());
        assertEquals(1, movements.size());
        assertEquals(new BigDecimal("125.2500"), inventoryItemRepository.findById(inventoryItem.getId()).orElseThrow().getStock());
    }

    @Test
    void allowsMissingUnitCostButStillDeductsStock() {
        InventoryItem withoutCost = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("CNS0-" + UUID.randomUUID().toString().substring(0, 8)),
                "Sin costo",
                "FABRIC",
                "METER",
                new BigDecimal("20.0000"),
                null
        ));

        ConsumeInventoryMaterialResult result = consumeInventoryMaterialUseCase.execute(
                new ConsumeInventoryMaterialCommand(
                        withoutCost.getId(),
                        new BigDecimal("5.0000"),
                        "METER",
                        InventoryMovementSourceType.PRODUCTION,
                        UUID.randomUUID(),
                        null
                )
        );

        assertNull(result.unitCost());
        assertNull(result.totalCost());
        assertEquals(new BigDecimal("15.0000"), result.resultingStock());
    }

    @Test
    void rejectsInsufficientStockIncompatibleUnitAndMissingItem() {
        UUID consumptionId = UUID.randomUUID();

        assertThrows(InventoryDomainException.class, () -> consumeInventoryMaterialUseCase.execute(
                new ConsumeInventoryMaterialCommand(
                        inventoryItem.getId(),
                        new BigDecimal("200.0000"),
                        "METER",
                        InventoryMovementSourceType.PRODUCTION,
                        consumptionId,
                        null
                )
        ));

        assertThrows(InventoryDomainException.class, () -> consumeInventoryMaterialUseCase.execute(
                new ConsumeInventoryMaterialCommand(
                        inventoryItem.getId(),
                        new BigDecimal("1.0000"),
                        "ROLL",
                        InventoryMovementSourceType.PRODUCTION,
                        UUID.randomUUID(),
                        null
                )
        ));

        assertThrows(IllegalArgumentException.class, () -> consumeInventoryMaterialUseCase.execute(
                new ConsumeInventoryMaterialCommand(
                        UUID.randomUUID(),
                        new BigDecimal("1.0000"),
                        "METER",
                        InventoryMovementSourceType.PRODUCTION,
                        UUID.randomUUID(),
                        null
                )
        ));

        assertThrows(InventoryDomainException.class, () -> consumeInventoryMaterialUseCase.execute(
                new ConsumeInventoryMaterialCommand(
                        inventoryItem.getId(),
                        new BigDecimal("1.0000"),
                        "METER",
                        InventoryMovementSourceType.MANUAL,
                        UUID.randomUUID(),
                        null
                )
        ));
    }

}
