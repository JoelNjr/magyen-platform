package com.magyen.platform.inventory.infrastructure.persistence.mapper;

import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.MaterialCode;
import com.magyen.platform.inventory.infrastructure.persistence.entity.InventoryItemEntity;

import java.util.Objects;

/**
 * Convierte entre el agregado de dominio {@link InventoryItem} y su modelo JPA.
 * <p>
 * No contiene reglas de negocio ni accede a la base de datos.
 */
public class InventoryPersistenceMapper {

    public InventoryItemEntity toEntity(InventoryItem inventoryItem) {
        Objects.requireNonNull(inventoryItem, "Inventory item must not be null");

        InventoryItemEntity inventoryItemEntity = new InventoryItemEntity();
        inventoryItemEntity.setId(inventoryItem.getId());
        inventoryItemEntity.setMaterialCode(inventoryItem.getMaterialCode().getValue());
        inventoryItemEntity.setName(inventoryItem.getName());
        inventoryItemEntity.setCategory(inventoryItem.getCategory());
        inventoryItemEntity.setUnitOfMeasure(inventoryItem.getUnitOfMeasure());
        inventoryItemEntity.setStock(inventoryItem.getStock());
        inventoryItemEntity.setMinimumStock(inventoryItem.getMinimumStock());
        inventoryItemEntity.setStatus(inventoryItem.getStatus());
        return inventoryItemEntity;
    }

    public InventoryItem toDomain(InventoryItemEntity inventoryItemEntity) {
        Objects.requireNonNull(inventoryItemEntity, "Inventory item entity must not be null");

        return InventoryItem.reconstitute(
                inventoryItemEntity.getId(),
                MaterialCode.of(inventoryItemEntity.getMaterialCode()),
                inventoryItemEntity.getName(),
                inventoryItemEntity.getCategory(),
                inventoryItemEntity.getUnitOfMeasure(),
                inventoryItemEntity.getStock(),
                inventoryItemEntity.getMinimumStock(),
                inventoryItemEntity.getStatus()
        );
    }
}
