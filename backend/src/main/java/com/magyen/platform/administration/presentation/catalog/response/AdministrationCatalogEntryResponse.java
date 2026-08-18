package com.magyen.platform.administration.presentation.catalog.response;

import java.util.UUID;

/**
 * Entrada de catálogo expuesta a Administración.
 */
public record AdministrationCatalogEntryResponse(
        UUID catalogEntryId,
        String kind,
        String name,
        boolean active
) {
}
