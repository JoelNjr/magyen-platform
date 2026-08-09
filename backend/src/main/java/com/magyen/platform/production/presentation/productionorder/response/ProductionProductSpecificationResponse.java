package com.magyen.platform.production.presentation.productionorder.response;

/**
 * Especificación productiva tipada expuesta por la API de consulta.
 */
public record ProductionProductSpecificationResponse(
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
