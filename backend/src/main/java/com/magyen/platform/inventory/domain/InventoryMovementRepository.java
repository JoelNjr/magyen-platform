package com.magyen.platform.inventory.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistencia para movimientos históricos de inventario.
 */
public interface InventoryMovementRepository {

    InventoryMovement save(InventoryMovement inventoryMovement);

    Optional<InventoryMovement> findById(UUID id);

    /**
     * Historial de movimientos de un material, ordenado por fecha descendente.
     */
    List<InventoryMovement> findByInventoryItemIdOrderByMovementDateDesc(UUID inventoryItemId);

    /**
     * Busca el movimiento asociado a un origen de negocio auditable.
     */
    Optional<InventoryMovement> findBySourceTypeAndSourceId(
            InventoryMovementSourceType sourceType,
            UUID sourceId
    );
}
