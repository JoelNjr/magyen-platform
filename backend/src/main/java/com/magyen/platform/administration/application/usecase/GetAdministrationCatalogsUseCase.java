package com.magyen.platform.administration.application.usecase;

import com.magyen.platform.administration.application.dto.AdministrationCatalogEntryResult;
import com.magyen.platform.administration.application.dto.GetAdministrationCatalogsResult;
import com.magyen.platform.administration.domain.AdministrationCatalogEntry;
import com.magyen.platform.administration.domain.AdministrationCatalogEntryRepository;
import com.magyen.platform.administration.domain.AdministrationCatalogKind;

import java.util.List;
import java.util.Objects;

/**
 * Devuelve los cuatro catálogos de Administración (activos e inactivos).
 */
public class GetAdministrationCatalogsUseCase {

    private final AdministrationCatalogEntryRepository administrationCatalogEntryRepository;

    public GetAdministrationCatalogsUseCase(
            AdministrationCatalogEntryRepository administrationCatalogEntryRepository
    ) {
        this.administrationCatalogEntryRepository = Objects.requireNonNull(
                administrationCatalogEntryRepository,
                "Catalog entry repository must not be null"
        );
    }

    public GetAdministrationCatalogsResult execute() {
        List<AdministrationCatalogEntry> entries =
                administrationCatalogEntryRepository.findAllOrderByKindAndName();

        return new GetAdministrationCatalogsResult(
                filter(entries, AdministrationCatalogKind.GARMENT),
                filter(entries, AdministrationCatalogKind.FABRIC),
                filter(entries, AdministrationCatalogKind.COLLAR),
                filter(entries, AdministrationCatalogKind.SLEEVE)
        );
    }

    private static List<AdministrationCatalogEntryResult> filter(
            List<AdministrationCatalogEntry> entries,
            AdministrationCatalogKind kind
    ) {
        return entries.stream()
                .filter(entry -> entry.getKind() == kind)
                .map(AdministrationCatalogEntryMapper::toResult)
                .toList();
    }
}
