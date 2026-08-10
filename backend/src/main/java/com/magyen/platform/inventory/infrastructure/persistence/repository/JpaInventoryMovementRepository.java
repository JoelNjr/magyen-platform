package com.magyen.platform.inventory.infrastructure.persistence.repository;

import com.magyen.platform.inventory.domain.InventoryMovement;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.infrastructure.persistence.entity.InventoryItemEntity;
import com.magyen.platform.inventory.infrastructure.persistence.entity.InventoryMovementEntity;
import com.magyen.platform.inventory.infrastructure.persistence.mapper.InventoryPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa el port {@link InventoryMovementRepository}.
 */
@Repository
public class JpaInventoryMovementRepository implements InventoryMovementRepository {

    private final SpringDataInventoryMovementRepository springDataInventoryMovementRepository;
    private final SpringDataInventoryItemRepository springDataInventoryItemRepository;
    private final InventoryPersistenceMapper inventoryPersistenceMapper;

    public JpaInventoryMovementRepository(
            SpringDataInventoryMovementRepository springDataInventoryMovementRepository,
            SpringDataInventoryItemRepository springDataInventoryItemRepository,
            InventoryPersistenceMapper inventoryPersistenceMapper
    ) {
        this.springDataInventoryMovementRepository = Objects.requireNonNull(
                springDataInventoryMovementRepository,
                "Spring Data Inventory Movement repository must not be null"
        );
        this.springDataInventoryItemRepository = Objects.requireNonNull(
                springDataInventoryItemRepository,
                "Spring Data Inventory Item repository must not be null"
        );
        this.inventoryPersistenceMapper = Objects.requireNonNull(
                inventoryPersistenceMapper,
                "Inventory persistence mapper must not be null"
        );
    }

    @Override
    public InventoryMovement save(InventoryMovement inventoryMovement) {
        Objects.requireNonNull(inventoryMovement, "Inventory movement must not be null");

        InventoryItemEntity inventoryItemEntity = springDataInventoryItemRepository
                .findById(inventoryMovement.getInventoryItemId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventory item not found: " + inventoryMovement.getInventoryItemId()
                ));

        InventoryMovementEntity inventoryMovementEntity = inventoryPersistenceMapper.toEntity(
                inventoryMovement,
                inventoryItemEntity
        );
        InventoryMovementEntity savedEntity = springDataInventoryMovementRepository.save(inventoryMovementEntity);
        return inventoryPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<InventoryMovement> findById(UUID id) {
        Objects.requireNonNull(id, "Inventory movement id must not be null");

        return springDataInventoryMovementRepository.findById(id)
                .map(inventoryPersistenceMapper::toDomain);
    }

    @Override
    public List<InventoryMovement> findByInventoryItemIdOrderByMovementDateDesc(UUID inventoryItemId) {
        Objects.requireNonNull(inventoryItemId, "Inventory item id must not be null");

        return springDataInventoryMovementRepository
                .findByInventoryItemIdOrderByMovementDateDescIdDesc(inventoryItemId)
                .stream()
                .map(inventoryPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<InventoryMovement> findBySourceTypeAndSourceId(
            InventoryMovementSourceType sourceType,
            UUID sourceId
    ) {
        Objects.requireNonNull(sourceType, "Source type must not be null");
        Objects.requireNonNull(sourceId, "Source id must not be null");

        return springDataInventoryMovementRepository
                .findBySourceTypeAndSourceId(sourceType, sourceId)
                .map(inventoryPersistenceMapper::toDomain);
    }
}
