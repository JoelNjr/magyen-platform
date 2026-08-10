package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.GetInventoryMovementResult;
import com.magyen.platform.inventory.application.dto.GetInventoryMovementsQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryMovementsResult;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMovement;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;

import java.util.Objects;

/**
 * Caso de uso que consulta el historial de movimientos de un material.
 */
public class GetInventoryMovementsUseCase {

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public GetInventoryMovementsUseCase(
            InventoryItemRepository inventoryItemRepository,
            InventoryMovementRepository inventoryMovementRepository
    ) {
        this.inventoryItemRepository = Objects.requireNonNull(
                inventoryItemRepository,
                "Inventory item repository must not be null"
        );
        this.inventoryMovementRepository = Objects.requireNonNull(
                inventoryMovementRepository,
                "Inventory movement repository must not be null"
        );
    }

    public GetInventoryMovementsResult execute(GetInventoryMovementsQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.inventoryItemId(), "Inventory item id must not be null");

        inventoryItemRepository.findById(query.inventoryItemId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventory item not found: " + query.inventoryItemId()
                ));

        return new GetInventoryMovementsResult(
                inventoryMovementRepository
                        .findByInventoryItemIdOrderByMovementDateDesc(query.inventoryItemId())
                        .stream()
                        .map(GetInventoryMovementsUseCase::toResult)
                        .toList()
        );
    }

    private static GetInventoryMovementResult toResult(InventoryMovement movement) {
        return new GetInventoryMovementResult(
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
