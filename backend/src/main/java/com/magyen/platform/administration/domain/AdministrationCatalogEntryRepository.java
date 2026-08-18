package com.magyen.platform.administration.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistencia para {@link AdministrationCatalogEntry}.
 */
public interface AdministrationCatalogEntryRepository {

    AdministrationCatalogEntry save(AdministrationCatalogEntry catalogEntry);

    Optional<AdministrationCatalogEntry> findById(UUID id);

    Optional<AdministrationCatalogEntry> findByKindAndNameIgnoreCase(
            AdministrationCatalogKind kind,
            String name
    );

    List<AdministrationCatalogEntry> findByKindOrderByName(AdministrationCatalogKind kind);

    List<AdministrationCatalogEntry> findAllOrderByKindAndName();
}
