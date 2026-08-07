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

    Optional<InventoryItem> findById(UUID id);

    Optional<InventoryItem> findByCode(MaterialCode materialCode);

    List<InventoryItem> findAll();
}
