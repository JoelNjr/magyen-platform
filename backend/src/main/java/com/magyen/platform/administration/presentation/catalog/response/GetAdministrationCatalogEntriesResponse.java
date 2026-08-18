package com.magyen.platform.administration.presentation.catalog.response;

import java.util.List;

/**
 * Lista de un catálogo concreto.
 */
public record GetAdministrationCatalogEntriesResponse(List<AdministrationCatalogEntryResponse> entries) {
}
