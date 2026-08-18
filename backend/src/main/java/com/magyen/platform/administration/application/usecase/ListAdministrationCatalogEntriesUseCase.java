package com.magyen.platform.administration.application.usecase;

import com.magyen.platform.administration.application.dto.AdministrationCatalogEntryResult;
import com.magyen.platform.administration.application.dto.ListAdministrationCatalogEntriesQuery;
import com.magyen.platform.administration.domain.AdministrationCatalogEntryRepository;

import java.util.List;
import java.util.Objects;

/**
 * Lista un catálogo de Administración, opcionalmente solo valores activos.
 */
public class ListAdministrationCatalogEntriesUseCase {

    private final AdministrationCatalogEntryRepository administrationCatalogEntryRepository;

    public ListAdministrationCatalogEntriesUseCase(
            AdministrationCatalogEntryRepository administrationCatalogEntryRepository
    ) {
        this.administrationCatalogEntryRepository = Objects.requireNonNull(
                administrationCatalogEntryRepository,
                "Catalog entry repository must not be null"
        );
    }

    public List<AdministrationCatalogEntryResult> execute(ListAdministrationCatalogEntriesQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.kind(), "Catalog kind must not be null");

        return administrationCatalogEntryRepository.findByKindOrderByName(query.kind()).stream()
                .filter(entry -> !query.activeOnly() || entry.isActive())
                .map(AdministrationCatalogEntryMapper::toResult)
                .toList();
    }
}
