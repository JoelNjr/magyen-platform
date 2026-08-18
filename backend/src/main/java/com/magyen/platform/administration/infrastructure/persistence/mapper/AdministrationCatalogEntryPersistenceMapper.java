package com.magyen.platform.administration.infrastructure.persistence.mapper;

import com.magyen.platform.administration.domain.AdministrationCatalogEntry;
import com.magyen.platform.administration.infrastructure.persistence.entity.AdministrationCatalogEntryEntity;

import java.util.Objects;

/**
 * Convierte entre el agregado de dominio y su modelo JPA.
 */
public class AdministrationCatalogEntryPersistenceMapper {

    public AdministrationCatalogEntryEntity toEntity(AdministrationCatalogEntry catalogEntry) {
        Objects.requireNonNull(catalogEntry, "Catalog entry must not be null");

        AdministrationCatalogEntryEntity entity = new AdministrationCatalogEntryEntity();
        entity.setId(catalogEntry.getId());
        entity.setCatalogKind(catalogEntry.getKind());
        entity.setName(catalogEntry.getName());
        entity.setActive(catalogEntry.isActive());
        return entity;
    }

    public AdministrationCatalogEntry toDomain(AdministrationCatalogEntryEntity entity) {
        Objects.requireNonNull(entity, "Catalog entry entity must not be null");

        return AdministrationCatalogEntry.reconstitute(
                entity.getId(),
                entity.getCatalogKind(),
                entity.getName(),
                entity.isActive()
        );
    }
}
