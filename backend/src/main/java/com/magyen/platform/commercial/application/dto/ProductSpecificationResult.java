package com.magyen.platform.commercial.application.dto;

/**
 * Representación de la especificación comercial tipada para casos de uso de consulta.
 */
public record ProductSpecificationResult(
        String garmentType,
        String collarType,
        String sleeveType,
        Boolean cuffRequired,
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
