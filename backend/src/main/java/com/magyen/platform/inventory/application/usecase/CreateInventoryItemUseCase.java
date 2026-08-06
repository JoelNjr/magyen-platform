package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.MaterialCode;
import com.magyen.platform.inventory.domain.exception.InventoryDomainException;

import java.util.Objects;

/**
 * Caso de uso que coordina la creación de un nuevo material de inventario.
 */
public class CreateInventoryItemUseCase {

    private final InventoryItemRepository inventoryItemRepository;

    public CreateInventoryItemUseCase(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = Objects.requireNonNull(
                inventoryItemRepository,
                "Inventory item repository must not be null"
        );
    }

    public CreateInventoryItemResult execute(CreateInventoryItemCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        MaterialCode materialCode = MaterialCode.of(command.code());

        inventoryItemRepository.findByCode(materialCode).ifPresent(existing -> {
            throw new InventoryDomainException(
                    "An inventory item already exists with code: " + materialCode.getValue()
            );
        });

        InventoryItem inventoryItem = InventoryItem.create(
                materialCode,
                command.name(),
                command.category(),
                command.unitOfMeasure(),
                command.stock(),
                command.minimumStock()
        );

        InventoryItem savedInventoryItem = inventoryItemRepository.save(inventoryItem);

        return toResult(savedInventoryItem);
    }

    private void validateCommand(CreateInventoryItemCommand command) {
        Objects.requireNonNull(command.code(), "Code must not be null");
        Objects.requireNonNull(command.name(), "Name must not be null");
        Objects.requireNonNull(command.category(), "Category must not be null");
        Objects.requireNonNull(command.unitOfMeasure(), "Unit of measure must not be null");
        Objects.requireNonNull(command.stock(), "Stock must not be null");
        Objects.requireNonNull(command.minimumStock(), "Minimum stock must not be null");

        if (command.code().isBlank()) {
            throw new IllegalArgumentException("Code must not be blank");
        }
        if (command.name().isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        if (command.category().isBlank()) {
            throw new IllegalArgumentException("Category must not be blank");
        }
        if (command.unitOfMeasure().isBlank()) {
            throw new IllegalArgumentException("Unit of measure must not be blank");
        }
    }

    private CreateInventoryItemResult toResult(InventoryItem inventoryItem) {
        return new CreateInventoryItemResult(
                inventoryItem.getId(),
                inventoryItem.getMaterialCode().getValue(),
                inventoryItem.getName(),
                inventoryItem.getCategory(),
                inventoryItem.getUnitOfMeasure(),
                inventoryItem.getStock(),
                inventoryItem.getMinimumStock(),
                inventoryItem.getStatus()
        );
    }
}
