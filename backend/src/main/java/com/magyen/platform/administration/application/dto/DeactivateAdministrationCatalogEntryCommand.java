package com.magyen.platform.administration.application.dto;

import java.util.UUID;

/**
 * Desactiva una entrada de catálogo. No elimina la fila.
 */
public record DeactivateAdministrationCatalogEntryCommand(UUID catalogEntryId) {
}
