package com.magyen.platform.inventory.infrastructure.persistence.repository;

import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.infrastructure.persistence.entity.InventoryMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para {@link InventoryMovementEntity}.
 */
public interface SpringDataInventoryMovementRepository extends JpaRepository<InventoryMovementEntity, UUID> {

    List<InventoryMovementEntity> findByInventoryItemIdOrderByMovementDateDescIdDesc(UUID inventoryItemId);

    Optional<InventoryMovementEntity> findBySourceTypeAndSourceId(
            InventoryMovementSourceType sourceType,
            UUID sourceId
    );
}
