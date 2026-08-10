package com.magyen.platform.inventory.infrastructure.persistence;

import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMovement;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.InventoryMovementType;
import com.magyen.platform.inventory.domain.MaterialCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Transactional
class InventoryMovementSourcePersistenceTest {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsSourceTypeAndNullableSourceId() {
        InventoryItem saved = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("SRCP-" + UUID.randomUUID().toString().substring(0, 8)),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("20.0000"),
                null
        ));

        InventoryMovement manual = saved.registerMovement(
                InventoryMovementType.OUT,
                new BigDecimal("1.0000"),
                null,
                "manual",
                LocalDateTime.now(),
                InventoryMovementSourceType.MANUAL,
                null
        );
        inventoryItemRepository.saveWithMovement(saved, manual);

        UUID productionOrderId = UUID.randomUUID();
        InventoryMovement production = saved.registerMovement(
                InventoryMovementType.OUT,
                new BigDecimal("2.0000"),
                null,
                "production",
                LocalDateTime.now(),
                InventoryMovementSourceType.PRODUCTION,
                productionOrderId
        );
        inventoryItemRepository.saveWithMovement(saved, production);

        entityManager.flush();
        entityManager.clear();

        InventoryMovement reloadedManual = inventoryMovementRepository
                .findByInventoryItemIdOrderByMovementDateDesc(saved.getId())
                .stream()
                .filter(movement -> movement.getId().equals(manual.getId()))
                .findFirst()
                .orElseThrow();

        InventoryMovement reloadedProduction = inventoryMovementRepository
                .findByInventoryItemIdOrderByMovementDateDesc(saved.getId())
                .stream()
                .filter(movement -> movement.getId().equals(production.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(InventoryMovementSourceType.MANUAL, reloadedManual.getSourceType());
        assertNull(reloadedManual.getSourceId());
        assertEquals(InventoryMovementSourceType.PRODUCTION, reloadedProduction.getSourceType());
        assertEquals(productionOrderId, reloadedProduction.getSourceId());
    }

    @Test
    void loadsHistoricalMovementsWithNormalizedManualSource() {
        InventoryItem existing = inventoryItemRepository.findByCode(MaterialCode.of("TELA-001")).orElseThrow();

        inventoryMovementRepository
                .findByInventoryItemIdOrderByMovementDateDesc(existing.getId())
                .forEach(movement -> {
                    assertEquals(InventoryMovementSourceType.MANUAL, movement.getSourceType());
                    assertNull(movement.getSourceId());
                });
    }
}
