package com.magyen.platform.production.application.dto;

/**
 * Representación de la especificación productiva tipada para casos de uso de consulta.
 */
public record ProductionProductSpecificationResult(
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
