package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.RegisterInventoryMovementCommand;
import com.magyen.platform.inventory.application.dto.RegisterInventoryMovementResult;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class InventoryMovementSourceUseCaseTest {

    @Autowired
    private RegisterInventoryMovementUseCase registerInventoryMovementUseCase;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    private InventoryItem inventoryItem;

    @BeforeEach
    void setUp() {
        inventoryItem = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("SRCAPP-" + UUID.randomUUID().toString().substring(0, 8)),
                "Papel",
                "PAPER",
                "METER",
                new BigDecimal("40.0000"),
                null,
                null,
                new BigDecimal("1000.00")
        ));
    }

    @Test
    void defaultsMissingSourceToManualAndPreservesCostBehavior() {
        RegisterInventoryMovementResult result = registerInventoryMovementUseCase.execute(
                new RegisterInventoryMovementCommand(
                        inventoryItem.getId(),
                        InventoryMovementType.OUT,
                        new BigDecimal("4.0000"),
                        "METER",
                        "legacy style"
                )
        );

        assertEquals(InventoryMovementSourceType.MANUAL, result.sourceType());
        assertNull(result.sourceId());
        assertEquals(new BigDecimal("1000.00"), result.unitCost());
        assertEquals(new BigDecimal("4000.00"), result.totalCost());
        assertEquals(new BigDecimal("36.0000"), result.resultingStock());

        InventoryMovement persisted = inventoryMovementRepository
                .findByInventoryItemIdOrderByMovementDateDesc(inventoryItem.getId())
                .getFirst();

        assertEquals(InventoryMovementSourceType.MANUAL, persisted.getSourceType());
        assertNull(persisted.getSourceId());
    }

    @Test
    void preservesExplicitSourceInformation() {
        UUID productionOrderId = UUID.randomUUID();

        RegisterInventoryMovementResult result = registerInventoryMovementUseCase.execute(
                new RegisterInventoryMovementCommand(
                        inventoryItem.getId(),
                        InventoryMovementType.OUT,
                        new BigDecimal("2.0000"),
                        null,
                        "future production",
                        InventoryMovementSourceType.PRODUCTION,
                        productionOrderId
                )
        );

        assertEquals(InventoryMovementSourceType.PRODUCTION, result.sourceType());
        assertEquals(productionOrderId, result.sourceId());
        assertEquals(new BigDecimal("2000.00"), result.totalCost());
    }

    @Test
    void rejectsProductionWithoutSourceId() {
        assertThrows(InventoryDomainException.class, () -> registerInventoryMovementUseCase.execute(
                new RegisterInventoryMovementCommand(
                        inventoryItem.getId(),
                        InventoryMovementType.OUT,
                        new BigDecimal("1.0000"),
                        null,
                        "invalid",
                        InventoryMovementSourceType.PRODUCTION,
                        null
                )
        ));
    }

    @Test
    void rejectsPurchaseThroughManualMovementFlow() {
        InventoryDomainException exception = assertThrows(
                InventoryDomainException.class,
                () -> registerInventoryMovementUseCase.execute(
                        new RegisterInventoryMovementCommand(
                                inventoryItem.getId(),
                                InventoryMovementType.IN,
                                new BigDecimal("1.0000"),
                                null,
                                "must use purchase flow",
                                InventoryMovementSourceType.PURCHASE,
                                UUID.randomUUID()
                        )
                )
        );

        assertTrue(exception.getMessage().contains("purchase flow"));
    }
}
