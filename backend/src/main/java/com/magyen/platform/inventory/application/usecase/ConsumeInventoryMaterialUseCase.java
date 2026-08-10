package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.ConsumeInventoryMaterialCommand;
import com.magyen.platform.inventory.application.dto.ConsumeInventoryMaterialResult;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMovement;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.InventoryMovementType;
import com.magyen.platform.inventory.domain.InventoryUnitOfMeasure;
import com.magyen.platform.inventory.domain.exception.InventoryDomainException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Consume material de inventario mediante un movimiento {@code OUT} con origen auditable.
 * <p>
 * Es idempotente por ({@code sourceType}, {@code sourceId}): un mismo origen no puede
 * generar dos salidas ni doble descuento de stock. El índice único parcial en base de datos
 * es la garantía final ante condiciones de carrera.
 */
public class ConsumeInventoryMaterialUseCase {

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public ConsumeInventoryMaterialUseCase(
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

    @Transactional
    public ConsumeInventoryMaterialResult execute(ConsumeInventoryMaterialCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        InventoryMovementSourceType sourceType = command.sourceType();
        if (!sourceType.requiresSourceId()) {
            throw new InventoryDomainException(
                    "Consume inventory material requires a non-manual source type"
            );
        }

        return inventoryMovementRepository
                .findBySourceTypeAndSourceId(sourceType, command.sourceId())
                .map(existing -> toResult(existing, true))
                .orElseGet(() -> createOutMovement(command));
    }

    private ConsumeInventoryMaterialResult createOutMovement(ConsumeInventoryMaterialCommand command) {
        InventoryItem inventoryItem = inventoryItemRepository.findById(command.inventoryItemId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventory item not found: " + command.inventoryItemId()
                ));

        InventoryUnitOfMeasure movementUnit = InventoryUnitOfMeasure.of(command.unitOfMeasure());
        if (!inventoryItem.getUnitOfMeasureValue().isCompatibleWith(movementUnit)) {
            throw new InventoryDomainException(
                    "Incompatible unit of measure. Expected: " + inventoryItem.getUnitOfMeasure()
                            + ", provided: " + movementUnit.getCode()
            );
        }

        try {
            InventoryMovement movement = inventoryItem.registerMovement(
                    InventoryMovementType.OUT,
                    command.quantity(),
                    movementUnit,
                    command.observation(),
                    LocalDateTime.now(),
                    command.sourceType(),
                    command.sourceId()
            );

            inventoryItemRepository.saveWithMovement(inventoryItem, movement);
            return toResult(movement, false);
        } catch (DataIntegrityViolationException exception) {
            return inventoryMovementRepository
                    .findBySourceTypeAndSourceId(command.sourceType(), command.sourceId())
                    .map(existing -> toResult(existing, true))
                    .orElseThrow(() -> exception);
        }
    }

    private void validateCommand(ConsumeInventoryMaterialCommand command) {
        Objects.requireNonNull(command.inventoryItemId(), "Inventory item id must not be null");
        Objects.requireNonNull(command.quantity(), "Quantity must not be null");
        Objects.requireNonNull(command.unitOfMeasure(), "Unit of measure must not be null");
        Objects.requireNonNull(command.sourceType(), "Source type must not be null");
        Objects.requireNonNull(command.sourceId(), "Source id must not be null");

        if (command.unitOfMeasure().isBlank()) {
            throw new InventoryDomainException("Unit of measure must not be blank");
        }
    }

    private static ConsumeInventoryMaterialResult toResult(InventoryMovement movement, boolean alreadyProcessed) {
        return new ConsumeInventoryMaterialResult(
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
                movement.getSourceId(),
                alreadyProcessed
        );
    }
}
