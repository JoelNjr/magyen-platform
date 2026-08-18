package com.magyen.platform.administration.application.dto;

import com.magyen.platform.administration.domain.AdministrationCatalogKind;

import java.util.UUID;

/**
 * Vista de aplicación de una entrada de catálogo de Administración.
 */
public record AdministrationCatalogEntryResult(
        UUID catalogEntryId,
        AdministrationCatalogKind kind,
        String name,
        boolean active
) {
}
