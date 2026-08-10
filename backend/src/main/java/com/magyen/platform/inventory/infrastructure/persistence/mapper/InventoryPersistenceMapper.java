package com.magyen.platform.inventory.infrastructure.persistence.mapper;

import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryMovement;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.InventoryUnitOfMeasure;
import com.magyen.platform.inventory.domain.MaterialCode;
import com.magyen.platform.inventory.infrastructure.persistence.entity.InventoryItemEntity;
import com.magyen.platform.inventory.infrastructure.persistence.entity.InventoryMovementEntity;

import java.util.Objects;

/**
 * Convierte entre el modelo de dominio de inventario y sus entidades JPA.
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
        inventoryItemEntity.setMaterialType(inventoryItem.getMaterialType());
        inventoryItemEntity.setPaperRollNumber(inventoryItem.getPaperRollNumber());
        inventoryItemEntity.setDescription(inventoryItem.getDescription());
        inventoryItemEntity.setUnitOfMeasure(inventoryItem.getUnitOfMeasure());
        inventoryItemEntity.setStock(inventoryItem.getStock());
        inventoryItemEntity.setMinimumStock(inventoryItem.getMinimumStock());
        inventoryItemEntity.setUnitCost(inventoryItem.getUnitCost());
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
                inventoryItemEntity.getStatus(),
                inventoryItemEntity.getDescription(),
                inventoryItemEntity.getUnitCost(),
                inventoryItemEntity.getMaterialType(),
                inventoryItemEntity.getPaperRollNumber()
        );
    }

    public InventoryMovementEntity toEntity(
            InventoryMovement inventoryMovement,
            InventoryItemEntity inventoryItemEntity
    ) {
        Objects.requireNonNull(inventoryMovement, "Inventory movement must not be null");
        Objects.requireNonNull(inventoryItemEntity, "Inventory item entity must not be null");

        InventoryMovementEntity inventoryMovementEntity = new InventoryMovementEntity();
        inventoryMovementEntity.setId(inventoryMovement.getId());
        inventoryMovementEntity.setInventoryItem(inventoryItemEntity);
        inventoryMovementEntity.setMovementType(inventoryMovement.getMovementType());
        inventoryMovementEntity.setQuantity(inventoryMovement.getQuantity());
        inventoryMovementEntity.setUnitOfMeasure(inventoryMovement.getUnitOfMeasure().getCode());
        inventoryMovementEntity.setMovementDate(inventoryMovement.getMovementDate());
        inventoryMovementEntity.setObservation(inventoryMovement.getObservation());
        inventoryMovementEntity.setResultingStock(inventoryMovement.getResultingStock());
        inventoryMovementEntity.setUnitCost(inventoryMovement.getUnitCost());
        inventoryMovementEntity.setTotalCost(inventoryMovement.getTotalCost());
        inventoryMovementEntity.setSourceType(inventoryMovement.getSourceType());
        inventoryMovementEntity.setSourceId(inventoryMovement.getSourceId());
        return inventoryMovementEntity;
    }

    public InventoryMovement toDomain(InventoryMovementEntity inventoryMovementEntity) {
        Objects.requireNonNull(inventoryMovementEntity, "Inventory movement entity must not be null");
        Objects.requireNonNull(
                inventoryMovementEntity.getInventoryItem(),
                "Inventory movement must reference an inventory item"
        );

        InventoryMovementSourceType sourceType = inventoryMovementEntity.getSourceType() == null
                ? InventoryMovementSourceType.MANUAL
                : inventoryMovementEntity.getSourceType();

        return InventoryMovement.reconstitute(
                inventoryMovementEntity.getId(),
                inventoryMovementEntity.getInventoryItem().getId(),
                inventoryMovementEntity.getMovementType(),
                inventoryMovementEntity.getQuantity(),
                InventoryUnitOfMeasure.reconstitute(inventoryMovementEntity.getUnitOfMeasure()),
                inventoryMovementEntity.getMovementDate(),
                inventoryMovementEntity.getObservation(),
                inventoryMovementEntity.getResultingStock(),
                inventoryMovementEntity.getUnitCost(),
                inventoryMovementEntity.getTotalCost(),
                sourceType,
                inventoryMovementEntity.getSourceId()
        );
    }
}
