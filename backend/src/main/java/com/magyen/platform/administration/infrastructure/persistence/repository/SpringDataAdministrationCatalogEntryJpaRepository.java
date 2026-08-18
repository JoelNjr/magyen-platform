package com.magyen.platform.administration.infrastructure.persistence.repository;

import com.magyen.platform.administration.domain.AdministrationCatalogKind;
import com.magyen.platform.administration.infrastructure.persistence.entity.AdministrationCatalogEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para {@link AdministrationCatalogEntryEntity}.
 */
public interface SpringDataAdministrationCatalogEntryJpaRepository
        extends JpaRepository<AdministrationCatalogEntryEntity, UUID> {

    Optional<AdministrationCatalogEntryEntity> findByCatalogKindAndNameIgnoreCase(
            AdministrationCatalogKind catalogKind,
            String name
    );

    List<AdministrationCatalogEntryEntity> findByCatalogKindOrderByNameAsc(AdministrationCatalogKind catalogKind);

    List<AdministrationCatalogEntryEntity> findAllByOrderByCatalogKindAscNameAsc();
}
