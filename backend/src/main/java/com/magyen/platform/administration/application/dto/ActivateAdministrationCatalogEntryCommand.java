package com.magyen.platform.administration.application.dto;

import java.util.UUID;

/**
 * Activa una entrada de catálogo existente.
 */
public record ActivateAdministrationCatalogEntryCommand(UUID catalogEntryId) {
}
