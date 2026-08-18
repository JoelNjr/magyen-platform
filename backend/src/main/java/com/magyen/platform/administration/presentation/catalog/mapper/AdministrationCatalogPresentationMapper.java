package com.magyen.platform.administration.presentation.catalog.mapper;

import com.magyen.platform.administration.application.dto.ActivateAdministrationCatalogEntryCommand;
import com.magyen.platform.administration.application.dto.AdministrationCatalogEntryResult;
import com.magyen.platform.administration.application.dto.CreateAdministrationCatalogEntryCommand;
import com.magyen.platform.administration.application.dto.DeactivateAdministrationCatalogEntryCommand;
import com.magyen.platform.administration.application.dto.GetAdministrationCatalogsResult;
import com.magyen.platform.administration.application.dto.ListAdministrationCatalogEntriesQuery;
import com.magyen.platform.administration.domain.AdministrationCatalogKind;
import com.magyen.platform.administration.presentation.catalog.request.CreateAdministrationCatalogEntryRequest;
import com.magyen.platform.administration.presentation.catalog.response.AdministrationCatalogEntryResponse;
import com.magyen.platform.administration.presentation.catalog.response.GetAdministrationCatalogEntriesResponse;
import com.magyen.platform.administration.presentation.catalog.response.GetAdministrationCatalogsResponse;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Convierte entre HTTP y DTOs de Application para catálogos de Administración.
 */
public class AdministrationCatalogPresentationMapper {

    public AdministrationCatalogKind toKind(String catalogKindPath) {
        return AdministrationCatalogKind.of(catalogKindPath);
    }

    public CreateAdministrationCatalogEntryCommand toCreateCommand(
            String catalogKindPath,
            CreateAdministrationCatalogEntryRequest request
    ) {
        Objects.requireNonNull(request, "Create catalog request must not be null");
        return new CreateAdministrationCatalogEntryCommand(toKind(catalogKindPath), request.name());
    }

    public ListAdministrationCatalogEntriesQuery toListQuery(String catalogKindPath) {
        return new ListAdministrationCatalogEntriesQuery(toKind(catalogKindPath), false);
    }

    public ActivateAdministrationCatalogEntryCommand toActivateCommand(UUID catalogEntryId) {
        return new ActivateAdministrationCatalogEntryCommand(catalogEntryId);
    }

    public DeactivateAdministrationCatalogEntryCommand toDeactivateCommand(UUID catalogEntryId) {
        return new DeactivateAdministrationCatalogEntryCommand(catalogEntryId);
    }

    public AdministrationCatalogEntryResponse toResponse(AdministrationCatalogEntryResult result) {
        Objects.requireNonNull(result, "Catalog entry result must not be null");
        return new AdministrationCatalogEntryResponse(
                result.catalogEntryId(),
                result.kind().name(),
                result.name(),
                result.active()
        );
    }

    public GetAdministrationCatalogEntriesResponse toEntriesResponse(List<AdministrationCatalogEntryResult> results) {
        Objects.requireNonNull(results, "Catalog entries must not be null");
        return new GetAdministrationCatalogEntriesResponse(
                results.stream().map(this::toResponse).toList()
        );
    }

    public GetAdministrationCatalogsResponse toResponse(GetAdministrationCatalogsResult result) {
        Objects.requireNonNull(result, "Catalogs result must not be null");
        return new GetAdministrationCatalogsResponse(
                result.garments().stream().map(this::toResponse).toList(),
                result.fabrics().stream().map(this::toResponse).toList(),
                result.collars().stream().map(this::toResponse).toList(),
                result.sleeves().stream().map(this::toResponse).toList()
        );
    }
}
