package com.magyen.platform.administration.application.dto;

import com.magyen.platform.administration.domain.AdministrationCatalogKind;

/**
 * Consulta de un catálogo de Administración. {@code activeOnly=true} oculta inactivos.
 */
public record ListAdministrationCatalogEntriesQuery(
        AdministrationCatalogKind kind,
        boolean activeOnly
) {
}
