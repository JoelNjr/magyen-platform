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

@SpringBootTest
@Transactional
class InventoryCostPersistenceTest {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsInventoryItemUnitCostAndMovementCostSnapshots() {
        InventoryItem created = InventoryItem.create(
                MaterialCode.of("CP-" + UUID.randomUUID().toString().substring(0, 8)),
                "Tela deportiva",
                "FABRIC",
                "METER",
                new BigDecimal("100.0000"),
                null,
                null,
                new BigDecimal("15000.00")
        );

        InventoryItem saved = inventoryItemRepository.save(created);

        InventoryMovement movement = saved.registerMovement(
                InventoryMovementType.OUT,
                new BigDecimal("20.0000"),
                null,
                "costed out",
                LocalDateTime.now()
        );
        inventoryItemRepository.saveWithMovement(saved, movement);

        entityManager.flush();
        entityManager.clear();

        InventoryItem reloadedItem = inventoryItemRepository.findById(saved.getId()).orElseThrow();
        assertEquals(new BigDecimal("15000.00"), reloadedItem.getUnitCost());
        assertEquals(new BigDecimal("80.0000"), reloadedItem.getStock());

        List<InventoryMovement> movements =
                inventoryMovementRepository.findByInventoryItemIdOrderByMovementDateDesc(saved.getId());
        assertEquals(1, movements.size());
        assertEquals(new BigDecimal("15000.00"), movements.getFirst().getUnitCost());
        assertEquals(new BigDecimal("300000.00"), movements.getFirst().getTotalCost());
    }

    @Test
    void loadsHistoricalNullCostsAndExistingBaselineWithoutFabricatedCost() {
        InventoryItem existing = inventoryItemRepository.findByCode(MaterialCode.of("TELA-001")).orElseThrow();

        assertNull(existing.getUnitCost());
        assertEquals(0, existing.getStock().compareTo(new BigDecimal("135.2500")));

        InventoryItem withoutCost = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("CPN-" + UUID.randomUUID().toString().substring(0, 8)),
                "Sin costo",
                "FABRIC",
                "METER",
                new BigDecimal("10.0000"),
                null
        ));

        InventoryMovement movement = withoutCost.registerMovement(
                InventoryMovementType.IN,
                new BigDecimal("1.0000"),
                null,
                "legacy style",
                LocalDateTime.now()
        );
        inventoryItemRepository.saveWithMovement(withoutCost, movement);

        entityManager.flush();
        entityManager.clear();

        InventoryMovement reloaded = inventoryMovementRepository
                .findByInventoryItemIdOrderByMovementDateDesc(withoutCost.getId())
                .getFirst();

        assertNull(reloaded.getUnitCost());
        assertNull(reloaded.getTotalCost());
    }

    @Test
    void changingItemUnitCostDoesNotRewritePersistedMovementSnapshot() {
        InventoryItem saved = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("CPI-" + UUID.randomUUID().toString().substring(0, 8)),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("50.0000"),
                null,
                null,
                new BigDecimal("15000.00")
        ));

        InventoryMovement movement = saved.registerMovement(
                InventoryMovementType.OUT,
                new BigDecimal("20.0000"),
                null,
                "freeze",
                LocalDateTime.now()
        );
        inventoryItemRepository.saveWithMovement(saved, movement);

        saved.updateUnitCost(new BigDecimal("18000.00"));
        inventoryItemRepository.save(saved);

        entityManager.flush();
        entityManager.clear();

        InventoryItem reloadedItem = inventoryItemRepository.findById(saved.getId()).orElseThrow();
        InventoryMovement reloadedMovement = inventoryMovementRepository
                .findByInventoryItemIdOrderByMovementDateDesc(saved.getId())
                .getFirst();

        assertEquals(new BigDecimal("18000.00"), reloadedItem.getUnitCost());
        assertEquals(new BigDecimal("15000.00"), reloadedMovement.getUnitCost());
        assertEquals(new BigDecimal("300000.00"), reloadedMovement.getTotalCost());
    }
}
