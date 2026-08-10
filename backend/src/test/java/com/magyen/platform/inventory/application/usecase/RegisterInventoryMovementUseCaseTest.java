package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.RegisterInventoryMovementCommand;
import com.magyen.platform.inventory.application.dto.RegisterInventoryMovementResult;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryItemStatus;
import com.magyen.platform.inventory.domain.InventoryMovement;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class RegisterInventoryMovementUseCaseTest {

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
                MaterialCode.of("MOV-" + UUID.randomUUID().toString().substring(0, 8)),
                "Papel sublimación",
                "PAPER",
                "METER",
                new BigDecimal("20.0000"),
                new BigDecimal("5.0000"),
                "Roll paper"
        ));
    }

    @Test
    void registersMovementAgainstExistingItemAndPersistsHistory() {
        RegisterInventoryMovementResult result = registerInventoryMovementUseCase.execute(
                new RegisterInventoryMovementCommand(
                        inventoryItem.getId(),
                        InventoryMovementType.OUT,
                        new BigDecimal("3.5000"),
                        "METER",
                        "Plotter consumption"
                )
        );

        assertEquals(InventoryMovementType.OUT, result.movementType());
        assertEquals(new BigDecimal("16.5000"), result.resultingStock());

        InventoryItem reloaded = inventoryItemRepository.findById(inventoryItem.getId()).orElseThrow();
        assertEquals(new BigDecimal("16.5000"), reloaded.getStock());

        List<InventoryMovement> movements =
                inventoryMovementRepository.findByInventoryItemIdOrderByMovementDateDesc(inventoryItem.getId());
        assertEquals(1, movements.size());
        assertEquals(result.movementId(), movements.getFirst().getId());
        assertEquals("Plotter consumption", movements.getFirst().getObservation());
    }

    @Test
    void rejectsMissingInventoryItem() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> registerInventoryMovementUseCase.execute(
                        new RegisterInventoryMovementCommand(
                                UUID.randomUUID(),
                                InventoryMovementType.IN,
                                BigDecimal.ONE,
                                null,
                                "missing"
                        )
                )
        );

        assertTrue(exception.getMessage().contains("Inventory item not found"));
    }

    @Test
    void rejectsInvalidAdjustmentThatWouldProduceNegativeStock() {
        assertThrows(InventoryDomainException.class, () -> registerInventoryMovementUseCase.execute(
                new RegisterInventoryMovementCommand(
                        inventoryItem.getId(),
                        InventoryMovementType.ADJUSTMENT,
                        new BigDecimal("-50.0000"),
                        null,
                        "bad adjustment"
                )
        ));

        InventoryItem reloaded = inventoryItemRepository.findById(inventoryItem.getId()).orElseThrow();
        assertEquals(new BigDecimal("20.0000"), reloaded.getStock());
        assertEquals(InventoryItemStatus.ACTIVE, reloaded.getStatus());
        assertTrue(inventoryMovementRepository
                .findByInventoryItemIdOrderByMovementDateDesc(inventoryItem.getId())
                .isEmpty());
    }
}
