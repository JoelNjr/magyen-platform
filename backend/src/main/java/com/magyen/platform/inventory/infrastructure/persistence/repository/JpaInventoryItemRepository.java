package com.magyen.platform.inventory.infrastructure.persistence.repository;

import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMovement;
import com.magyen.platform.inventory.domain.MaterialCode;
import com.magyen.platform.inventory.infrastructure.persistence.entity.InventoryItemEntity;
import com.magyen.platform.inventory.infrastructure.persistence.entity.InventoryMovementEntity;
import com.magyen.platform.inventory.infrastructure.persistence.mapper.InventoryPersistenceMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa el port {@link InventoryItemRepository}.
 * <p>
 * Traduce entre el agregado de dominio y el modelo JPA; nunca expone entidades de persistencia.
 */
@Repository
public class JpaInventoryItemRepository implements InventoryItemRepository {

    private final SpringDataInventoryItemRepository springDataInventoryItemRepository;
    private final SpringDataInventoryMovementRepository springDataInventoryMovementRepository;
    private final InventoryPersistenceMapper inventoryPersistenceMapper;

    public JpaInventoryItemRepository(
            SpringDataInventoryItemRepository springDataInventoryItemRepository,
            SpringDataInventoryMovementRepository springDataInventoryMovementRepository,
            InventoryPersistenceMapper inventoryPersistenceMapper
    ) {
        this.springDataInventoryItemRepository = Objects.requireNonNull(
                springDataInventoryItemRepository,
                "Spring Data Inventory Item repository must not be null"
        );
        this.springDataInventoryMovementRepository = Objects.requireNonNull(
                springDataInventoryMovementRepository,
                "Spring Data Inventory Movement repository must not be null"
        );
        this.inventoryPersistenceMapper = Objects.requireNonNull(
                inventoryPersistenceMapper,
                "Inventory persistence mapper must not be null"
        );
    }

    @Override
    public InventoryItem save(InventoryItem inventoryItem) {
        Objects.requireNonNull(inventoryItem, "Inventory item must not be null");

        InventoryItemEntity inventoryItemEntity = inventoryPersistenceMapper.toEntity(inventoryItem);
        InventoryItemEntity savedInventoryItemEntity = springDataInventoryItemRepository.save(inventoryItemEntity);
        return inventoryPersistenceMapper.toDomain(savedInventoryItemEntity);
    }

    @Override
    @Transactional
    public InventoryItem saveWithMovement(InventoryItem inventoryItem, InventoryMovement inventoryMovement) {
        Objects.requireNonNull(inventoryItem, "Inventory item must not be null");
        Objects.requireNonNull(inventoryMovement, "Inventory movement must not be null");

        if (!inventoryItem.getId().equals(inventoryMovement.getInventoryItemId())) {
            throw new IllegalArgumentException(
                    "Movement inventory item id does not match the inventory item being saved"
            );
        }

        InventoryItemEntity inventoryItemEntity = inventoryPersistenceMapper.toEntity(inventoryItem);
        InventoryItemEntity savedInventoryItemEntity = springDataInventoryItemRepository.save(inventoryItemEntity);

        InventoryMovementEntity inventoryMovementEntity = inventoryPersistenceMapper.toEntity(
                inventoryMovement,
                savedInventoryItemEntity
        );
        springDataInventoryMovementRepository.save(inventoryMovementEntity);

        return inventoryPersistenceMapper.toDomain(savedInventoryItemEntity);
    }

    @Override
    public Optional<InventoryItem> findById(UUID id) {
        Objects.requireNonNull(id, "Inventory item id must not be null");

        return springDataInventoryItemRepository.findById(id)
                .map(inventoryPersistenceMapper::toDomain);
    }

    @Override
    public Optional<InventoryItem> findByCode(MaterialCode materialCode) {
        Objects.requireNonNull(materialCode, "Material code must not be null");

        return springDataInventoryItemRepository.findByMaterialCode(materialCode.getValue())
                .map(inventoryPersistenceMapper::toDomain);
    }

    @Override
    public List<InventoryItem> findAll() {
        return springDataInventoryItemRepository.findAll().stream()
                .map(inventoryPersistenceMapper::toDomain)
                .toList();
    }
}
