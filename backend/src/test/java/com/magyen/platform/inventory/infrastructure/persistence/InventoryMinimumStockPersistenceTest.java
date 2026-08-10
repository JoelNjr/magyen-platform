package com.magyen.platform.inventory.infrastructure.persistence;

import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMovement;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementType;
import com.magyen.platform.inventory.domain.MaterialCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class InventoryMinimumStockPersistenceTest {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsNullableMinimumStockAndKeepsExistingBaselineIntact() {
        InventoryItem created = InventoryItem.create(
                MaterialCode.of("MIN-" + UUID.randomUUID().toString().substring(0, 8)),
                "Tinta cyan",
                "INK",
                "LITER",
                new BigDecimal("3.0000"),
                null,
                "Tinta cyan"
        );
        UUID id = inventoryItemRepository.save(created).getId();

        entityManager.flush();
        entityManager.clear();

        InventoryItem reloaded = inventoryItemRepository.findById(id).orElseThrow();
        assertNull(reloaded.getMinimumStock());
        assertEquals(new BigDecimal("3.0000"), reloaded.getStock());

        reloaded.updateMinimumStock(new BigDecimal("2.0000"));
        inventoryItemRepository.save(reloaded);

        entityManager.flush();
        entityManager.clear();

        InventoryItem withThreshold = inventoryItemRepository.findById(id).orElseThrow();
        assertEquals(new BigDecimal("2.0000"), withThreshold.getMinimumStock());

        InventoryItem existing = inventoryItemRepository.findByCode(MaterialCode.of("TELA-001")).orElseThrow();
        assertEquals(0, existing.getStock().compareTo(new BigDecimal("135.2500")));
        assertEquals(0, existing.getMinimumStock().compareTo(new BigDecimal("30.0000")));
    }

    @Test
    void loadsMovementHistoryNewestFirst() {
        InventoryItem item = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("ORD-" + UUID.randomUUID().toString().substring(0, 8)),
                "Hilo",
                "THREAD",
                "ROLL",
                new BigDecimal("5.0000"),
                new BigDecimal("1.0000"),
                null
        ));

        LocalDateTime older = LocalDateTime.now().minusHours(2);
        LocalDateTime newer = LocalDateTime.now().minusHours(1);

        InventoryMovement first = item.registerMovement(
                InventoryMovementType.IN,
                new BigDecimal("1.0000"),
                null,
                "older",
                older
        );
        inventoryItemRepository.saveWithMovement(item, first);

        InventoryMovement second = item.registerMovement(
                InventoryMovementType.OUT,
                new BigDecimal("0.5000"),
                null,
                "newer",
                newer
        );
        inventoryItemRepository.saveWithMovement(item, second);

        entityManager.flush();
        entityManager.clear();

        List<InventoryMovement> movements =
                inventoryMovementRepository.findByInventoryItemIdOrderByMovementDateDesc(item.getId());

        assertEquals(2, movements.size());
        assertEquals("newer", movements.get(0).getObservation());
        assertEquals("older", movements.get(1).getObservation());
        assertTrue(movements.get(0).getMovementDate().isAfter(movements.get(1).getMovementDate()));
    }
}
