package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.DeactivateInventoryMaterialCommand;
import com.magyen.platform.inventory.application.dto.DeactivateInventoryMaterialResult;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryItemStatus;
import com.magyen.platform.inventory.domain.MaterialCode;
import com.magyen.platform.inventory.domain.exception.InventoryDomainException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Desactiva todas las unidades físicas de un {@code materialCode}.
 * <p>
 * No elimina filas ni toca compras, movimientos, finanzas, producción o Plotter.
 */
public class DeactivateInventoryMaterialUseCase {

    private final InventoryItemRepository inventoryItemRepository;

    public DeactivateInventoryMaterialUseCase(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = Objects.requireNonNull(
                inventoryItemRepository,
                "Inventory item repository must not be null"
        );
    }

    @Transactional
    public DeactivateInventoryMaterialResult execute(DeactivateInventoryMaterialCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        if (command.materialCode() == null || command.materialCode().isBlank()) {
            throw new InventoryDomainException("Material code must not be blank");
        }

        MaterialCode materialCode = MaterialCode.of(command.materialCode());
        List<InventoryItem> units = inventoryItemRepository.findAllByMaterialCode(materialCode);
        if (units.isEmpty()) {
            throw new IllegalArgumentException("Inventory material not found: " + materialCode.getValue());
        }

        BigDecimal aggregatedStock = units.stream()
                .map(InventoryItem::getStock)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (aggregatedStock.compareTo(BigDecimal.ZERO) > 0) {
            throw new InventoryDomainException(
                    "Cannot deactivate a material with remaining physical stock"
            );
        }

        for (InventoryItem unit : units) {
            unit.deactivate();
            inventoryItemRepository.save(unit);
        }

        return new DeactivateInventoryMaterialResult(
                materialCode.getValue(),
                InventoryItemStatus.INACTIVE,
                units.size(),
                aggregatedStock
        );
    }
}
