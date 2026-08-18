package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.ConsumeInventoryMaterialCommand;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.MaterialCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica que el índice único (source_type, source_id) impide doble descuento concurrente.
 */
@SpringBootTest
class ConsumeInventoryMaterialConcurrencyTest {

    @Autowired
    private ConsumeInventoryMaterialUseCase consumeInventoryMaterialUseCase;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID createdInventoryItemId;
    private UUID consumptionId;

    @AfterEach
    void deleteSyntheticRaceRows() {
        if (consumptionId != null) {
            jdbcTemplate.update(
                    "DELETE FROM inventory_movements WHERE source_type = ? AND source_id = ?",
                    InventoryMovementSourceType.PRODUCTION.name(),
                    consumptionId
            );
        }
        if (createdInventoryItemId != null) {
            jdbcTemplate.update("DELETE FROM inventory_movements WHERE inventory_item_id = ?", createdInventoryItemId);
            jdbcTemplate.update("DELETE FROM inventory_items WHERE id = ?", createdInventoryItemId);
        }
    }

    @Test
    void concurrentDuplicateSourceCreatesOnlyOneMovement() throws Exception {
        InventoryItem inventoryItem = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("CNR-" + UUID.randomUUID().toString().substring(0, 8)),
                "Tela race",
                "FABRIC",
                "METER",
                new BigDecimal("50.0000"),
                null,
                null,
                new BigDecimal("1000.00")
        ));
        createdInventoryItemId = inventoryItem.getId();

        consumptionId = UUID.randomUUID();
        ConsumeInventoryMaterialCommand command = new ConsumeInventoryMaterialCommand(
                inventoryItem.getId(),
                new BigDecimal("1.0000"),
                "METER",
                InventoryMovementSourceType.PRODUCTION,
                consumptionId,
                "race"
        );

        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int index = 0; index < threads; index++) {
            tasks.add(() -> {
                try {
                    consumeInventoryMaterialUseCase.execute(command);
                    return true;
                } catch (RuntimeException exception) {
                    return false;
                }
            });
        }

        try {
            List<Future<Boolean>> futures = executor.invokeAll(tasks, 30, TimeUnit.SECONDS);
            long successes = futures.stream().filter(future -> {
                try {
                    return Boolean.TRUE.equals(future.get());
                } catch (Exception exception) {
                    return false;
                }
            }).count();

            assertTrue(successes >= 1);
            assertEquals(1, inventoryMovementRepository
                    .findByInventoryItemIdOrderByMovementDateDesc(inventoryItem.getId())
                    .size());
            assertEquals(
                    new BigDecimal("49.0000"),
                    inventoryItemRepository.findById(inventoryItem.getId()).orElseThrow().getStock()
            );
        } finally {
            executor.shutdownNow();
        }
    }
}
