package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.GetInventoryItemQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryItemResult;
import com.magyen.platform.inventory.application.dto.GetInventoryItemsResult;
import com.magyen.platform.inventory.application.dto.GetInventoryMovementsQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryMovementsResult;
import com.magyen.platform.inventory.application.dto.RegisterInventoryMovementCommand;
import com.magyen.platform.inventory.application.dto.UpdateInventoryMinimumStockCommand;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class InventoryReadAndMinimumStockUseCaseTest {

    @Autowired
    private GetInventoryItemsUseCase getInventoryItemsUseCase;

    @Autowired
    private GetInventoryItemUseCase getInventoryItemUseCase;

    @Autowired
    private GetInventoryMovementsUseCase getInventoryMovementsUseCase;

    @Autowired
    private UpdateInventoryMinimumStockUseCase updateInventoryMinimumStockUseCase;

    @Autowired
    private RegisterInventoryMovementUseCase registerInventoryMovementUseCase;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    private InventoryItem inventoryItem;

    @BeforeEach
    void setUp() {
        inventoryItem = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("READ-" + UUID.randomUUID().toString().substring(0, 8)),
                "Papel sublimación",
                "PAPER",
                "METER",
                new BigDecimal("12.5000"),
                new BigDecimal("20.0000"),
                "Papel sublimación"
        ));
    }

    @Test
    void listsAndDetailsInventoryItemsWithLowStockFlag() {
        GetInventoryItemsResult list = getInventoryItemsUseCase.execute();
        assertTrue(list.items().stream().anyMatch(item -> item.inventoryItemId().equals(inventoryItem.getId())));

        GetInventoryItemResult detail = getInventoryItemUseCase.execute(
                new GetInventoryItemQuery(inventoryItem.getId())
        );

        assertEquals(inventoryItem.getId(), detail.inventoryItemId());
        assertEquals("Papel sublimación", detail.description());
        assertEquals(new BigDecimal("12.5000"), detail.stock());
        assertEquals(new BigDecimal("20.0000"), detail.minimumStock());
        assertTrue(detail.lowStock());
    }

    @Test
    void returnsEmptyMovementHistoryAndRejectsMissingItem() {
        GetInventoryMovementsResult emptyHistory = getInventoryMovementsUseCase.execute(
                new GetInventoryMovementsQuery(inventoryItem.getId())
        );
        assertTrue(emptyHistory.movements().isEmpty());

        assertThrows(IllegalArgumentException.class, () -> getInventoryItemUseCase.execute(
                new GetInventoryItemQuery(UUID.randomUUID())
        ));
        assertThrows(IllegalArgumentException.class, () -> getInventoryMovementsUseCase.execute(
                new GetInventoryMovementsQuery(UUID.randomUUID())
        ));
    }

    @Test
    void returnsMovementHistoryOrderedByDateDescending() {
        registerInventoryMovementUseCase.execute(new RegisterInventoryMovementCommand(
                inventoryItem.getId(),
                InventoryMovementType.IN,
                new BigDecimal("1.0000"),
                null,
                "first"
        ));
        registerInventoryMovementUseCase.execute(new RegisterInventoryMovementCommand(
                inventoryItem.getId(),
                InventoryMovementType.OUT,
                new BigDecimal("0.5000"),
                null,
                "second"
        ));

        GetInventoryMovementsResult history = getInventoryMovementsUseCase.execute(
                new GetInventoryMovementsQuery(inventoryItem.getId())
        );

        assertEquals(2, history.movements().size());
        assertEquals("second", history.movements().get(0).observation());
        assertEquals("first", history.movements().get(1).observation());
        assertTrue(history.movements().get(0).movementDate()
                .compareTo(history.movements().get(1).movementDate()) >= 0);
    }

    @Test
    void updatesMinimumStockWithoutChangingStockOrCreatingMovements() {
        GetInventoryItemResult updated = updateInventoryMinimumStockUseCase.execute(
                new UpdateInventoryMinimumStockCommand(inventoryItem.getId(), new BigDecimal("10.0000"))
        );

        assertEquals(new BigDecimal("12.5000"), updated.stock());
        assertEquals(new BigDecimal("10.0000"), updated.minimumStock());
        assertFalse(updated.lowStock());

        GetInventoryMovementsResult history = getInventoryMovementsUseCase.execute(
                new GetInventoryMovementsQuery(inventoryItem.getId())
        );
        assertTrue(history.movements().isEmpty());

        GetInventoryItemResult disabled = updateInventoryMinimumStockUseCase.execute(
                new UpdateInventoryMinimumStockCommand(inventoryItem.getId(), null)
        );
        assertEquals(null, disabled.minimumStock());
        assertFalse(disabled.lowStock());

        assertThrows(InventoryDomainException.class, () -> updateInventoryMinimumStockUseCase.execute(
                new UpdateInventoryMinimumStockCommand(inventoryItem.getId(), new BigDecimal("-1"))
        ));
    }
}
