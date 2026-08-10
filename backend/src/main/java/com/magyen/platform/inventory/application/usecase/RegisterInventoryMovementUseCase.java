package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.RegisterInventoryMovementCommand;
import com.magyen.platform.inventory.application.dto.RegisterInventoryMovementResult;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMovement;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.InventoryUnitOfMeasure;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Caso de uso que registra un movimiento histórico y actualiza el stock del material.
 */
public class RegisterInventoryMovementUseCase {

    private final InventoryItemRepository inventoryItemRepository;

    public RegisterInventoryMovementUseCase(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = Objects.requireNonNull(
                inventoryItemRepository,
                "Inventory item repository must not be null"
        );
    }

    public RegisterInventoryMovementResult execute(RegisterInventoryMovementCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        InventoryItem inventoryItem = inventoryItemRepository.findById(command.inventoryItemId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventory item not found: " + command.inventoryItemId()
                ));

        InventoryUnitOfMeasure movementUnit = command.unitOfMeasure() == null || command.unitOfMeasure().isBlank()
                ? inventoryItem.getUnitOfMeasureValue()
                : InventoryUnitOfMeasure.of(command.unitOfMeasure());

        InventoryMovementSourceType sourceType = command.sourceType() == null
                ? InventoryMovementSourceType.MANUAL
                : command.sourceType();

        InventoryMovement movement = inventoryItem.registerMovement(
                command.movementType(),
                command.quantity(),
                movementUnit,
                command.observation(),
                LocalDateTime.now(),
                sourceType,
                command.sourceId()
        );

        inventoryItemRepository.saveWithMovement(inventoryItem, movement);

        return new RegisterInventoryMovementResult(
                movement.getId(),
                movement.getInventoryItemId(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getUnitOfMeasure().getCode(),
                movement.getResultingStock(),
                movement.getMovementDate(),
                movement.getObservation(),
                movement.getUnitCost(),
                movement.getTotalCost(),
                movement.getSourceType(),
                movement.getSourceId()
        );
    }

    private void validateCommand(RegisterInventoryMovementCommand command) {
        Objects.requireNonNull(command.inventoryItemId(), "Inventory item id must not be null");
        Objects.requireNonNull(command.movementType(), "Movement type must not be null");
        Objects.requireNonNull(command.quantity(), "Quantity must not be null");
    }
}
