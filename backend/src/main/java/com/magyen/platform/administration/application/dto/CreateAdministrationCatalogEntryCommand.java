package com.magyen.platform.administration.application.dto;

import com.magyen.platform.administration.domain.AdministrationCatalogKind;

/**
 * Comando para crear una entrada de catálogo de Administración.
 */
public record CreateAdministrationCatalogEntryCommand(
        AdministrationCatalogKind kind,
        String name
) {
}
