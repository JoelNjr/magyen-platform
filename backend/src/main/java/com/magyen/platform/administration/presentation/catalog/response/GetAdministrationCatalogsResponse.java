package com.magyen.platform.administration.presentation.catalog.response;

import java.util.List;

/**
 * Los cuatro catálogos de Administración.
 */
public record GetAdministrationCatalogsResponse(
        List<AdministrationCatalogEntryResponse> garments,
        List<AdministrationCatalogEntryResponse> fabrics,
        List<AdministrationCatalogEntryResponse> collars,
        List<AdministrationCatalogEntryResponse> sleeves
) {
}
