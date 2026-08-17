package com.magyen.platform.commercial.presentation.quotation.request;

/**
 * Payload HTTP tipado de la especificación comercial del producto.
 */
public record ProductSpecificationRequest(
        String garmentType,
        String collarType,
        String sleeveType,
        Boolean cuffRequired,
        Boolean sublimationRequired,
        Boolean embroideryRequired,
        Boolean dtfRequired,
        String decorationNotes,
        Boolean includesNames,
        Boolean includesNumbers,
        Boolean includesLogos,
        String personalizationNotes,
        String itemObservations
) {
}
