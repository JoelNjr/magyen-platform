package com.magyen.platform.inventory.infrastructure.persistence;

import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryItemStatus;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Transactional
class InventoryMovementPersistenceTest {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsInventoryItemDescriptionUnitAndMovementRelationship() {
        InventoryItem created = InventoryItem.create(
                MaterialCode.of("PER-" + UUID.randomUUID().toString().substring(0, 8)),
                "Tinta cyan",
                "INK",
                "LITER",
                new BigDecimal("4.0000"),
                new BigDecimal("1.0000"),
                "Tinta cyan sublimación"
        );

        InventoryItem saved = inventoryItemRepository.save(created);
        UUID inventoryItemId = saved.getId();

        InventoryMovement movement = saved.registerMovement(
                InventoryMovementType.IN,
                new BigDecimal("1.2500"),
                null,
                "Supplier delivery",
                java.time.LocalDateTime.now()
        );
        inventoryItemRepository.saveWithMovement(saved, movement);

        entityManager.flush();
        entityManager.clear();

        InventoryItem reloadedItem = inventoryItemRepository.findById(inventoryItemId).orElseThrow();
        assertEquals("Tinta cyan sublimación", reloadedItem.getDescription());
        assertEquals("LITER", reloadedItem.getUnitOfMeasure());
        assertEquals(new BigDecimal("5.2500"), reloadedItem.getStock());
        assertEquals(InventoryItemStatus.ACTIVE, reloadedItem.getStatus());

        List<InventoryMovement> movements =
                inventoryMovementRepository.findByInventoryItemIdOrderByMovementDateDesc(inventoryItemId);
        assertEquals(1, movements.size());

        InventoryMovement reloadedMovement = movements.getFirst();
        assertEquals(movement.getId(), reloadedMovement.getId());
        assertEquals(inventoryItemId, reloadedMovement.getInventoryItemId());
        assertEquals(InventoryMovementType.IN, reloadedMovement.getMovementType());
        assertEquals(new BigDecimal("1.2500"), reloadedMovement.getQuantity());
        assertEquals("LITER", reloadedMovement.getUnitOfMeasure().getCode());
        assertEquals(new BigDecimal("5.2500"), reloadedMovement.getResultingStock());
        assertEquals("Supplier delivery", reloadedMovement.getObservation());
        assertNotNull(reloadedMovement.getMovementDate());
    }

    @Test
    void loadsExistingInventoryBaselineWithoutFabricatedCost() {
        InventoryItem existing = inventoryItemRepository.findByCode(MaterialCode.of("TELA-001")).orElseThrow();

        assertEquals("Tela Sudáfrica Blanca", existing.getName());
        assertEquals("METER", existing.getUnitOfMeasure());
        assertEquals(0, existing.getStock().compareTo(new BigDecimal("135.2500")));
        assertNull(existing.getUnitCost());
    }
}
