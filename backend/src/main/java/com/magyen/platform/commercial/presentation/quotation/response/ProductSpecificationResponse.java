package com.magyen.platform.commercial.presentation.quotation.response;

/**
 * Especificación comercial tipada expuesta por la API de cotización/orden.
 */
public record ProductSpecificationResponse(
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
