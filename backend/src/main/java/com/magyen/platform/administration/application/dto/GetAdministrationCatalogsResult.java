package com.magyen.platform.administration.application.dto;

import java.util.List;

/**
 * Los cuatro catálogos de Administración, incluyendo inactivos.
 */
public record GetAdministrationCatalogsResult(
        List<AdministrationCatalogEntryResult> garments,
        List<AdministrationCatalogEntryResult> fabrics,
        List<AdministrationCatalogEntryResult> collars,
        List<AdministrationCatalogEntryResult> sleeves
) {
}
