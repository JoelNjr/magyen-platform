package com.magyen.platform.commercial.application.dto;

/**
 * Entrada tipada de especificación comercial para casos de uso de escritura.
 */
public record ProductSpecificationCommand(
        String garmentType,
        String collarType,
        String sleeveType,
        String garmentVariant,
        boolean sublimationRequired,
        boolean embroideryRequired,
        boolean dtfRequired,
        String decorationNotes,
        boolean includesNames,
        boolean includesNumbers,
        boolean includesLogos,
        String personalizationNotes,
        String itemObservations
) {
}
