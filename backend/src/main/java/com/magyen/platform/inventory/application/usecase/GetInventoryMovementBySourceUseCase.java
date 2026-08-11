package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.GetInventoryMovementBySourceQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryMovementBySourceResult;
import com.magyen.platform.inventory.domain.InventoryMovement;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;

import java.util.Objects;
import java.util.Optional;

/**
 * Caso de uso que obtiene un movimiento de inventario por sourceType + sourceId.
 * <p>
 * Expone el snapshot histórico de costo sin leer el unitCost actual del ítem.
 */
public class GetInventoryMovementBySourceUseCase {

    private final InventoryMovementRepository inventoryMovementRepository;

    public GetInventoryMovementBySourceUseCase(InventoryMovementRepository inventoryMovementRepository) {
        this.inventoryMovementRepository = Objects.requireNonNull(
                inventoryMovementRepository,
                "Inventory movement repository must not be null"
        );
    }

    public Optional<GetInventoryMovementBySourceResult> execute(GetInventoryMovementBySourceQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.sourceType(), "Source type must not be null");
        Objects.requireNonNull(query.sourceId(), "Source id must not be null");

        return inventoryMovementRepository
                .findBySourceTypeAndSourceId(query.sourceType(), query.sourceId())
                .map(GetInventoryMovementBySourceUseCase::toResult);
    }

    private static GetInventoryMovementBySourceResult toResult(InventoryMovement movement) {
        return new GetInventoryMovementBySourceResult(
                movement.getId(),
                movement.getInventoryItemId(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getUnitOfMeasure().getCode(),
                movement.getMovementDate(),
                movement.getObservation(),
                movement.getResultingStock(),
                movement.getUnitCost(),
                movement.getTotalCost(),
                movement.getSourceType(),
                movement.getSourceId()
        );
    }
}
