package com.magyen.platform.inventory.infrastructure.persistence.repository;

import com.magyen.platform.inventory.infrastructure.persistence.entity.InventoryItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para {@link InventoryItemEntity}.
 * <p>
 * Detalle técnico de infraestructura; no debe usarse fuera de esta capa.
 */
public interface SpringDataInventoryItemRepository extends JpaRepository<InventoryItemEntity, UUID> {

    Optional<InventoryItemEntity> findByMaterialCode(String materialCode);
}
