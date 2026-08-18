package com.magyen.platform.inventory.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistencia para el agregado {@link InventoryItem}.
 * <p>
 * La implementación concreta vivirá en la capa de infraestructura.
 */
public interface InventoryItemRepository {

    InventoryItem save(InventoryItem inventoryItem);

    /**
     * Persiste el estado de stock del material junto con un movimiento histórico nuevo.
     */
    InventoryItem saveWithMovement(InventoryItem inventoryItem, InventoryMovement inventoryMovement);

    Optional<InventoryItem> findById(UUID id);

    Optional<InventoryItem> findByCode(MaterialCode materialCode);

    Optional<InventoryItem> findFirstByMaterialType(InventoryMaterialType materialType);

    boolean existsNonPaperWithCode(MaterialCode materialCode);

    List<InventoryItem> findAll();
}
